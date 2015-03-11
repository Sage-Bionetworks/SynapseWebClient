package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidgetViewImpl extends FlowPanel implements OpenTeamInvitationsWidgetView {
	
	private SageImageBundle sageImageBundle;
	private OpenTeamInvitationsWidgetView.Presenter presenter;
	private PortalGinInjector ginInjector;
	private FlowPanel mainContainer;
	@Inject
	public OpenTeamInvitationsWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
		this.sageImageBundle = sageImageBundle;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_TEAM_INVITATIONS);
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
	public void configure(List<Team> teams, List<String> inviteMessages) {
		clear();
		mainContainer.clear();
		FlowPanel singleRow = DisplayUtils.createRowContainerFlowPanel();
		for (int i = 0; i < teams.size(); i++) {
			FlowPanel lc = new FlowPanel();
			final Team team = teams.get(i);
			BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
			teamRenderer.configure(team, inviteMessages.get(i));
			Column teamBadgeColumn = new Column(ColumnSize.XS_8, ColumnSize.SM_9, ColumnSize.MD_10);
			teamBadgeColumn.addStyleName("margin-top-15");
			teamBadgeColumn.add(teamRenderer.asWidget());
			Button joinButton = DisplayUtils.createButton(DisplayConstants.JOIN, ButtonType.PRIMARY);
			joinButton.addStyleName("right margin-top-15 margin-right-15 btn-lg");
			joinButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.joinTeam(team.getId());
				}
			});
			Column buttonContainer = new Column(ColumnSize.XS_4, ColumnSize.SM_3, ColumnSize.MD_2);
			buttonContainer.add(joinButton);
			lc.add(teamBadgeColumn);
			lc.add(buttonContainer);
			singleRow.add(lc);
		}
		mainContainer.add(singleRow);
		if (teams.size() > 0)
			add(mainContainer);
	}
}
