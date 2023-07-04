package com.nsdl.beckn.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "alert_config")
@Data
public class AlertConfigEntity {

	@Id
	@Column(name = "pattern")
	private String pattern;

	@Column(name = "priority")
	private String priority;

	@Column(name = "enabled")
	private boolean enabled;

}
