package com.example.blps.connector.record;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import jakarta.resource.cci.MappedRecord;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BitrixMappedRecord implements MappedRecord {

    private static final long serialVersionUID = 1L;

    private String recordName;

    private String description;

    private Map<String, Object> parameters = new HashMap<>();

    private String response;

    public BitrixMappedRecord() {
    }

    public BitrixMappedRecord(String recordName) {
        this.recordName = recordName;
    }

    @Override
    public void setRecordName(String name) {
        this.recordName = name;
    }

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public void setRecordShortDescription(String description) {
        this.description = description;
    }

    @Override
    public String getRecordShortDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MappedRecord) {
            return getRecordName().equals(((MappedRecord) other).getRecordName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return recordName != null ? recordName.hashCode() : 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BitrixMappedRecord clone = new BitrixMappedRecord(recordName);
        clone.description = description;
        clone.parameters = new HashMap<>(parameters);
        clone.response = response;
        return clone;
    }

    @Override
    public int size() {
        return parameters.size();
    }

    @Override
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return parameters.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return parameters.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return parameters.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        if (key instanceof String) {
            return parameters.put((String) key, value);
        }
        throw new IllegalArgumentException("Key must be a String");
    }

    @Override
    public Object remove(Object key) {
        return parameters.remove(key);
    }

    @Override
    public void putAll(Map m) {
        for (Object entryObj : m.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            Object key = entry.getKey();
            if (key instanceof String) {
                parameters.put((String) key, entry.getValue());
            } else {
                throw new IllegalArgumentException("Keys must be Strings");
            }
        }
    }

    @Override
    public void clear() {
        parameters.clear();
    }

    @Override
    public Set<String> keySet() {
        return parameters.keySet();
    }

    @Override
    public Collection<Object> values() {
        return parameters.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return parameters.entrySet();
    }
}