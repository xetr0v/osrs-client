package org.osrs.client.prop;

import java.util.HashMap;
import java.util.Map;

/**
 * oldrs
 * 2.3.2013
 */
public class Section {

    private Map<String, String> entries;
    private String name;

    public Section(String name) {
        entries = new HashMap<String, String>();
        this.name = name;
    }

    public Map<String, String> getEntries() {
        return entries;
    }

    public String getProperty(String key) {
        if(!entries.containsKey(key))
            throw new IllegalArgumentException();
        return entries.get(key);
    }

    public void putProperty(String key, String value) {
        entries.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void clear() {
        entries.clear();
    }
}
