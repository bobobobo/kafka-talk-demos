package io.notnot.kafkatalkdemos;

import io.notnot.kafkatalkdemos.domain.Order;
import io.notnot.kafkatalkdemos.serialization.GsonSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class CustomerOrderCountService {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String NEW_ORDER_TOPIC = "demo-new-order";
    private static final String CUSTOMER_ORDER_COUNT_TOPIC = "demo-customer-order-count";
    public static final String APPLICATION_ID = "customer-order-count-service";

    public static void main(String[] args) {
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(NEW_ORDER_TOPIC, Consumed.with(Serdes.String(), GsonSerde.get(Order.class)))
                .groupBy((orderId, order) -> order.getCustomerId())
                .count()
                .toStream()
                .mapValues(String::valueOf)
                .to(CUSTOMER_ORDER_COUNT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, getKafkaConfiguration());
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
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
