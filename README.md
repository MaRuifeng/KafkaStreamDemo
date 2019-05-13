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
    ```
    mvn clean package
    mvn exec:java -Dexec.mainClass=com.flyer.kafka.WordCountDemo
    ```
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

## Note
1. This demo application is written with [Kafka Streams DSL](https://docs.confluent.io/current/streams/developer-guide/dsl-api.html). It's worthy to rewrite it using low level [Processor API](https://docs.confluent.io/current/streams/developer-guide/processor-api.html).
2. The default aggregate function `count` involves state store materialization, which has a commit frequency. Based on the `commit.interval.ms` config setting, there will be a delay in aggregation results in the demo. This is better explained in this [SO entry](https://stackoverflow.com/questions/44711499/apache-kafka-streams-materializing-ktables-to-a-topic-seems-slow) and this [article](https://cwiki.apache.org/confluence/display/KAFKA/KIP-63%3A+Unify+store+and+downstream+caching+in+streams). 
    > The semantics of caching is that data is flushed to the state store and forwarded to the next downstream processor node whenever the earliest of commit.interval.ms or cache.max.bytes.buffering (cache pressure) hits. 
 




