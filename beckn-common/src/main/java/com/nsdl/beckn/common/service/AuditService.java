package com.nsdl.beckn.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.common.builder.ModelBuilder;
import com.nsdl.beckn.common.model.AuditModel;
import com.nsdl.beckn.common.model.BlipModel;
import com.nsdl.beckn.common.util.AdaptorUtil;
import com.nsdl.beckn.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Primary
public class AuditService {

	@Autowired
	private AdaptorUtil adaptorUtil;

	@Autowired
	private AuditDbService auditDbService;

	@Autowired
	private AuditFileService auditFileService;

	@Autowired
	private AuditHttpService auditHttpService;

	@Autowired
	private BlipService blipService;

	@Autowired
	private ModelBuilder modelBuilder;

	@Autowired
	private JsonUtil jsonUtil;

	@Value("${beckn.blip.enabled: false}")
	private boolean blipEnabled;

	public void audit(AuditModel auditModel) {
		log.info("going to do audit[{}] with flags {}", auditModel.getType(), auditModel.getAuditFlags());

		if (this.blipEnabled && auditModel.getAuditFlags().isBlip()) {
			log.info("blip audit service is enabled");
			BlipModel blipModel = this.modelBuilder.buildBlipModel(auditModel);
			this.blipService.blip(blipModel, auditModel.getType());
		} else {
			log.debug("blip audit service is disabled");
		}

		if (auditModel.getAuditFlags().isHttp() && this.adaptorUtil.isHttpPersistanceConfigured()) {
			AuditModel auditModelHttp = this.jsonUtil.toModel(this.jsonUtil.toJson(auditModel), AuditModel.class);
			this.auditHttpService.http(auditModelHttp);
		}

		// code for database audit
		boolean dbFlag = auditModel.getAuditFlags().isDatabase();
		if (dbFlag) {
			this.auditDbService.databaseAudit(dbFlag, auditModel, auditModel.getType(), auditModel.getEndTime(), auditModel.getCreatedOn());
		}

		// code for file audit
		fileAudit(auditModel, this.adaptorUtil.isFilePersistanceConfigured());

	}

	private void fileAudit(AuditModel auditModel, boolean isFileAudit) {
		if (auditModel.getAuditFlags().isFile() && isFileAudit) {
			this.auditFileService.fileAudit(auditModel);
		}
	}

}
