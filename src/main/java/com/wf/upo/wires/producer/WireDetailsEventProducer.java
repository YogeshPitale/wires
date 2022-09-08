package com.wf.upo.wires.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.upo.wires.domain.WireDetailsEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class WireDetailsEventProducer {

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	String topic = "events";
	@Autowired
	ObjectMapper objectMapper;

	public void sendEvent(WireDetailsEvent event) throws JsonProcessingException {

		String key = event.getAppId() + event.getPmtRail();
		String value = objectMapper.writeValueAsString(event);

		ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

			@Override
			public void onSuccess(SendResult<String, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}

	public ListenableFuture<SendResult<String, String>> sendEvent_Approach2(WireDetailsEvent event)
			throws JsonProcessingException {

		String key = event.getAppId() + event.getPmtRail();
		String value = objectMapper.writeValueAsString(event);

		ProducerRecord<String, String> producerRecord = buildProducerRecord(key, value, topic);

		ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(producerRecord);

		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

			@Override
			public void onSuccess(SendResult<String, String> result) {
				handleSuccess(key, value, result);
			}
		});

		return listenableFuture;
	}

	private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic) {

		List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

		return new ProducerRecord<>(topic, null, key, value, recordHeaders);
	}

	public SendResult<String, String> sendEventSynchronous(WireDetailsEvent event)
			throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

		String key = event.getAppId() + event.getPmtRail();
		String value = objectMapper.writeValueAsString(event);
		SendResult<String, String> sendResult = null;
		try {
			sendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
		} catch (ExecutionException | InterruptedException e) {
			log.error("ExecutionException/InterruptedException Sending the Message and the exception is {}",
					e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Exception Sending the Message and the exception is {}", e.getMessage());
			throw e;
		}

		return sendResult;

	}

	private void handleFailure(String key, String value, Throwable ex) {
		log.error("Error Sending the Message and the exception is {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable throwable) {
			log.error("Error in OnFailure: {}", throwable.getMessage());
		}

	}

	private void handleSuccess(String key, String value, SendResult<String, String> result) {
		log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value,
				result.getRecordMetadata().partition());
	}
}
