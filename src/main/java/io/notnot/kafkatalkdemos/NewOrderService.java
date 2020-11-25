package io.notnot.kafkatalkdemos;

import io.notnot.kafkatalkdemos.domain.Order;
import io.notnot.kafkatalkdemos.serialization.GsonSerializer;
import io.notnot.kafkatalkdemos.serialization.GsonSupplier;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import spark.Spark;

import java.util.Properties;
import java.util.concurrent.Future;

import static spark.Spark.before;
import static spark.Spark.options;

public class NewOrderService {
    private static final int HTTP_PORT = 8080;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String NEW_ORDER_TOPIC = "demo-new-order";

    public static void main(String[] args) {
        Spark.port(HTTP_PORT);
        enableCORS();
        KafkaProducer<String, Order> orderProducer = new KafkaProducer<>(
                getKafkaConfiguration(), new StringSerializer(), new GsonSerializer<>());

        Spark.post("/order", (request, response) -> {
            Order order = GsonSupplier.get().fromJson(request.body(), Order.class);
            Future<RecordMetadata> result = orderProducer.send(new ProducerRecord<>(NEW_ORDER_TOPIC, order.getId(), order));

            return String.valueOf(result.get().offset());
        });
    }

    private static void enableCORS() {

        options("/*", (request, response) ->
        {
            return "OK";
        });

        before((request, response) ->
        {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
    }

    public static Properties getKafkaConfiguration() {
        Properties config = new Properties();
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        return config;
    }
}
