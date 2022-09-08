package com.wf.upo.wires.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.upo.wires.domain.WireDetailsEvent;
import com.wf.upo.wires.producer.WireDetailsEventProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.io.IOException;
import java.io.InputStream;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class WireDetailsEventsController {

	@Autowired
	WireDetailsEventProducer eventProducer;

	@PostMapping("/v1/psrm/account")
	public ResponseEntity<WireDetailsEvent> postEvent(@RequestParam HashMap<String, Double> req, @RequestBody @Valid WireDetailsEvent event)
			throws JsonProcessingException, ExecutionException, InterruptedException {

		// invoke kafka producer
		eventProducer.sendEvent_Approach2(event);
		return ResponseEntity.status(HttpStatus.CREATED).body(event);
	}
	
	@PostMapping("/v1/startWireTransfers")
	public void sendWires() throws IOException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		InputStream is = WireDetailsEvent.class.getResourceAsStream("/static/wires.json");
		TypeReference<List<WireDetailsEvent>> mapType = new TypeReference<List<WireDetailsEvent>>() {
		};
		List<WireDetailsEvent> list = mapper.readValue(is, mapType);
		for (WireDetailsEvent event : list) {
			Thread.sleep(2000);
			eventProducer.sendEvent_Approach2(event);
//			System.out.println(event);
		}
	}

	// PUT
//	@PutMapping("/v1/libraryevent")
//	public ResponseEntity<?> putEvent(@RequestBody @Valid WireDetailsEvent event)
//			throws JsonProcessingException, ExecutionException, InterruptedException {
//
//		if (event.getEventId() == null) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the EventId");
//		}
//
//		event.setEventType(EventType.UPDATE);
//		eventProducer.sendEvent_Approach2(event);
//		return ResponseEntity.status(HttpStatus.OK).body(event);
//	}

//	public String getDtTm() {
//		DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//		LocalDateTime now = LocalDateTime.now();
//		String evtDtTm = dtf.format(now.withNano(0));
//		return evtDtTm;
//	}

}
