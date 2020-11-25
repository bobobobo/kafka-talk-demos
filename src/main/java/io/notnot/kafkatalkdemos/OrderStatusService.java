package io.notnot.kafkatalkdemos;

import io.notnot.kafkatalkdemos.domain.OrderStatus;
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

public class OrderStatusService {
    private static final int HTTP_PORT = 8081;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String ORDER_STATUS_TOPIC = "demo-order-status";

    public static void main(String[] args) {
        Spark.port(HTTP_PORT);
        enableCORS();
        KafkaProducer<String, String> orderProducer = new KafkaProducer<>(
                getKafkaConfiguration(), new StringSerializer(), new StringSerializer());

        Spark.put("/order/:orderId/:status", (request, response) -> {
            OrderStatus status = OrderStatus.valueOf(request.params("status"));
            if(status == null) {
                response.status(406);
                return "";
            }

            Future<RecordMetadata> result = orderProducer.send(new ProducerRecord(ORDER_STATUS_TOPIC, request.params("orderId"), status.name()));

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
