package org.sagebionetworks.web.client.widget.team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidgetViewImpl extends FlowPanel implements TeamListWidgetView {

	private SageImageBundle sageImageBundle;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private Map<String, SimplePanel> team2NotificationPanel;
	@Inject
	public TeamListWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setRequestCount(String teamId, Long count) {
		if (team2NotificationPanel.containsKey(teamId)) {
			HTML widget = new HTML(DisplayUtils.getBadgeHtml(count.toString()));
			DisplayUtils.addToolTip(widget, "Team has a pending join request");
			team2NotificationPanel.get(teamId).setWidget(widget);
		}
	}
	
	@Override
	public void configure(List<Team> teams, boolean isBig) {
		clear();
		team2NotificationPanel = new HashMap<String, SimplePanel>();
		if (isBig)
			addStyleName("row");
		else
			removeStyleName("row");
		
		for (Team team : teams) {
			if (isBig) {
				BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
				teamRenderer.configure(team, team.getDescription());
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("col-sm-12 col-md-6 margin-top-15");
				teamRendererWidget.setHeight("120px");
				add(teamRendererWidget);
			} else {
				FlowPanel teamPanel = new FlowPanel();
				add(teamPanel);
				TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
				teamRenderer.configure(team);
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("margin-top-5 inline-block");
				teamPanel.add(teamRendererWidget);
				SimplePanel notificationContainer = new SimplePanel();
				notificationContainer.addStyleName("inline-block moveup-5 margin-left-5");
				team2NotificationPanel.put(team.getId(), notificationContainer);
				teamPanel.add(notificationContainer);
			}
			
			
		}
		if (teams.isEmpty())
			add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.PLACEHOLDER_NAME_PREFIX + " " + DisplayConstants.EMPTY + "</div>").asString()));
	}
}
