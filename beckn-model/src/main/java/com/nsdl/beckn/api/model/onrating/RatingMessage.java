package com.nsdl.beckn.api.model.onrating;

import java.util.List;

import com.nsdl.beckn.api.model.common.FeedbackForm;
import com.nsdl.beckn.api.model.common.FeedbackUrl;

import lombok.Data;

@Data
public class RatingMessage {
	private String ratingCategory;
	private String id;
	private Float value;
	private List<FeedbackForm> feedbackForm;
	private FeedbackUrl feedbackId;
}