package com.nsdl.beckn.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BlackListModel implements Serializable {
	private static final long serialVersionUID = -7570666827957272636L;

	private List<String> buyer = new ArrayList<>();
	private List<String> seller = new ArrayList<>();
}
