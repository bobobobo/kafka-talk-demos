package io.notnot.kafkatalkdemos;

import io.notnot.kafkatalkdemos.domain.*;
import io.notnot.kafkatalkdemos.serialization.GsonSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class EnrichOrderService {
    public static final String APPLICATION_ID = "enrich-order-service";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String NEW_ORDER_TOPIC = "demo-new-order";
    private static final String ENRICHED_ORDER_TOPIC = "demo-enriched-order";
    private static final String CUSTOMER_STORE = "customer-store";
    private static final String CUSTOMER_TOPIC = "demo-customer";
    private static final String PRODUCT_TOPIC = "demo-product";
    private static final String PRODUCT_STORE = "product-store";

    public static void main(String[] args) {
        final StreamsBuilder builder = new StreamsBuilder();


        final GlobalKTable<String, Customer> customers =
                builder.globalTable(CUSTOMER_TOPIC, Materialized.<String, Customer, KeyValueStore<Bytes, byte[]>>as(CUSTOMER_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(GsonSerde.get(Customer.class)));

        final GlobalKTable<String, Product> products =
                builder.globalTable(PRODUCT_TOPIC, Materialized.<String, Product, KeyValueStore<Bytes, byte[]>>as(PRODUCT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(GsonSerde.get(Product.class)));

        builder.stream(NEW_ORDER_TOPIC, Consumed.with(Serdes.String(), GsonSerde.get(Order.class)))
                .join(customers,
                        (orderId, order) -> order.getCustomerId(),
                        (order, customer) -> new OrderAndCustomer(order, customer))
                .transform(() -> new EnrichProductsTransformer())
                .to(ENRICHED_ORDER_TOPIC, Produced.with(Serdes.String(), GsonSerde.get(EnrichedOrder.class)));

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

    public static class OrderAndCustomer {
        private final Order order;
        private final Customer customer;

        public OrderAndCustomer(Order order, Customer customer) {
            this.order = order;
            this.customer = customer;
        }

        public Order getOrder() {
            return order;
        }

        public Customer getCustomer() {
            return customer;
        }
    }

    public static class EnrichProductsTransformer implements Transformer<String, OrderAndCustomer, KeyValue<String, EnrichedOrder>> {
        private ReadOnlyKeyValueStore<String, ValueAndTimestamp<Product>> stateStore;

        @Override
        public void init(ProcessorContext context) {
            stateStore = (ReadOnlyKeyValueStore) context.getStateStore("product-store");
        }

        @Override
        public KeyValue<String, EnrichedOrder> transform(String key, OrderAndCustomer value) {
            List<Product> products = value.getOrder().getProductIds().stream()
                    .map(stateStore::get)
                    .filter(Objects::nonNull)
                    .map(ValueAndTimestamp::value)
                    .collect(Collectors.toList());
            return KeyValue.pair(key, ImmutableEnrichedOrder.builder()
                    .id(value.getOrder().getId())
                    .customer(value.getCustomer())
                    .addAllProduct(products).build());
        }

        @Override
        public void close() {

        }
    }

}
