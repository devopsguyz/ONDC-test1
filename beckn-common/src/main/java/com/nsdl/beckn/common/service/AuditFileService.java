package com.nsdl.beckn.common.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.common.model.AuditModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditFileService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

	@Value("${beckn.persistence.file-path}")
	private String filePath;

	@Async(value = "commonExecutor")
	public void fileAudit(AuditModel dataModel) {
		try {
			Context context = dataModel.getContext();
			String fileName = context.getAction().toLowerCase() + "_" + context.getMessageId() + "_" + System.currentTimeMillis();

			// byte[] strToBytes = this.mapper.writeValueAsString(dataModel).getBytes(); // writing
			// complete model as json part to file
			byte[] strToBytes = dataModel.getBody().getBytes(); // writing only the json part to
																// file

			LocalDate now = LocalDate.now();
			String folderName = now.format(FORMATTER).toUpperCase();
			StringBuilder sb = new StringBuilder();
			StringBuilder file = sb.append(this.filePath).append(folderName).append("/").append(fileName).append(".json");

			Path path = Paths.get(file.toString());

			Files.createDirectories(path.getParent());
			Files.createFile(path);
			Files.write(path, strToBytes);

			log.info("file create at path {}", file);
		} catch (IOException e) {
			log.error("file creating failed", e);
		}
	}

}
