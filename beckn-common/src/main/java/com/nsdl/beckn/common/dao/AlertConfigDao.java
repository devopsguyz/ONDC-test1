package com.nsdl.beckn.common.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsdl.beckn.common.entity.AlertConfigEntity;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AlertConfigDao {

	@Autowired
	private EntityManager em;

	public List<AlertConfigEntity> getAlertConfig() {
		List<AlertConfigEntity> resultList = this.em.createQuery("from AlertConfigEntity where enabled= true", AlertConfigEntity.class)
				.getResultList();

		log.info("size of AlertConfigEntity list is {}", resultList.size());
		return resultList;
	}

}
