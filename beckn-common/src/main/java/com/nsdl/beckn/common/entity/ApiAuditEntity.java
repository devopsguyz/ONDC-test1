package com.nsdl.beckn.common.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.Data;

@Entity
@Table(name = "api_audit")
@Data
@TypeDef(name = "json", typeClass = JsonType.class)
public class ApiAuditEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "remote_host")
	private String remoteHost;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "transaction_id")
	private String transactionId;

	@Column(name = "buyer_id")
	private String buyerId;

	@Column(name = "seller_id")
	private String sellerId;

	@Column(name = "action")
	private String action;

	@Column(name = "domain")
	private String domain;

	@Column(name = "city")
	private String city;

	@Column(name = "core_version")
	private String coreVersion;

	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Type(type = "json")
	@Column(name = "json")
	private String json;

	@Column(name = "headers")
	private String headers;

	@Column(name = "status")
	private String status;

	@Column(name = "type")
	private String type;

	@Column(name = "error")
	private String errorStackTrace;

	@Column(name = "error_technical")
	private String errorTechnical;

	@Column(name = "error_functional")
	private String errorFunctional;

	@Column(name = "time_taken")
	private String timeTaken;

	@Column(name = "host_id")
	private String hostId;
}
