
package com.nsdl.beckn.common.model;
import java.io.Serializable;

import lombok.Data;
@Data
public class SigningModel implements Serializable {
	private String subscriberId;
	private String uniqueKeyId;
	private String privateKey;

}
