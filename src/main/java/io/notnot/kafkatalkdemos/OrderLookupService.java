package io.notnot.kafkatalkdemos;

import io.notnot.kafkatalkdemos.domain.EnrichedOrder;
import io.notnot.kafkatalkdemos.rest.OrderRestService;
import io.notnot.kafkatalkdemos.serialization.GsonSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class OrderLookupService {
    public static final String APPLICATION_ID = "order-service";
    public static final String ORDER_STORE = "order-store";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String ENRICHED_ORDER_TOPIC = "demo-enriched-order";
    private static final int PORT = 8082;

    public static void main(String[] args) {
        final StreamsBuilder builder = new StreamsBuilder();


        builder.stream(ENRICHED_ORDER_TOPIC, Consumed.with(Serdes.String(), GsonSerde.get(EnrichedOrder.class)))
                .toTable(Materialized.<String, EnrichedOrder, KeyValueStore<Bytes, byte[]>>as(ORDER_STORE).withValueSerde(GsonSerde.get(EnrichedOrder.class)));

        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, getKafkaConfiguration());
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();

            OrderRestService restService = new OrderRestService(streams, new HostInfo("localhost", PORT));
            restService.start(PORT);

            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    public static Properties getKafkaConfiguration() {
        Properties config = new Properties();
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return config;
    }

}
