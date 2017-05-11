package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserIdCellRendererImpl implements UserIdCellRenderer{

	String principalId;
	DivView view;
	PortalGinInjector ginInjector;
	UserGroupHeaderAsyncHandler userGroupHeaderAsyncHandler;
	
	@Inject
	public UserIdCellRendererImpl(DivView view, UserGroupHeaderAsyncHandler userGroupHeaderAsyncHandler, PortalGinInjector ginInjector) {
		this.view = view;
		this.userGroupHeaderAsyncHandler = userGroupHeaderAsyncHandler;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void setValue(String value) {
		view.clear();
		principalId = value;
		if (DisplayUtils.isDefined(principalId)) {
			userGroupHeaderAsyncHandler.getUserGroupHeader(principalId, new AsyncCallback<UserGroupHeader>() {
				@Override
				public void onSuccess(UserGroupHeader result) {
					if (result.getIsIndividual()) {
						UserBadge badge = ginInjector.getUserBadgeWidget();
						badge.configure(principalId);
						view.add(badge);
					} else {
						TeamBadge badge = ginInjector.getTeamBadgeWidget();
						badge.configure(principalId);
						view.add(badge);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
					synAlert.showError(caught.getMessage());
					view.add(synAlert);
				}
			});
		}
	}

	@Override
	public String getValue() {
		return principalId;
	}

}
