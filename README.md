# Kafka Streams - Word count demo program

This is an [example](https://kafka.apache.org/22/documentation/streams/quickstart) from the Kafka official documentation which demonstrates 
how to write a streaming application with the *Kafka Streams* client library. 

The source code is found [here](https://github.com/apache/kafka/blob/2.2/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountDemo.java). 

## Set up and run

1. Start the local Zookeeper and Kafka servers
2. Initiate a topic for the input stream
    ```
    kafka-topics --create \
        --zookeeper 127.0.0.1:2181 \
        --replication-factor 1 \
        --partitions 1 \
        --topic kafka-stream-wordcount-input
    ```
3. Initiate a topic for the output stream
    ```
    kafka-topics --create \
        --zookeeper 127.0.0.1:2181 \
        --replication-factor 1 \
        --partitions 1 \
        --topic kafka-stream-wordcount-output \
        --config cleanup.policy=compact
    ```
4. Run the word count demo Java application
5. Start a producer to write data to the input stream topic
    ```
    kafka-console-producer --broker-list 127.0.0.1:9092 --topic kafka-stream-wordcount-input
    ```
6. Start a consumer to observe results from the output stream topic
    ```
    kafka-console-consumer --bootstrap-server 127.0.0.1:9092 \
    	--topic kafka-stream-wordcount-output \
        --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter \
        --property print.key=true \
        --property print.value=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    ```
7. When writing data strings via the producer, the consumer should display word counts continuously in a streaming manner





