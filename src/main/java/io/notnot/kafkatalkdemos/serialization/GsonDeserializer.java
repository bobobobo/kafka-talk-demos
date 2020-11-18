package io.notnot.kafkatalkdemos.serialization;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class GsonDeserializer<T> implements Deserializer<T> {

    private final Class<T> cls;

    public GsonDeserializer(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        final String json = new String(data, StandardCharsets.UTF_8);
        try {
            return GsonSupplier.get().fromJson(json, cls);
        } catch (Throwable t) {
            throw new SerializationException();
        }
    }
}
