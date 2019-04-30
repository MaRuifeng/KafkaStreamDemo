package com.flyer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

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
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-stream-wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("kafka-stream-wordcount-input");

        final KTable<String, Long> counts = source
                .flatMapValues(v -> Arrays.asList(v.toLowerCase(Locale.getDefault()).split(" ")))
                .groupBy((k, v) -> v)
                .count();

        counts.toStream().to("kafka-stream-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // attach shutdown handler to catch Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread("kafka-stream-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                countDownLatch.countDown();
            }
        });

        try {
            streams.start();
            countDownLatch.await();
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}

