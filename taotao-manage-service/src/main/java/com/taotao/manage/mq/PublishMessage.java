package com.taotao.manage.mq;

import org.springframework.amqp.core.MessageListener;

public interface PublishMessage {
	public MessageListener publishMessage();
}
