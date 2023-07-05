package com.nsdl.beckn.adaptor;

import com.nsdl.beckn.api.model.issue.IssueRequest;
import com.nsdl.beckn.api.model.issuestatus.IssueStatusRequest;
import com.nsdl.beckn.api.model.onissue.OnIssueRequest;
import com.nsdl.beckn.api.model.onissuestatus.OnIssueStatusRequest;
import com.nsdl.beckn.api.model.prepareforsettle.PrepareForSettleRequest;
import com.nsdl.beckn.api.model.collector_recon.CollectorReconRequest;
import com.nsdl.beckn.api.model.recon_status.ReconStatusRequest;
import com.nsdl.beckn.api.model.Reciever_recon.RecieverReconRequest;
import com.nsdl.beckn.api.model.OnCollectorRecon.OnCollectorReconRequest;
import com.nsdl.beckn.api.model.on_reconStatus.OnReconStatusRequest;
import com.nsdl.beckn.api.model.OnReciever_recon.OnRecieverReconRequest;

import org.springframework.stereotype.Service;

import com.nsdl.beckn.api.model.cancel.CancelRequest;
import com.nsdl.beckn.api.model.common.Context;
import com.nsdl.beckn.api.model.confirm.ConfirmRequest;
import com.nsdl.beckn.api.model.init.InitRequest;
import com.nsdl.beckn.api.model.oncancel.OnCancelRequest;
import com.nsdl.beckn.api.model.onconfirm.OnConfirmRequest;
import com.nsdl.beckn.api.model.oninit.OnInitRequest;
import com.nsdl.beckn.api.model.onrating.RatingRequest;
import com.nsdl.beckn.api.model.onrecon.OnReconRequest;
import com.nsdl.beckn.api.model.onsearch.OnSearchRequest;
import com.nsdl.beckn.api.model.onselect.OnSelectRequest;
import com.nsdl.beckn.api.model.onstatus.OnStatusRequest;
import com.nsdl.beckn.api.model.onsupport.OnSupportRequest;
import com.nsdl.beckn.api.model.ontrack.OnTrackRequest;
import com.nsdl.beckn.api.model.onupdate.OnUpdateRequest;
import com.nsdl.beckn.api.model.rating.OnRatingRequest;
import com.nsdl.beckn.api.model.search.SearchRequest;
import com.nsdl.beckn.api.model.select.SelectRequest;
import com.nsdl.beckn.api.model.status.StatusRequest;
import com.nsdl.beckn.api.model.support.SupportRequest;
import com.nsdl.beckn.api.model.track.TrackRequest;
import com.nsdl.beckn.api.model.update.UpdateRequest;

@Service
public class ApiClassFinder {

	public Class<?> findClass(Context ctx) {
		if ("search".equalsIgnoreCase(ctx.getAction())) {
			return SearchRequest.class;
		}

		if ("select".equalsIgnoreCase(ctx.getAction())) {
			return SelectRequest.class;
		}

		if ("init".equalsIgnoreCase(ctx.getAction())) {
			return InitRequest.class;
		}

		if ("confirm".equalsIgnoreCase(ctx.getAction())) {
			return ConfirmRequest.class;
		}

		if ("status".equalsIgnoreCase(ctx.getAction())) {
			return StatusRequest.class;
		}

		if ("track".equalsIgnoreCase(ctx.getAction())) {
			return TrackRequest.class;
		}

		if ("cancel".equalsIgnoreCase(ctx.getAction())) {
			return CancelRequest.class;
		}

		if ("update".equalsIgnoreCase(ctx.getAction())) {
			return UpdateRequest.class;
		}

		if ("rating".equalsIgnoreCase(ctx.getAction())) {
			return RatingRequest.class;
		}

		if ("support".equalsIgnoreCase(ctx.getAction())) {
			return SupportRequest.class;
		}
		if ("prepareforsettle".equalsIgnoreCase(ctx.getAction())) {
			return PrepareForSettleRequest.class;
		}

		if ("collector_recon".equalsIgnoreCase(ctx.getAction())) {
			return CollectorReconRequest.class;
		}

		if ("recon_status".equalsIgnoreCase(ctx.getAction())) {
			return ReconStatusRequest.class;
		}

		if ("receiver_recon".equalsIgnoreCase(ctx.getAction())) {
			return RecieverReconRequest.class;
		}

		if ("issue".equalsIgnoreCase(ctx.getAction())) {
			return IssueRequest.class;
		}

		if ("issue_status".equalsIgnoreCase(ctx.getAction())) {
			return IssueStatusRequest.class;
		}

		if ("on_search".equalsIgnoreCase(ctx.getAction())) {
			return OnSearchRequest.class;
		}

		if ("on_select".equalsIgnoreCase(ctx.getAction())) {
			return OnSelectRequest.class;
		}

		if ("on_init".equalsIgnoreCase(ctx.getAction())) {
			return OnInitRequest.class;
		}

		if ("on_confirm".equalsIgnoreCase(ctx.getAction())) {
			return OnConfirmRequest.class;
		}

		if ("on_status".equalsIgnoreCase(ctx.getAction())) {
			return OnStatusRequest.class;
		}

		if ("on_track".equalsIgnoreCase(ctx.getAction())) {
			return OnTrackRequest.class;
		}

		if ("on_cancel".equalsIgnoreCase(ctx.getAction())) {
			return OnCancelRequest.class;
		}

		if ("on_update".equalsIgnoreCase(ctx.getAction())) {
			return OnUpdateRequest.class;
		}

		if ("on_rating".equalsIgnoreCase(ctx.getAction())) {
			return OnRatingRequest.class;
		}

		if ("on_support".equalsIgnoreCase(ctx.getAction())) {
			return OnSupportRequest.class;
		}

		if ("on_recon".equalsIgnoreCase(ctx.getAction())) {
			return OnReconRequest.class;
		}
		if ("on_collector_recon".equalsIgnoreCase(ctx.getAction())) {
			return OnCollectorReconRequest.class;
		}

		if ("on_recon_status".equalsIgnoreCase(ctx.getAction())) {
			return OnReconStatusRequest.class;
		}

		if ("on_receiver_recon".equalsIgnoreCase(ctx.getAction())) {
			return OnRecieverReconRequest.class;
		}

		if ("on_issue".equalsIgnoreCase(ctx.getAction())) {
			return OnIssueRequest.class;
		}

		if ("on_issue_status".equalsIgnoreCase(ctx.getAction())) {
			return OnIssueStatusRequest.class;
		}

		return Object.class;
	}

}
