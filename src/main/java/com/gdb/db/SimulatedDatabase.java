package com.gdb.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory simulated database table engine for logging tests. */
public class SimulatedDatabase {
    private final Map<String, List<Object>> tables = new HashMap<>();

    public void insert(String table, Object object) {
        tables.computeIfAbsent(table, ignored -> new ArrayList<>()).add(object);
    }

    public List<Object> selectAll(String table) {
        return new ArrayList<>(tables.getOrDefault(table, Collections.emptyList()));
    }

    public void deleteAll(String table) {
        tables.put(table, new ArrayList<>());
    }

    public int count(String table) {
        return tables.getOrDefault(table, Collections.emptyList()).size();
    }
}
