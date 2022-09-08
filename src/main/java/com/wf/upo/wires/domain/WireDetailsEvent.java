package com.wf.upo.wires.domain;

//import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WireDetailsEvent {

	private String appId;

	private String pmtRail;

	private String payoriswells;

	private String payeeiswells;

	private Double amt;

	private String ccy;

	private String nm;

	private String evtDtTm;
	
//	@DecimalMin(value = "0.1", inclusive = true)
//	private Double initialBalance;

}
