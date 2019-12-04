package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserIdCellRenderer implements Cell {

	String principalId;
	DivView view;
	PortalGinInjector ginInjector;
	UserGroupHeaderAsyncHandler userGroupHeaderAsyncHandler;
	ClickHandler customClickHandler = null;

	@Inject
	public UserIdCellRenderer(DivView view, UserGroupHeaderAsyncHandler userGroupHeaderAsyncHandler, PortalGinInjector ginInjector) {
		this.view = view;
		this.userGroupHeaderAsyncHandler = userGroupHeaderAsyncHandler;
		this.ginInjector = ginInjector;
		view.addStyleName("displayInlineBlock");
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setValue(String value, ClickHandler customClickHandler) {
		this.customClickHandler = customClickHandler;
		setValue(value);
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
						if (customClickHandler != null) {
							badge.setCustomClickHandler(customClickHandler);
						}
						badge.configure(principalId);
						view.add(badge);
					} else {
						TeamBadge badge = ginInjector.getTeamBadgeWidget();
						if (customClickHandler != null) {
							badge.configure(principalId, customClickHandler);
						} else {
							badge.configure(principalId);
						}

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

	public void addStyleName(String styles) {
		view.addStyleName(styles);
	}
}
