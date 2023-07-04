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
@Table(name = "ondc_seller_on_record")
@Data
public class SellerOnRecordEntity {

	@EmbeddedId
	private SellerOnRecordEntityPk pk = new SellerOnRecordEntityPk();

	@Column(name = "provider_id")
	private String providerId;

	@Column(name = "private_key")
	private String privateKey;

	@Column(name = "active")
	private boolean active;

	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

	@Data
	@Embeddable
	public static class SellerOnRecordEntityPk implements Serializable {

		private static final long serialVersionUID = 6836810290914545853L;

		@Column(name = "subscriber_id")
		private String subscriberId;

		@Column(name = "unique_key_id")
		private String uniqueKeyId;
	}

}
