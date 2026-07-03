package com.kf7mxe.autowall.engine

actual fun scheduleTriggers() {
    TriggerScheduler.scheduleAll()
}
