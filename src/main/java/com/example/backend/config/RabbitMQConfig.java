package com.example.backend.config;

import org.springframework.amqp.core.Binding;
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
    public static final String ROUTING_KEY = "transcoding.request";

    @Bean
    public Queue queue() { return new Queue(QUEUE_NAME, false); }

    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE_NAME); }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) { return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY); }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}