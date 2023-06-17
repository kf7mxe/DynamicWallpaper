package com.kf7mxe.dynamicwallpaper.database;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Converters {

    @TypeConverter
    public String fromStringMap(Map<String, String> value) {
        TreeMap<String, String> sortedMap = new TreeMap<>(value);
        return String.join(",", sortedMap.keySet()) + "<divider>" +
                String.join(",", sortedMap.values());
    }

    @TypeConverter
    public Map<String, String> toStringMap(String value) {
        String[] parts = value.split("<divider>");
        List<String> keys = Arrays.asList(parts[0].split(","));
        List<String> values = Arrays.asList(parts[1].split(","));

        Map<String, String> res = new HashMap<>();
        for (int index = 0; index < keys.size(); index++) {
            String key = keys.get(index);
            String val = (index < values.size()) ? values.get(index) : "";
            res.put(key, val);
        }
        return res;
    }
}
