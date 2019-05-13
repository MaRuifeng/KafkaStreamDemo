package com.flyer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Re-doing the word count example for Kafka stream demo.
 *
 * https://kafka.apache.org/22/documentation/streams/quickstart
 * https://github.com/apache/kafka/blob/2.2/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountDemo.java
 *
 * Make sure the topic and producer are created before running this program.
 */

public final class WordCountDemo {

    private static final Logger logger = LoggerFactory.getLogger(WordCountDemo.class.getSimpleName());

    private static Properties getStreamsConfig() {
        Properties props = new Properties();

        // https://kafka.apache.org/11/documentation/streams/developer-guide/config-streams

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-stream-wordcount"); // streaming application ID
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092"); // Kafka bootstrap server
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName()); // default key ser/des class
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName()); // default value ser/des class
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // no buffer cache
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000); // default to 30,000 ms

        return props;
    }

    private static Topology createTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("kafka-stream-wordcount-input");

        // compact version
//        source.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
//                .groupBy((key, value) -> value)
//                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count-store"))
//                .toStream()
//                .to("kafka-stream-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        // breakdown version
        KStream<String, String> splitWordStream = source.flatMapValues((value)
                -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")));

        splitWordStream.foreach((key, value) -> {
            logger.info("Source stream hashcode: " + source.hashCode());
            logger.info("[split word entry from latest input - key]   " + key);
            logger.info("[split word entry from latest input - value] " + value);
        });

        // Materialization is delayed according to commit.interval.ms set in application config

        KTable<String, Long> wordCountTable = splitWordStream
                .groupBy((key, value) -> value)
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count-store"));

        KStream<String, Long> wordCounts = wordCountTable.toStream((key, value) -> {
            logger.info("[counted word entry - key]   " + key);
            logger.info("[counted word entry - value] " + value);
            return key;
        });

        wordCounts.to("kafka-stream-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }

    public static void main(String[] args) {

        final KafkaStreams streamsClient = new KafkaStreams(createTopology(), getStreamsConfig());

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // attach shutdown handler to catch Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread("kafka-stream-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streamsClient.close();
                countDownLatch.countDown();
            }
        });

        try {
            streamsClient.start();
            countDownLatch.await();
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}

