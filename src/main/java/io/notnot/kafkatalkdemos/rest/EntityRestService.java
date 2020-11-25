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

import io.notnot.kafkatalkdemos.serialization.GsonSupplier;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.before;
import static spark.Spark.options;

public class EntityRestService {

    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo hostInfo;

    public EntityRestService(
            final KafkaStreams streams,
                             final HostInfo hostInfo) {
        this.streams = streams;
        this.metadataService = new MetadataService(streams);
        this.hostInfo = hostInfo;

    }

    public Object getEntityById(final String storeName, final String id) {

//        final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(storeName, id);
//        if (!thisHost(hostStoreInfo)) {
//            return fetchByKey(hostStoreInfo, entityName + "/" + id);
//        }

        final ReadOnlyKeyValueStore<String, Object> store = streams.store(storeName, QueryableStoreTypes.keyValueStore());
        if (store == null) {
            throw new RuntimeException("State store not found");
        }

        return store.get(id);

    }

    private Object fetchByKey(final HostStoreInfo host, final String path) {
//    return client.target(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path))
//        .request(MediaType.APPLICATION_JSON_TYPE)
//        .get(new GenericType<KeyValueBean>() {
//        });
        return null;
    }


    private HostStoreInfo streamsMetadataForStoreAndKey(String store, String key) {
        return metadataService.streamsMetadataForStoreAndKey(store, key, new StringSerializer());
    }


    private List<Object> getAllEntities(final String storeName) {
        final ReadOnlyKeyValueStore<String, Object> store = streams.store(storeName, QueryableStoreTypes.keyValueStore());
        final List<Object> results = new ArrayList<>();
        KeyValueIterator<String, Object> all = store.all();
        while (all.hasNext()) {
            final KeyValue<String, Object> next = all.next();
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
        enableCORS();
        Spark.get("/:store", (request, response) -> {
            response.header("content-type", "application/json");
            return GsonSupplier.get().toJson(getAllEntities(request.params("store")));
        });
        Spark.get("/:store/:id", (request, response) -> {
            response.header("content-type", "application/json");
            return GsonSupplier.get().toJson(getEntityById(request.params("store"), request.params("id")));
        });
    }

    private void enableCORS() {

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
}

