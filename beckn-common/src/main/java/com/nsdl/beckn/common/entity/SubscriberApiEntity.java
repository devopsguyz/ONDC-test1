package com.nsdl.beckn.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "ondc_subscriber_api")
@Data
public class SubscriberApiEntity {

	@EmbeddedId
	private SubscriberApiEntityPk pk = new SubscriberApiEntityPk();

	@Column(name = "endpoint")
	private String endpoint;

	@Column(name = "timeout")
	private short timeout;

	@Column(name = "retry_count")
	private short retryCount;

	@Column(name = "header_validity")
	private int headerValidity;

	@Column(name = "validate_auth_header")
	private boolean validateAuthHeader;

	@Column(name = "set_auth_header")
	private boolean setAuthHeader;

	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

	@Data
	@Embeddable
	public static class SubscriberApiEntityPk implements Serializable {

		private static final long serialVersionUID = 5803122732109076389L;

		@Column(name = "subscriber_id")
		private String subscriberId;

		@Column(name = "name")
		private String name;
	}

}
