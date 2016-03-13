package de.htwg.tqm.server.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MapDBPersistenceServiceTest {

    private static final String TMP_TQM_DB = "/tmp/tqm-db-test";
    private MapDBPersistenceService sut;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        new File(TMP_TQM_DB).delete();
        sut = new MapDBPersistenceService(TMP_TQM_DB);
    }

    @After
    public void tearDown() throws Exception {
        new File(TMP_TQM_DB).delete();
        new File(TMP_TQM_DB + ".p").delete();
        new File(TMP_TQM_DB + ".t").delete();
    }

    @Test
    public void testStore() throws Exception {
        String key = "-1232332";
        JsonNode value = mapper.createObjectNode().put("my", "content");
        String collection1 = "c1";
        String collection2 = "c2";
        sut.getCollection(collection1).store(key, value);

        assertTrue(sut.getCollection(collection1).containsKey(key));
        assertThat(sut.getCollection(collection1).load(key), equalTo(value));
        assertThat(sut.getCollection(collection1).load(key + "2"), equalTo(null));

        assertFalse(sut.getCollection(collection2).containsKey(key));

        assertThat(sut.getCollection(collection1).loadKeys().size(), is(1));
        assertThat(sut.getCollection(collection1).loadKeys().contains(key), is(true));
    }
}