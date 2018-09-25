package org.sagebionetworks.web.client.widget.team;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidgetViewImpl extends FlowPanel implements TeamListWidgetView {
	private PortalGinInjector ginInjector;
	private Map<String, HasNotificationUI> team2Badge = new HashMap<String, HasNotificationUI>();
	Widget emptyHTML;
	
	@Inject
	public TeamListWidgetViewImpl(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
		this.emptyHTML = new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.EMPTY_DISPLAY + "</div>").asString());
	}
	
	@Override
	public void clear() {
		super.clear();
		team2Badge = new HashMap<String, HasNotificationUI>();
	}
	
	@Override
	public void showEmpty() {
		add(emptyHTML);
	}
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget());
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void addTeam(Team team) {
		emptyHTML.setVisible(false);
		SimplePanel container = new SimplePanel();
		container.addStyleName("margin-top-10");
		TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
		teamRenderer.configure(team);
		team2Badge.put(team.getId(), teamRenderer);
		Widget teamRendererWidget = teamRenderer.asWidget();
		container.add(teamRendererWidget);
		add(container);
	}
	
	@Override
	public void setNotificationValue(String teamId, Long notificationCount) {
		if (notificationCount != null && notificationCount > 0) {
			team2Badge.get(teamId).setNotificationValue(String.valueOf(notificationCount));
		}
	}
}
