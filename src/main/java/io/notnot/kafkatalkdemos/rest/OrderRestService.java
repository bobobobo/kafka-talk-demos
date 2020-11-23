/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.notnot.kafkatalkdemos.rest;

import io.notnot.kafkatalkdemos.OrderLookupService;
import io.notnot.kafkatalkdemos.domain.EnrichedOrder;
import io.notnot.kafkatalkdemos.serialization.GsonSupplier;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.jetty.server.Server;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;

public class OrderRestService {

    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo hostInfo;
    private Server jettyServer;

    public OrderRestService(final KafkaStreams streams,
                            final HostInfo hostInfo) {
        this.streams = streams;
        this.metadataService = new MetadataService(streams);
        this.hostInfo = hostInfo;

    }

    public EnrichedOrder getOrderById(final String id) {

//        final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(OrderLookupService.ORDER_STORE, id);
//        if (!thisHost(hostStoreInfo)) {
//            return fetchByKey(hostStoreInfo, "order/" + id);
//        }

        final ReadOnlyKeyValueStore<String, EnrichedOrder> store = streams.store(OrderLookupService.ORDER_STORE, QueryableStoreTypes.keyValueStore());
        if (store == null) {
            throw new RuntimeException("State store not found");
        }

        return store.get(id);

    }

    private EnrichedOrder fetchByKey(final HostStoreInfo host, final String path) {
//    return client.target(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path))
//        .request(MediaType.APPLICATION_JSON_TYPE)
//        .get(new GenericType<KeyValueBean>() {
//        });
        return null;
    }


    private HostStoreInfo streamsMetadataForStoreAndKey(String store, String key) {
        return metadataService.streamsMetadataForStoreAndKey(store, key, new StringSerializer());
    }


    private List<EnrichedOrder> getAllOrders() {
        final ReadOnlyKeyValueStore<String, EnrichedOrder> store = streams.store(OrderLookupService.ORDER_STORE, QueryableStoreTypes.keyValueStore());
        final List<EnrichedOrder> results = new ArrayList<>();
        KeyValueIterator<String, EnrichedOrder> all = store.all();
        while (all.hasNext()) {
            final KeyValue<String, EnrichedOrder> next = all.next();
            results.add(next.value);
        }

        return results;
    }

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(hostInfo.host()) &&
                host.getPort() == hostInfo.port();
    }


    public void start(final int port) throws Exception {
        Spark.port(port);

        Spark.get("/order", (request, response) -> {
            response.header("content-type", "application/json");
            return GsonSupplier.get().toJson(getAllOrders());
        });
        Spark.get("/order/:orderId", (request, response) -> {
            response.header("content-type", "application/json");
            return GsonSupplier.get().toJson(getOrderById(request.params("orderId")));
        });
    }


}

