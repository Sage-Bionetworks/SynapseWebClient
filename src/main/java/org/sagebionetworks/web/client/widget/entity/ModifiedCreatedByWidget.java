package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ModifiedCreatedByWidget implements IsWidget {

	private ModifiedCreatedByWidgetView view;
	private UserBadge createdByBadge;
	private UserBadge modifiedByBadge;
	private DateTimeUtils dateTimeUtils;

	@Inject
	public ModifiedCreatedByWidget(ModifiedCreatedByWidgetView view, UserBadge createdByBadge, UserBadge modifiedByBadge, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.createdByBadge = createdByBadge;
		this.modifiedByBadge = modifiedByBadge;
		this.dateTimeUtils = dateTimeUtils;
		view.setCreatedBadge(createdByBadge);
		view.setModifiedBadge(modifiedByBadge);
	}

	public void configure(Date createdOn, String createdBy, Date modifiedOn, String modifiedBy) {
		createdByBadge.configure(createdBy);
		modifiedByBadge.configure(modifiedBy);
		view.setCreatedOnText(" on " + dateTimeUtils.getLongFriendlyDate(createdOn));
		view.setModifiedOnText(" on " + dateTimeUtils.getLongFriendlyDate(modifiedOn));
		view.setVisible(true);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setCreatedByUIVisible(boolean visible) {
		view.setCreatedByUIVisible(visible);
	}

	public void setModifiedByUIVisible(boolean visible) {
		view.setModifiedByUIVisible(visible);
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
}
