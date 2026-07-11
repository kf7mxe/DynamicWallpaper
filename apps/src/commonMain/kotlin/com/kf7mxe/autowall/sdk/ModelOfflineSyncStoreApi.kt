package com.kf7mxe.autowall.sdk


import com.foodecision.sdk.API_TIMEOUT
import com.foodecision.sdk.hasSubscription
import com.foodecision.sdk.session
import com.foodecision.storage.getFileByteArray
import com.foodecision.storage.saveFile
import com.kf7mxe.autowall.MaybeHasUser
import com.kf7mxe.autowall.ModelTableVersionContainer
import com.lightningkite.kiteui.reactive.*

import com.lightningkite.lightningserver.db.ModelCache
import com.lightningkite.reactive.context.*
import com.lightningkite.reactive.core.*
import com.lightningkite.reactive.extensions.*
import com.lightningkite.reactive.lensing.*
import com.lightningkite.services.database.Condition
import com.lightningkite.services.database.HasId
import com.lightningkite.services.database.Query
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ModelOfflineSyncStoreApi<T, ID, UID>(
    val remote: ModelCache<T, ID>?,
    val serializer: KSerializer<T>,
    private val className: String,
    private val filterCondition: Condition<T>,
    private val syncOfflineMedia: (suspend (List<T>) -> List<T>)?,
    private val syncRemoteMedia: (suspend (List<T>) -> List<T>)?,
    val copyWithUser: (model: T, userId: UID) -> T
) where T : HasId<ID>,
        T : MaybeHasUser<UID>,
        ID : Comparable<ID>,
        UID : Comparable<UID>
{

    val items = Signal(emptyList<T>())
    private val loadMutex = Mutex()
    private var isLoaded = false
    private val localStorageFileName = "${className}_data.json"

    // A dedicated scope for background tasks that won't be cancelled with the UI
    val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
    }

    init {
        // Launch a coroutine to pre-load data on initialization
        CoroutineScope(Dispatchers.Default).launch {
            ensureLoaded()
        }
    }

    /**
     * Ensures data is loaded. It prioritizes loading local data for immediate UI display,
     * then triggers a background sync with the remote source.
     */
    @PublishedApi
    internal suspend fun ensureLoaded() {
        if (isLoaded) return
        loadMutex.withLock {
            if (isLoaded) return@withLock

            val localData = loadFromLocalFile()
            if (localData != null) {
                // --- Offline First Path ---
                // 1. Immediately display local data
                items.value = localData
                isLoaded = true
                // 2. Launch background sync
                backgroundScope.launch {
                    if(className=="GenericIngredient") return@launch
                    val syncedData = synchronize(localItems = localData)
                    items.value = syncedData
                    persistItems()
                }
            } else {
                // --- First Time / No Cache Path ---
                // No local data, fetch from remote before declaring loaded
                if(className=="GenericIngredient") return
                val remoteItems = fetchRemoteData(Query(filterCondition))
                items.value = syncRemoteMedia?.invoke(remoteItems) ?: remoteItems
                persistItems()
                isLoaded = true
            }
        }
    }

    /**
     * Synchronizes local data with the remote source. This is designed to run in the background.
     */
    private suspend fun synchronize(localItems: List<T>): List<T> {
        val remoteItems = fetchRemoteData(Query())
        // If there's no network or remote data, stick with local
        if (remoteItems.isEmpty()) return localItems

        // 1. Download media for new items from the server
        val remoteItemsWithSyncedMedia = syncNewRemoteItems(remoteItems, localItems)

        // 2. Upload new local items (and their media) to the server if subscribed
        val hasActiveSubscription: Boolean = withTimeoutOrNull(API_TIMEOUT) { hasSubscription.invoke()} ?: false
        if (hasActiveSubscription) {
            uploadNewLocalItems(localItems, remoteItemsWithSyncedMedia)
        }

        // 3. Merge local and remote lists
        return if (hasActiveSubscription) {
            (localItems + remoteItemsWithSyncedMedia).distinctBy { it._id }
        } else {
            localItems
        }
    }

    /**
     * Identifies new remote items, downloads their media, and returns the full list of remote items.
     */
    private suspend fun syncNewRemoteItems(remoteItems: List<T>, localItems: List<T>): List<T> {
        val localIds = localItems.map { it._id }.toSet()
        val newRemoteItems = remoteItems.filter { it._id !in localIds }

        if (newRemoteItems.isEmpty()) return remoteItems

        val existingRemoteItems = remoteItems - newRemoteItems.toSet()
        val syncedNewItems = syncRemoteMedia?.invoke(newRemoteItems) ?: newRemoteItems

        return syncedNewItems + existingRemoteItems
    }

    /**
     * Identifies local items that are not on the server, syncs their media, and uploads them.
     */
    private suspend fun uploadNewLocalItems(localItems: List<T>, remoteItems: List<T>) {
        val remoteIds = remoteItems.map { it._id }.toSet()
        val newLocalItems = localItems.filter { it._id !in remoteIds }

        if (newLocalItems.isEmpty()) return

        val itemsWithMediaPaths = syncOfflineMedia?.invoke(newLocalItems) ?: newLocalItems
        val currentUserId = session().userId ?: return

        val itemsWithUser = itemsWithMediaPaths.map {
            @Suppress("UNCHECKED_CAST")
            copyWithUser(it, currentUserId as UID)
        }

        remote?.addAll(itemsWithUser)
    }

    /**
     * Deserializes and returns the list of items from the local JSON file.
     */
    private suspend fun loadFromLocalFile(): List<T>? {
        return try {
            getFileByteArray(localStorageFileName)?.let { byteArray ->
                val decodedString = byteArray.decodeToString()
                val container = Json.decodeFromString(ModelTableVersionContainer.serializer(), decodedString)
                Json.decodeFromString(ListSerializer(serializer), container.table)
            }
        } catch (e: Exception) {
            // Log error if needed
            println("Error loading from local file: ${e.message}")
            null
        }
    }

    /**
     * Fetches a list of items from the remote source with a timeout.
     */
    private suspend fun fetchRemoteData(query: Query<T>): List<T> {
        return withTimeoutOrNull(API_TIMEOUT) {
            remote?.query(query)?.invoke()
        } ?: emptyList()
    }

    /**
     * Serializes the current list of items and saves it to a local file.
     */
    suspend fun persistItems() {
        val jsonStringTable = Json.encodeToString(ListSerializer(serializer), items.value)
        val container = ModelTableVersionContainer(table = jsonStringTable)
        val jsonContainerString = Json.encodeToString(ModelTableVersionContainer.serializer(), container)
        println("DEBUG jsonContainerString ${jsonContainerString}")
        saveFile(jsonContainerString.encodeToByteArray(), localStorageFileName)
    }

    suspend fun isEmpty(): Boolean {
        ensureLoaded()
        return items.value.isEmpty()
    }

    suspend fun detail(id: ID): T? {
        ensureLoaded()
        return items.value.find { it._id == id }
    }

    suspend inline fun upsert(model: T) {
        ensureLoaded()

        val modelToSave = session().me()?._id?.let { currentUserId ->
            @Suppress("UNCHECKED_CAST")
            copyWithUser(model, currentUserId as UID)
        } ?: model

        val updatedList = items.value.toMutableList()
        val existingIndex = updatedList.indexOfFirst { it._id == modelToSave._id }
        if (existingIndex != -1) {
            updatedList[existingIndex] = modelToSave
        } else {
            updatedList.add(modelToSave)
        }
        items.value = updatedList

        persistItems()

        // Launch the remote update in the background
        backgroundScope.launch {
            if (hasSubscription()) remote?.upsert(modelToSave)
        }
    }

    suspend inline fun deleteById(id: ID) {
        ensureLoaded()
        items.value = items.value.filter { it._id != id }
        persistItems()

        // Launch the remote delete in the background
        backgroundScope.launch {
            remote?.skipCache?.delete(id)
        }
    }

    suspend inline fun <reified T> deleteAll() {
//        deleteAllLocalStorage<T>()

        // Launch the remote deletes in the background
        backgroundScope.launch {
            items.value.forEach { item ->
                remote?.skipCache?.delete(item._id)
            }
        }

        items.value = emptyList()
        persistItems()
    }
}