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
@Table(name = "api_audit_error")
@Data
@TypeDef(name = "json", typeClass = JsonType.class)
public class ApiAuditErrorEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "schema_class")
	private String schemaClass;

	@Column(name = "error")
	private String error;

	@Type(type = "json")
	@Column(name = "json")
	private String json;

	@Column(name = "created_on")
	private LocalDateTime createdOn;
}
