package com.nsdl.beckn.common.dao;

import static com.nsdl.beckn.common.constant.ApplicationConstant.SPACE;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_BY_GATEWAY;
import static com.nsdl.beckn.common.enums.AuditType.RESPONSE_BY_SELLER;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nsdl.beckn.common.entity.ApiAuditEntity;
import com.nsdl.beckn.common.entity.ApiAuditErrorEntity;
import com.nsdl.beckn.common.model.JsonRequestModel;
import com.nsdl.beckn.common.service.CommonService;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AuditDao {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private CommonService commonService;

	public void saveApiAudit(ApiAuditEntity entity) {
		this.em.persist(entity);
	}

	public void saveApiAuditError(ApiAuditErrorEntity entity) {
		this.em.persist(entity);
	}

	public void saveSchemaError(ApiAuditErrorEntity entity) {
		this.em.persist(entity);

	}

	public List<Object[]> getDataByTxId(JsonRequestModel model) {
		String enumType = RESPONSE_BY_SELLER.name();

		if ("on_search".equalsIgnoreCase(model.getAction())) {
			enumType = RESPONSE_BY_GATEWAY.name();
		}

		List<String> sellers = model.getSellers();

		if (CollectionUtils.isEmpty(sellers)) {
			return this.em.createQuery("select json, createdOn from ApiAuditEntity where action= :action and transactionId= :txId"
					+ " and type= :enumType", Object[].class)
					.setParameter("action", model.getAction())
					.setParameter("txId", model.getTransactionId())
					.setParameter("enumType", enumType)
					.getResultList();
		}

		return this.em.createQuery("select json, createdOn from ApiAuditEntity where action= :action and transactionId= :txId"
				+ " and type= :enumType and sellerId in :sellers", Object[].class)
				.setParameter("action", model.getAction())
				.setParameter("txId", model.getTransactionId())
				.setParameter("enumType", enumType)
				.setParameter("sellers", sellers)
				.getResultList();

	}

	public List<Object[]> getDataByTxIdAndMessageId(JsonRequestModel model) {
		String enumType = RESPONSE_BY_SELLER.name();

		if ("on_search".equalsIgnoreCase(model.getAction())) {
			enumType = RESPONSE_BY_GATEWAY.name();
		}

		List<String> sellers = model.getSellers();

		if (CollectionUtils.isEmpty(sellers)) {
			return this.em.createQuery("select json, createdOn from ApiAuditEntity where action= :action and transactionId= :txId and messageId= :msgId"
					+ " and type= :enumType", Object[].class)
					.setParameter("action", model.getAction())
					.setParameter("txId", model.getTransactionId())
					.setParameter("msgId", model.getMessageId())
					.setParameter("enumType", enumType)
					.getResultList();
		}

		return this.em.createQuery("select json, createdOn from ApiAuditEntity where action= :action and transactionId= :txId and messageId= :msgId"
				+ " and type= :enumType and sellerId in :sellers", Object[].class)
				.setParameter("action", model.getAction())
				.setParameter("txId", model.getTransactionId())
				.setParameter("msgId", model.getMessageId())
				.setParameter("enumType", enumType)
				.setParameter("sellers", sellers)
				.getResultList();

	}

	//
	public List<Object[]> getAuditData(JsonRequestModel model) {

		Instant start = Instant.now();

		StringBuilder query = new StringBuilder(
				"select json, createdOn from ApiAuditEntity where action = :action and transactionId = :txId and type = :enumType");

		String enumType = RESPONSE_BY_SELLER.name();

		if ("on_search".equalsIgnoreCase(model.getAction())) {
			enumType = RESPONSE_BY_GATEWAY.name();
		}

		List<String> sellers = model.getSellers();
		int[] range = getRecordRange(model.getRecordRange());

		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("action", model.getAction());
		parameterMap.put("txId", model.getTransactionId());
		parameterMap.put("enumType", enumType);

		if (isNotBlank(model.getMessageId())) {
			query.append(SPACE).append("and messageId = :msgId");
			parameterMap.put("msgId", model.getMessageId());
		}

		if (isNotEmpty(sellers)) {
			query.append(SPACE).append("and sellerId in :sellers");
			parameterMap.put("sellers", sellers);
		}

		query.append(SPACE).append("order by createdOn");

		log.info("query to execute is {}", query.toString());

		TypedQuery<Object[]> typedQuery = this.em.createQuery(query.toString(), Object[].class);

		if (range != null && range.length == 2) {
			typedQuery.setFirstResult(range[0]);
			typedQuery.setMaxResults(range[1]);
			log.info("pagination applied for the range {}", range);
		}

		for (Entry<String, Object> entry : parameterMap.entrySet()) {
			typedQuery.setParameter(entry.getKey(), entry.getValue());
		}

		List<Object[]> list = typedQuery.getResultList();

		long timer = this.commonService.getTimeDiff(start, Instant.now());
		log.info("query with result list size[{}] executed in {} ms", list.size(), timer);

		return list;

	}

	//
	public List<Object[]> getAuditData1(JsonRequestModel model) {

		Instant start = Instant.now();

		String enumType = RESPONSE_BY_SELLER.name();

		if ("on_search".equalsIgnoreCase(model.getAction())) {
			enumType = RESPONSE_BY_GATEWAY.name();
		}

		StringBuilder query = new StringBuilder("select json, createdOn from ApiAuditEntity where");
		query.append(SPACE).append("action = '" + model.getAction() + "'");
		query.append(SPACE).append("and transactionId = '" + model.getTransactionId() + "'");
		query.append(SPACE).append("and type = '" + enumType + "'");

		List<String> sellers = model.getSellers();
		int[] range = getRecordRange(model.getRecordRange());

		if (isNotBlank(model.getMessageId())) {
			query.append(SPACE).append("and messageId = '" + model.getMessageId() + "'");
		}

		if (isNotEmpty(sellers)) {
			StringBuilder sellersSb = new StringBuilder("(");

			int size = sellers.size();
			int seq = 1;
			for (String sellerId : sellers) {
				sellersSb.append("'").append(sellerId).append("'");

				if (seq != size) {
					sellersSb.append(",");
				}

				seq++;
			}
			sellersSb.append(")");

			log.info("sellers sb {}", sellersSb.toString());
			query.append(SPACE).append("and sellerId in " + sellersSb);
		}

		query.append(SPACE).append("order by createdOn");

		log.info("query to execute is {}", query.toString());

		TypedQuery<Object[]> typedQuery = this.em.createQuery(query.toString(), Object[].class);
		if (range != null && range.length == 2) {
			typedQuery.setFirstResult(range[0]);
			typedQuery.setMaxResults(range[1]);

			log.info("pagination applied for the range {}", range);
		}

		List<Object[]> list = typedQuery.getResultList();

		long timer = this.commonService.getTimeDiff(start, Instant.now());
		log.info("query with result list size[{}] executed in {} ms", list.size(), timer);

		return list;

	}

	public int[] getRecordRange(String recordRange) {

		if (isNotBlank(recordRange)) {
			try {
				int[] recordRangeArray = new int[2];
				String[] range = recordRange.split("-");
				if (range.length != 2) {
					String error = "invalid record range " + recordRange;
					log.error(error);
					throw new RuntimeException(error);
				}

				int start = Integer.parseInt(range[0]);
				int end = Integer.parseInt(range[1]);
				recordRangeArray[0] = start;
				recordRangeArray[1] = end;

				return recordRangeArray;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public BigInteger health() {
		BigInteger result = (BigInteger) this.em.createNativeQuery("SELECT count(*) FROM information_schema.tables")
				.getSingleResult();

		return result;
	}

}
