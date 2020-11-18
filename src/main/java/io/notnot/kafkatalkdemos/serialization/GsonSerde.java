package io.notnot.kafkatalkdemos.serialization;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class GsonSerde {

    public static <T> Serde<T> get(Class<T> cls) {
        return Serdes.serdeFrom(new GsonSerializer<>(), new GsonDeserializer<>(cls));
    }

}
