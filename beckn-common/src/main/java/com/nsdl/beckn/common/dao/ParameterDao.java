package com.nsdl.beckn.common.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.nsdl.beckn.common.entity.ParameterEntity;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ParameterDao {

	@PersistenceContext
	private EntityManager em;

	public List<ParameterEntity> getParameterList() {
		log.info("going to load all parameters");
		return this.em.createQuery("from ParameterEntity", ParameterEntity.class).getResultList();
	}

}
