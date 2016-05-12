package org.sagebionetworks.web.client.widget.team;

import java.util.HashMap;
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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamListWidgetViewImpl extends FlowPanel implements TeamListWidgetView {

	private SageImageBundle sageImageBundle;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private Map<String, HasNotificationUI> team2Badge;
	boolean isBig;
	Widget emptyHTML;
	
	@Inject
	public TeamListWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.ginInjector = ginInjector;
		this.emptyHTML = new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.EMPTY_DISPLAY + "</div>").asString());
	}
	
	@Override
	public void showEmpty() {
		add(emptyHTML);
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
	public void addTeam(Team team, Long notificationCount) {
		emptyHTML.setVisible(false);
		SimplePanel container = new SimplePanel();
		container.addStyleName("margin-top-10");
		if (isBig) {
			BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
			teamRenderer.configure(team, team.getDescription());
			if (notificationCount != null && notificationCount > 0)
				teamRenderer.setNotificationValue(String.valueOf(notificationCount));
			team2Badge.put(team.getId(), teamRenderer);
			Widget teamRendererWidget = teamRenderer.asWidget();
			teamRendererWidget.addStyleName("col-sm-12 col-md-6");
			teamRendererWidget.setHeight("120px");
			container.add(teamRendererWidget);
		} else {
			TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
			teamRenderer.configure(team);
			if (notificationCount != null && notificationCount > 0)
				teamRenderer.setNotificationValue(String.valueOf(notificationCount));
			team2Badge.put(team.getId(), teamRenderer);
			Widget teamRendererWidget = teamRenderer.asWidget();
			container.add(teamRendererWidget);
		}
		add(container);
	}
	
	@Override
	public void configure(boolean isBig) {
		this.isBig = isBig;
		team2Badge = new HashMap<String, HasNotificationUI>();
		if (isBig)
			addStyleName("row");
		else
			removeStyleName("row");
	}
}
