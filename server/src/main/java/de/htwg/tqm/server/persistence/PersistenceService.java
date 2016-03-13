package de.htwg.tqm.server.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;

@ThreadSafe
public interface PersistenceService {

    /**
     * A collection is a bundled resource (i.e. users, bookings, etc.)
     */
    @NotNull Collection getCollection(@NotNull String collectionName);

    @ThreadSafe
    interface Collection {

        /**
         * Saves the value for the unique key in the database. Any existing value will be overwritten.
         */
        void store(@NotNull String key, @NotNull JsonNode value);

        /**
         * Tries to retrieve the value for the key.
         */
        @Nullable JsonNode load(@NotNull String key);

        /**
         * Checks if the key is in this collection.
         */
        boolean containsKey(@NotNull String key);

        /**
         * Returns all keys sorted ascending.
         */
        @NotNull SortedSet<String> loadKeys();

        /**
         * Returns all values of this collection.
         */
        @NotNull java.util.Collection<JsonNode> loadValues();

    }

}
