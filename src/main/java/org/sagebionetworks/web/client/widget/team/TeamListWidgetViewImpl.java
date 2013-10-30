package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
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
	public void configure(List<Team> teams, boolean isBig) {
		clear();
		for (Team team : teams) {
			if (isBig) {
				BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
				teamRenderer.configure(team, team.getDescription());
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("margin-top-15 margin-bottom-40");
				add(teamRendererWidget);
			} else {
				TeamBadge teamRenderer = ginInjector.getTeamBadgeWidget();
				teamRenderer.configure(team);
				Widget teamRendererWidget = teamRenderer.asWidget();
				teamRendererWidget.addStyleName("margin-top-5");
				add(teamRendererWidget);
			}
		}
		if (teams.isEmpty())
			add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.PLACEHOLDER_NAME_PREFIX + " " + DisplayConstants.EMPTY + "</div>").asString()));
	}
}
