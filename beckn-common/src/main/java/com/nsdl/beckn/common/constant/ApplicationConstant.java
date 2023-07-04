package com.nsdl.beckn.common.constant;

import static com.nsdl.beckn.api.enums.ContextAction.ON_SEARCH;
import static com.nsdl.beckn.api.enums.ContextAction.SEARCH;

import java.util.Arrays;
import java.util.List;

public final class ApplicationConstant {

	private ApplicationConstant() {

	}

	public static final String BLANK = "";
	public static final String SPACE = " ";
	public static final String PIPE = "|";
	public static final String DASH = "-";
	public static final String SLASH = "/";
	public static final String STAR = "*";
	public static final String BECKN_API_COMMON_CACHE = "beckn-api-cache-common";
	public static final String BECKN_API_LOOKUP_CACHE = "beckn-api-cache-lookup";
	public static final String BECKN_API_BLACKLIST_CACHE = "beckn-api-cache-blacklist";
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String REMOTE_HOST = "remoteHost";
	public static final String PROVIDER_ID = "ondc_provider_id";
	// public static final String ALL_REQUEST_HEADERS = "allRequestHeaders";
	public static final String SIGN_ALGORITHM = "ed25519";
	public static final String DB_POSTGRES = "db-postgres";
	public static final String FILE = "file";
	public static final String HTTP = "http";
	public static final String ON = "on_";
	public static final List<String> ALLOWED_PERSISTENCE_TYPES = Arrays.asList(HTTP, DB_POSTGRES);
	public static final List<String> GATEWAY_ACTIONS = Arrays.asList(SEARCH.value(), ON_SEARCH.value());
	public static final String ACTIVE_SELLERS = "ACTIVE_SELLERS";
	public static final String BLOCKED_BUYERS = "BLOCKED_BUYERS";
	public static final String BLOCKED_SELLERS = "BLOCKED_SELLERS";
	public static final String ON_COLLECTOR_RECON = "on_collector_recon";
	public static final String COLLECTOR_RECON = "collector_recon";
	public static final String ON_RECON_STATUS = "on_recon_status";
	public static final String RECEIVER_RECON = "receiver_recon";
	public static final String ON_RECEIVER_RECON = "on_receiver_recon";
	public static final String ON_CONFIRM = "on_confirm";
	public static final String ON_STATUS = "on_status";
	public static final String PREPARE_FOR_SETTLE = "prepareforsettle";
	public static final String RECON_STATUS = "recon_status";
}
