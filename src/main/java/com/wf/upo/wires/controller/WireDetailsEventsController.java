package com.wf.upo.wires.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wf.upo.wires.domain.WireDetailsEvent;
import com.wf.upo.wires.producer.WireDetailsEventProducer;

@Slf4j
@RestController
public class WireDetailsEventsController {

	@Autowired
	WireDetailsEventProducer eventProducer;

	String [] pmtRails = {"OBL","CEO","XCCY","RTL"};
	String [] banks = {"Bank of America","CITI","JPMorgan Chase","US Bankcorp"};

	@PostMapping("/v1/psrm/account")
	public ResponseEntity<WireDetailsEvent> postEvent(@RequestParam HashMap<String, Double> req,
			@RequestBody @Valid WireDetailsEvent event)
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
		int counter=0;
		for (WireDetailsEvent event : list) {
			event.setEvtDtTm(getDtTm());
			//event.setPmtRail(pmtRails[counter%4]);
			eventProducer.sendEvent_Approach2(event);
			Thread.sleep(2000);
			counter++;
		}
	}

	@PostMapping("/v1/startBulkWireTransfers")
	public void sendWiresInBulk(@RequestParam int durationInMinutes, @RequestParam int delayInMs, @RequestParam String transactionType) throws IOException, InterruptedException {
		WireDetailsEvent event;

		for (int i=0 ; i<durationInMinutes*60; i++) {
			double randomAmount = round(ThreadLocalRandom.current().nextDouble(100000, 999999),2);
			if(transactionType.equalsIgnoreCase("credit"))
				event=WireDetailsEvent.builder().amt(randomAmount).ccy("USD").pmtRail(pmtRails[i%4]).nm(banks[i%4]).payeeiswells("Y").payoriswells("N").evtDtTm(getDtTm()).build();
			else if(transactionType.equalsIgnoreCase("debit"))
				event=WireDetailsEvent.builder().amt(randomAmount).ccy("USD").pmtRail(pmtRails[i%4]).nm(banks[i%4]).payeeiswells("N").payoriswells("Y").evtDtTm(getDtTm()).build();
			else {
				if (i % 2 == 0)
					event = WireDetailsEvent.builder().amt(randomAmount).ccy("USD").pmtRail(pmtRails[i % 4]).nm(banks[i % 4]).payeeiswells("Y").payoriswells("N").evtDtTm(getDtTm()).build();
				else
					event = WireDetailsEvent.builder().amt(randomAmount).ccy("USD").pmtRail(pmtRails[i % 4]).nm(banks[i % 4]).payeeiswells("N").payoriswells("Y").evtDtTm(getDtTm()).build();
			}
			Thread.sleep(delayInMs);
			eventProducer.sendEvent_Approach2(event);
		}

	}
	private static double round (double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private String getDtTm() {
		DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		LocalDateTime now = LocalDateTime.now();
		String evtDtTm = dtf.format(now.withNano(0));
		return evtDtTm;
	}

}
