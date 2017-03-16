package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ModifiedCreatedByWidget implements ModifiedCreatedByWidgetView.Presenter {

	private ModifiedCreatedByWidgetView view;
	private UserBadge createdByBadge;
	private UserBadge modifiedByBadge;
	private SynapseJSNIUtils jsniUtils;
	@Inject
	public ModifiedCreatedByWidget(ModifiedCreatedByWidgetView view, UserBadge createdByBadge,
			UserBadge modifiedByBadge, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
		this.jsniUtils = jsniUtils;
		view.setCreatedBadge(createdByBadge);
		view.setModifiedBadge(modifiedByBadge);
	}
	
	public void configure(Date createdOn, String createdBy, Date modifiedOn, String modifiedBy) {
		createdByBadge.configure(createdBy);
		modifiedByBadge.configure(modifiedBy);
		view.setCreatedOnText(" on " + jsniUtils.getLongFriendlyDate(createdOn));
		view.setModifiedOnText(" on " + jsniUtils.getLongFriendlyDate(modifiedOn));
		view.setVisible(true);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
}
