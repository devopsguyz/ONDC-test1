package com.nsdl.beckn.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "ondc_parameter")
@Data
public class ParameterEntity {

	@Id
	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

}
