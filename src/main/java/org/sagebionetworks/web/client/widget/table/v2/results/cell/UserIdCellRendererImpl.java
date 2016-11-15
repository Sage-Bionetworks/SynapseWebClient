package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserIdCellRendererImpl implements UserIdCellRenderer{

	UserBadge userBadge;
	String principalId;
	
	@Inject
	public UserIdCellRendererImpl(UserBadge userBadge) {
		this.userBadge = userBadge;
	}
	
	@Override
	public Widget asWidget() {
		return userBadge.asWidget();
	}
	
	@Override
	public void setValue(String value) {
		principalId = value;
		userBadge.configure(principalId);
	}

	@Override
	public String getValue() {
		return principalId;
	}

}
