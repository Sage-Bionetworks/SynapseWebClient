package org.sagebionetworks.web.client.widget.team;

import java.util.Map;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamBadge implements WidgetRendererPresenter {
	
	PortalGinInjector ginInjector;
	Widget theWidget;
	
	@Inject
	public UserTeamBadge(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		Boolean isIndividual = Boolean.valueOf(widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY));
		String id = widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
		if (isIndividual) {
			UserBadge badge = ginInjector.getUserBadgeWidget();
			String username = widgetDescriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_USERNAME_KEY);
			if (username != null) {
				badge.configureWithUsername(username);
			} else{
				badge.configure(id);
			}
			theWidget = badge.asWidget();
			theWidget.addStyleName("movedown-7");
		} else {
			//team
			TeamBadge badge = ginInjector.getTeamBadgeWidget();
			badge.configure(id);
			theWidget = badge.asWidget();
		}
		theWidget.addStyleName("margin-left-2");
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return theWidget;
	}

}
