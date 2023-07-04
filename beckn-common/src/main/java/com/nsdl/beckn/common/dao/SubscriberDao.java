package com.nsdl.beckn.common.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.nsdl.beckn.common.entity.SellerOnRecordEntity;
import com.nsdl.beckn.common.entity.SubscriberApiEntity;
import com.nsdl.beckn.common.entity.SubscriberEntity;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class SubscriberDao {

	@PersistenceContext
	private EntityManager em;

	public List<SubscriberEntity> getSubscriberList() {
		List<SubscriberEntity> list = this.em.createQuery("from SubscriberEntity where active = true", SubscriberEntity.class)
				.getResultList();
		log.info("size of ondc_subscriber list {}", list.size());
		return list;
	}

	public List<SubscriberApiEntity> getSubscriberApiList() {
		List<SubscriberApiEntity> list = this.em.createQuery("from SubscriberApiEntity", SubscriberApiEntity.class)
				.getResultList();
		log.info("size of ondc_subscriber_api list {}", list.size());
		return list;
	}

	public List<SellerOnRecordEntity> getSellerOnRecordEntityList() {
		List<SellerOnRecordEntity> list = this.em.createQuery("from SellerOnRecordEntity where active = true", SellerOnRecordEntity.class)
				.getResultList();
		log.info("size of ondc_seller_on_record list {}", list.size());
		return list;
	}

}
