package org.sagebionetworks.web.client.widget.team;

import java.util.Map;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderFromAliasAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamBadge implements WidgetRendererPresenter {

	PortalGinInjector ginInjector;
	Map<String, String> widgetDescriptor;
	// resolve from ID
	UserGroupHeaderAsyncHandler usgFromIdAsyncHandler;
	// resolve from alias
	UserGroupHeaderFromAliasAsyncHandler usgFromAliasAsyncHandler;
	DivView div;

	@Inject
	public UserTeamBadge(PortalGinInjector ginInjector, UserGroupHeaderAsyncHandler usgFromIdAsyncHandler, UserGroupHeaderFromAliasAsyncHandler usgFromAliasAsyncHandler, DivView div) {
		this.ginInjector = ginInjector;
		this.usgFromIdAsyncHandler = usgFromIdAsyncHandler;
		this.usgFromAliasAsyncHandler = usgFromAliasAsyncHandler;
		this.div = div;
		div.addStyleName("displayInlineBlock");
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		div.clear();
		this.widgetDescriptor = widgetDescriptor;
		final boolean isToc = widgetDescriptor.containsKey(WidgetConstants.IS_TOC_KEY) ? Boolean.parseBoolean(widgetDescriptor.get(WidgetConstants.IS_TOC_KEY)) : false;

		String alias = widgetDescriptor.get(WidgetConstants.ALIAS_KEY);
		if (alias == null && widgetDescriptor.containsKey(WidgetConstants.USER_TEAM_BADGE_WIDGET_USERNAME_KEY)) {
			alias = widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_USERNAME_KEY);
		}
		if (alias != null) {
			// get user group header for this alias (using a new service)
			usgFromAliasAsyncHandler.getUserGroupHeader(alias, new AsyncCallback<UserGroupHeader>() {
				@Override
				public void onSuccess(UserGroupHeader ugh) {
					Boolean isIndividual = ugh.getIsIndividual();
					String id = ugh.getOwnerId();
					configure(isIndividual, id, isToc);
				}

				@Override
				public void onFailure(Throwable caught) {
					SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
					synAlert.handleException(caught);
					div.add(synAlert);
				}
			});
		} else {
			Boolean isIndividual = Boolean.valueOf(widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY));
			String id = widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
			configure(isIndividual, id, isToc);
		}
	}

	public void configure(final String id) {
		// determine if this is a team id or user
		usgFromIdAsyncHandler.getUserGroupHeader(id, new AsyncCallback<UserGroupHeader>() {
			@Override
			public void onFailure(Throwable caught) {
				SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
				synAlert.handleException(caught);
				div.add(synAlert);
			}

			@Override
			public void onSuccess(UserGroupHeader ugh) {
				configure(ugh.getIsIndividual(), id, false);
			}
		});
	}

	public void configure(Boolean isIndividual, String id, boolean isIgnoreClick) {
		Widget theWidget;
		if (isIndividual) {
			UserBadge badge = ginInjector.getUserBadgeWidget();
			if (id != null) {
				badge.configure(id);
			} else {
				badge.configure((String) null);
			}
			if (isIgnoreClick) {
				badge.setDoNothingOnClick();
			}

			theWidget = badge.asWidget();
		} else {
			// team
			ClickHandler customClickHandler = null;
			if (isIgnoreClick) {
				customClickHandler = UserBadge.DO_NOTHING_ON_CLICK;
			}
			TeamBadge badge = ginInjector.getTeamBadgeWidget();
			badge.configure(id, customClickHandler);
			badge.addStyleName("font-size-13");
			theWidget = badge.asWidget();
		}
		theWidget.addStyleName("margin-left-2");
		div.clear();
		div.add(theWidget);
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return div.asWidget();
	}

}
