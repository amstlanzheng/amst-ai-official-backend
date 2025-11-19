package com.amst.api.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * @author lanzhs
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // 队列常量
    public static final String ORDER_QUEUE = "order.queue";
    
    public static final String ORDER_EXCHANGE = "order.exchange";

    public static final String ORDER_ROUTING_KEY = "order.routing.key";

    /**
     * 订单队列
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true, false, false);
    }


    /**
     * 直连交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }


    /**
     * 绑定订单队列到交换机
     */
    @Bean
    public Binding bindingOrderQueue() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }


    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        
        // 设置确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            }
        });
        
        // 设置返回回调
        template.setReturnsCallback(returned -> {
            log.error("消息路由失败: {}", returned);
        });
        
        return template;
    }
}