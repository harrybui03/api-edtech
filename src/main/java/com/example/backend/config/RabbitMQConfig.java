package com.example.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "transcoding_exchange";
    public static final String QUEUE_NAME = "transcoding_queue";
    public static final String ROUTING_KEY = "video.transcoding.request";
    
    // Recording Merge Queue
    public static final String RECORDING_MERGE_QUEUE_NAME = "recording_merge_queue";
    public static final String RECORDING_MERGE_ROUTING_KEY = "recording.merge.request";

    // Dead Letter Queue (DLQ) configuration
    public static final String DLX_NAME = "transcoding_exchange_dlx";
    public static final String DLQ_NAME = "transcoding_queue_dlq";
    public static final String DLQ_ROUTING_KEY = "dlq.video.transcoding.request";
    public static final String RECORDING_MERGE_DLQ_ROUTING_KEY = "dlq.recording.merge.request";

    // Transcription routing (dùng chung exchange với transcode worker)
    public static final String TRANSCRIPTION_QUEUE_NAME = "transcription_queue";
    public static final String TRANSCRIPTION_DLX_NAME = "transcription_exchange_dlx";
    public static final String TRANSCRIPTION_DLQ_NAME = "transcription_queue_dlq";
    public static final String TRANSCRIPTION_DLQ_ROUTING_KEY = "dlq.video.transcoding.request";

    // Transcription routing
    public static final String TRANSCRIPTION_EXCHANGE = "transcription_exchange";
    public static final String TRANSCRIPTION_ROUTING_KEY = "audio.transcription.request";

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE_NAME); }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) { return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY); }
    
    @Bean
    public Queue recordingMergeQueue() {
        return QueueBuilder.durable(RECORDING_MERGE_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", RECORDING_MERGE_DLQ_ROUTING_KEY)
                .build();
    }
    
    @Bean
    public Binding recordingMergeBinding(Queue recordingMergeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(recordingMergeQueue).to(exchange).with(RECORDING_MERGE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange deadLetterExchange() { return new TopicExchange(DLX_NAME); }

    @Bean
    public Queue deadLetterQueue() { return new Queue(DLQ_NAME, true); }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    // Transcription Queue configuration
    @Bean
    public Queue transcriptionQueue() {
        return QueueBuilder.durable(TRANSCRIPTION_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", TRANSCRIPTION_DLX_NAME)
                .withArgument("x-dead-letter-routing-key", TRANSCRIPTION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding transcriptionBinding(Queue transcriptionQueue, TopicExchange exchange) {
        // Bind transcription_queue vào transcoding_exchange với routing key video.transcoding.request
        return BindingBuilder.bind(transcriptionQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public TopicExchange transcriptionDeadLetterExchange() {
        return new TopicExchange(TRANSCRIPTION_DLX_NAME);
    }

    @Bean
    public Queue transcriptionDeadLetterQueue() {
        return new Queue(TRANSCRIPTION_DLQ_NAME, true);
    }

    @Bean
    public Binding transcriptionDeadLetterBinding(Queue transcriptionDeadLetterQueue, TopicExchange transcriptionDeadLetterExchange) {
        return BindingBuilder.bind(transcriptionDeadLetterQueue)
                .to(transcriptionDeadLetterExchange)
                .with(TRANSCRIPTION_DLQ_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}