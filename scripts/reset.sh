#!/bin/bash
KAFKA_HOME=/opt/kafka_2.12-2.3.0/
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-customer --add-config retention.ms=1000
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-enriched-order --add-config retention.ms=1000
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-new-order --add-config retention.ms=1000
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-customer-order-count --add-config retention.ms=1000
sleep 30
$KAFKA_HOME/bin/kafka-streams-application-reset.sh --application-id crm-customer-service --input-topics demo-crm-customer --bootstrap-servers localhost:9092 --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-streams-application-reset.sh --application-id customer-order-count-service --input-topics demo-new-order --bootstrap-servers localhost:9092 --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-streams-application-reset.sh --application-id enrich-order-service --input-topics demo-new-order,demo-customer,demo-product --bootstrap-servers localhost:9092 --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-streams-application-reset.sh --application-id order-service --input-topics demo-enriched-order --bootstrap-servers localhost:9092 --zookeeper localhost:2181
rm -rf /tmp/kafka-streams/*
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-customer --delete-config retention.ms
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-enriched-order --delete-config retention.ms
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-new-order --delete-config retention.ms
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --entity-name demo-customer-order-count --delete-config retention.ms
