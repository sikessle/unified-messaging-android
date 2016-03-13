package de.htwg.tqm.server.beans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBeanTest {

    ObjectMapper mapper;
    private Object sut;
    private Class<?> sutType;

    @Before
    public final void setUp() throws Exception {
        mapper = new ObjectMapper();
        sut = createSut();
        sutType = sut.getClass();
    }

    protected abstract Object createSut();


    protected boolean doTestDeserialize() {
        return true;
    }

    protected boolean doTestCompareJsonAfterSerialization() {
        return false;
    }

    protected String getExpectedJsonAfterSerialization() throws Exception {
        return "";
    }

    protected String getJsonToDeserialize() throws Exception {
        return mapper.writeValueAsString(sut);
    }

    protected String readFile(String path) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }

    @Test
    public final void testSerialize() throws Exception {
        assertTrue(mapper.canSerialize(sutType));
    }

    @Test
    public final void testDeserialize() throws Exception {
        if (doTestDeserialize()) {
            final String serializedSut = getJsonToDeserialize();
            final Object actual = mapper.readValue(serializedSut, sutType);
            assertEquals(sut, actual);
        }
    }

    @Test
    public void testCompareJsonAfterSerialization() throws Exception {
        if (doTestCompareJsonAfterSerialization()) {
            JsonNode expected = mapper.readTree(getExpectedJsonAfterSerialization());
            JsonNode actual = mapper.readTree(mapper.writeValueAsString(sut));
            assertEquals(expected, actual);
        }
    }
}