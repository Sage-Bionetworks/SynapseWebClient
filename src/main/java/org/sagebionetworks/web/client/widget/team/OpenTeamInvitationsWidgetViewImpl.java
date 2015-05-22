package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidgetViewImpl extends FlowPanel implements OpenTeamInvitationsWidgetView {
	
	private SageImageBundle sageImageBundle;
	private OpenTeamInvitationsWidgetView.Presenter presenter;
	private PortalGinInjector ginInjector;
	private FlowPanel mainContainer;
	private Row singleRow;
	@Inject
	public OpenTeamInvitationsWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
		this.sageImageBundle = sageImageBundle;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_TEAM_INVITATIONS);
		add(mainContainer);
		singleRow = new Row();
		mainContainer.add(singleRow);
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
	public void clear() {
		mainContainer.setVisible(false);
		singleRow.clear();
	}
	
	@Override
	public void addTeamInvite(Team team, String inviteMessage, Widget joinButtonWidget) {
		mainContainer.setVisible(true);
		FlowPanel lc = new FlowPanel();
		BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
		teamRenderer.configure(team, inviteMessage);
		Column teamBadgeColumn = new Column(ColumnSize.XS_8, ColumnSize.SM_9, ColumnSize.MD_10);
		teamBadgeColumn.addStyleName("margin-top-15");
		teamBadgeColumn.add(teamRenderer.asWidget());
		joinButtonWidget.addStyleName("right margin-top-15 margin-right-15");
		
		Column buttonContainer = new Column(ColumnSize.XS_4, ColumnSize.SM_3, ColumnSize.MD_2);
		buttonContainer.add(joinButtonWidget);
		lc.add(teamBadgeColumn);
		lc.add(buttonContainer);
		singleRow.add(lc);
	}
}
