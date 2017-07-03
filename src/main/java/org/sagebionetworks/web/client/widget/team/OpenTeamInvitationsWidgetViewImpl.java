package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidgetViewImpl implements OpenTeamInvitationsWidgetView {
	
	public interface Binder extends UiBinder<Widget, OpenTeamInvitationsWidgetViewImpl> {}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Row singleRow;
	Widget widget;
	private OpenTeamInvitationsWidgetView.Presenter presenter;
	private PortalGinInjector ginInjector;
	@Inject
	public OpenTeamInvitationsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_TEAM_INVITATIONS);
		singleRow = new Row();
		mainContainer.add(singleRow);
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
	public void addTeamInvite(Team team, String inviteMessage, String createdOn, Widget joinButtonWidget) {
		mainContainer.setVisible(true);
		FlowPanel lc = new FlowPanel();
		BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
		teamRenderer.configure(team, inviteMessage);
		Column teamBadgeColumn = new Column(ColumnSize.XS_8, ColumnSize.SM_9, ColumnSize.MD_10);
		teamBadgeColumn.addStyleName("margin-top-15");
		teamBadgeColumn.add(teamRenderer.asWidget());
		teamBadgeColumn.add(new Label(createdOn));
		joinButtonWidget.addStyleName("right margin-top-15 margin-right-15");
		
		Column buttonContainer = new Column(ColumnSize.XS_4, ColumnSize.SM_3, ColumnSize.MD_2);
		buttonContainer.add(joinButtonWidget);
		lc.add(teamBadgeColumn);
		lc.add(buttonContainer);
		
		singleRow.add(lc);
	}
	
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
