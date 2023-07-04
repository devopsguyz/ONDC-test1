package com.nsdl.beckn.common.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "ondc_subscriber")
@Data
public class SubscriberEntity {

	@Id
	@Column(name = "subscriber_id")
	private String subscriberId;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "unique_key_id")
	private String uniqueKeyId;

	@Column(name = "private_key")
	private String privateKey;

	@Column(name = "msn")
	private boolean msn;

	@Column(name = "active")
	private boolean active;

	@Column(name = "subscriber_ip")
	private String subscriberIp;

	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

}
