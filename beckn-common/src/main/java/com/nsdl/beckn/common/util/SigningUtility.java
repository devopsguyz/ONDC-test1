package com.nsdl.beckn.common.util;

import static com.nsdl.beckn.common.exception.ErrorCode.HEADER_PARSING_FAILED;
import static com.nsdl.beckn.common.exception.ErrorCode.HEADER_SEQ_MISMATCH;
import static com.nsdl.beckn.common.exception.ErrorCode.INVALID_AUTH_HEADER;
import static com.nsdl.beckn.common.exception.ErrorCode.SIGNATURE_VERIFICATION_FAILED;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.stereotype.Component;

import com.nsdl.beckn.common.dto.KeyIdDto;
import com.nsdl.beckn.common.exception.ApplicationException;
import com.nsdl.beckn.common.exception.ErrorCode;
import com.nsdl.beckn.common.model.HeaderParams;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SigningUtility {

	public String generateSignature(String req, String privateKey) {
		String sign = null;
		try {

			Ed25519PrivateKeyParameters parameter = new Ed25519PrivateKeyParameters(Base64.getDecoder().decode(privateKey.getBytes()), 0);
			Ed25519Signer sig = new Ed25519Signer();
			sig.init(true, parameter);
			sig.update(req.getBytes(), 0, req.length());

			byte[] s1 = sig.generateSignature();
			sign = Base64.getEncoder().encodeToString(s1);

		} catch (Exception e) {
			log.error("error while generating the signature", e);
			throw new ApplicationException(ErrorCode.SIGNATURE_ERROR, ErrorCode.SIGNATURE_ERROR.getMessage());
		}

		log.info("signature generated is: {}", sign);
		return sign;
	}

	public boolean verifySignature(String signature, String requestData, String publicKey, String lookupKey) throws ApplicationException {
		boolean isVerified = false;

		try {
			Ed25519PublicKeyParameters publicKeyParams = new Ed25519PublicKeyParameters(Base64.getDecoder().decode(publicKey), 0);
			Ed25519Signer sv = new Ed25519Signer();
			sv.init(false, publicKeyParams);
			sv.update(requestData.getBytes(), 0, requestData.length());

			byte[] decodedSign = Base64.getDecoder().decode(signature);
			isVerified = sv.verifySignature(decodedSign);
			log.info("Is signature verified for key[{}] ? {}", lookupKey, isVerified);
		} catch (Exception e) {
			log.error("error while validating signature {}", e);
			// throw new ApplicationException(e);
			throw new ApplicationException(SIGNATURE_VERIFICATION_FAILED, SIGNATURE_VERIFICATION_FAILED.getMessage());
		}
		return isVerified;
	}

	public boolean verifyWithP12PublicKey(String signature, String requestData, String publicKey) throws ApplicationException {
		boolean isVerified = false;
		try {
			log.info("Verifying with public key from p12 certificate");

			byte[] decryptPubKey = Base64.getDecoder().decode(publicKey);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decryptPubKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFactory.generatePublic(keySpec);
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(pubKey);
			sig.update(requestData.getBytes());
			byte[] decodedSign = Base64.getDecoder().decode(signature);
			isVerified = sig.verify(decodedSign);
			log.info("Is signature verified using p12 certificate ? {}", isVerified);
		} catch (Exception e) {
			log.info("exception while verifing p12 certificate signature:", e);
			// throw new ApplicationException(e);
			throw new ApplicationException(SIGNATURE_VERIFICATION_FAILED, SIGNATURE_VERIFICATION_FAILED.getMessage());
		}
		return isVerified;
	}

	public Map<String, String> parseAuthorizationHeader(String authHeader) {
		Map<String, String> holder = new HashMap<>();
		if (authHeader.contains("Signature ")) {
			authHeader = authHeader.replace("Signature ", "");
			String[] keyVals = authHeader.split(",");
			for (String keyVal : keyVals) {
				String[] parts = keyVal.split("=", 2);
				if (parts[0] != null && parts[1] != null) {
					holder.put(parts[0].trim(), parts[1].trim());
				}
			}
			return holder;
		}
		return null;
	}

	public KeyIdDto splitKeyId(String kid) throws ApplicationException {
		KeyIdDto keyIdDto = null;
		try {
			if (kid != null && !kid.isEmpty()) {
				kid = kid.replace("\"", "");
				keyIdDto = new KeyIdDto();

				String[] arry = kid.split("[|]");
				keyIdDto.setSubscriberId(arry[0]);
				keyIdDto.setUniqueKeyId(arry[1]);
				keyIdDto.setAlgo(arry[2]);
			}
		} catch (Exception e) {
			log.error("error in splitKeyId", e);
			throw new ApplicationException(INVALID_AUTH_HEADER, INVALID_AUTH_HEADER.getMessage());
		}
		return keyIdDto;
	}

	public HeaderParams splitHeadersParam(String headers) throws ApplicationException {
		HeaderParams headerParams = null;
		try {
			if (headers != null && !headers.isEmpty()) {
				headers = headers.replace("\"", "");
				headerParams = new HeaderParams();

				String[] a = headers.split(" ");
				if (a == null || a.length <= 2) {
					log.error("Invalid Header");
					throw new ApplicationException(INVALID_AUTH_HEADER, INVALID_AUTH_HEADER.getMessage());
				}
				headerParams.setCreated(a[0].replace("(", "").replace(")", ""));
				headerParams.setExpires(a[1].replace("(", "").replace(")", ""));
				headerParams.setDiagest(a[2].trim());
				if (headerParams.getCreated() == null || !"created".equalsIgnoreCase(headerParams.getCreated()) || headerParams.getExpires() == null
						|| !"expires".equalsIgnoreCase(headerParams.getExpires()) || headerParams.getDiagest() == null
						|| !"digest".equalsIgnoreCase(headerParams.getDiagest())) {
					log.error("Header sequense mismatch");
					throw new ApplicationException(HEADER_SEQ_MISMATCH, HEADER_SEQ_MISMATCH.getMessage());
				}

			}
		} catch (Exception e) {
			log.error("Header parsing Failed");
			throw new ApplicationException(HEADER_PARSING_FAILED, HEADER_PARSING_FAILED.getMessage());
		}
		return headerParams;
	}

	public String generateBlakeHash(String req) {
		Blake2bDigest blake2bDigest = new Blake2bDigest(512);
		byte[] test = req.getBytes();
		blake2bDigest.update(test, 0, test.length);
		byte[] hash = new byte[blake2bDigest.getDigestSize()];
		blake2bDigest.doFinal(hash, 0);
		String bs64 = Base64.getEncoder().encodeToString(hash);
		// log.info("Base64 URL Encoded : " + bs64);
		return bs64;
	}

	public boolean validateTime(String crt, String exp) {

		boolean isValid = false;

		if (crt != null && exp != null) {
			crt = crt.replace("\"", "");
			exp = exp.replace("\"", "");
			long created = Long.parseLong(crt);
			long expiry = Long.parseLong(exp);
			long now = System.currentTimeMillis() / 1000L;
			long diffInSec = expiry - created;

			if (diffInSec > 0 && created <= now && expiry > now && expiry >= created) {
				isValid = true;
			}
		} else {
			log.error("created or expires timestamp value is null.");
		}

		log.debug("Is request valid with respect to sign header timestamp? {}", isValid);
		return isValid;
	}

}
