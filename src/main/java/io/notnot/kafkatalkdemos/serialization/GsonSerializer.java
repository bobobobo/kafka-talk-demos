package io.notnot.kafkatalkdemos.serialization;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class GsonSerializer<T> implements Serializer<T> {
    private static Gson gson;


    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        return GsonSupplier.get().toJson(data).getBytes(StandardCharsets.UTF_8);
    }

}
