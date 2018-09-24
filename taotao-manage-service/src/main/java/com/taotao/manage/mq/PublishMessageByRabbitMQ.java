package com.taotao.manage.mq;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 该类专门用于发布消息。
 */
@Component
public class PublishMessageByRabbitMQ {
	
	@Autowired
	private static RabbitTemplate rabbitTemplate;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 发布消息。实现方式二：使用Rabbitm的通配符模式发布消息：
	 * @param routingKey：指明queue接收哪类的消息，同时传给消息消费者告知数据做了CRUD哪类操作。
	 * @param id：被操作数据的id.
	 */
	public static void sendMessage(String routingKey, Long id){
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("id", id);
			message.put("routingKey", routingKey);
			message.put("date", System.currentTimeMillis());
			rabbitTemplate.convertAndSend(routingKey, MAPPER.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
}
