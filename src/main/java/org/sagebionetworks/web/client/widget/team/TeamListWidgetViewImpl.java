package org.sagebionetworks.web.client.widget.team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidgetViewImpl extends FlowPanel implements TeamListWidgetView {

	private SageImageBundle sageImageBundle;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private Map<String, HasNotificationUI> team2Badge;
	boolean isBig;
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
		if (team2Badge.containsKey(teamId)) {
			team2Badge.get(teamId).setNotificationValue(count.toString());
		}
	}
	
	@Override
	public void configure(List<Team> teams, boolean isBig) {
		this.isBig = isBig;
		clear();
		team2Badge = new HashMap<String, HasNotificationUI>();
		if (isBig)
			addStyleName("row");
		else
			removeStyleName("row");
		
		for (Team team : teams) {
			if (isBig) {
				BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
				teamRenderer.configure(team, team.getDescription());
				team2Badge.put(team.getId(), teamRenderer);
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("col-sm-12 col-md-6 margin-top-15");
				teamRendererWidget.setHeight("120px");
				add(teamRendererWidget);
			} else {
				TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
				teamRenderer.configure(team);
				team2Badge.put(team.getId(), teamRenderer);
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("margin-top-10");
				add(teamRendererWidget);
			}
		}
		if (teams.isEmpty())
			add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.EMPTY_DISPLAY + "</div>").asString()));
	}
}
