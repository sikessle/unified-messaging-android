package de.htwg.tqm.server.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

@ThreadSafe
@Singleton
public final class MapDBPersistenceService implements PersistenceService {

    private final DB db;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public MapDBPersistenceService(@NotNull @Named("dbPath") String dbPath) {
        db = DBMaker
                .newFileDB(new File(dbPath))
                .closeOnJvmShutdown()
                .make();
    }

    @Override
    public @NotNull Collection getCollection(@NotNull String collectionName) {
        return new MapDBCollection(db.getTreeMap(collectionName));
    }

    @ThreadSafe
    private final class MapDBCollection implements Collection {
        private final ConcurrentNavigableMap<String, String> map;

        public MapDBCollection(@NotNull ConcurrentNavigableMap<String, String> map) {
            this.map = map;
        }

        @Override
        public void store(@NotNull String key, @NotNull JsonNode value) {
            map.put(key, value.toString());
            db.commit();
        }

        @Override
        public @Nullable JsonNode load(@NotNull String key) {
            if (!containsKey(key)) {
                return null;
            }
            try {
                return mapper.readTree(map.get(key));
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public boolean containsKey(@NotNull String key) {
            return map.containsKey(key);
        }

        @Override
        public @NotNull SortedSet<String> loadKeys() {
            SortedSet<String> keys = new TreeSet<>();
            map.keySet().forEach(key -> keys.add((String) key));
            return keys;
        }


        @Override
        public @NotNull java.util.Collection<JsonNode> loadValues() {
            java.util.Collection<JsonNode> values = new LinkedList<>();
            map.values().forEach(value -> {
                try {
                    values.add(mapper.readTree(value));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return values;
        }

    }
}
