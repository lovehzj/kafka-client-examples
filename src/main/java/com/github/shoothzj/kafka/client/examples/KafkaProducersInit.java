package com.github.shoothzj.kafka.client.examples;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author hezhangjian
 */
@Slf4j
public class KafkaProducersInit {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("kafka-producers-init").build();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, threadFactory);

    private CopyOnWriteArrayList<Producer<String, String>> producers;

    private int initIndex;

    private final List<String> topics;

    public KafkaProducersInit(List<String> topics) {
        this.topics = topics;
    }

    public void init() {
        executorService.scheduleWithFixedDelay(this::initWithRetry, 0, 10, TimeUnit.SECONDS);
    }

    private void initWithRetry() {
        if (initIndex == topics.size()) {
            executorService.shutdown();
            return;
        }
        for (; initIndex < topics.size(); initIndex++) {
            try {
                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstant.BOOTSTRAP_SERVERS);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.ACKS_CONFIG, "1");
                // 2MB
                props.put(ProducerConfig.BATCH_SIZE_CONFIG, "2097152");
                props.put(ProducerConfig.LINGER_MS_CONFIG, "20");
                props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
                props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "5000");
                props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "500");
                KafkaProducer<String, String> producer = new KafkaProducer<>(props);
                producers.add(producer);
            } catch (Exception e) {
                log.error("init kafka producer error, exception is ", e);
                break;
            }
        }
    }

    public Producer<String, String> getProducer(int idx) {
        return producers.get(idx);
    }

}
