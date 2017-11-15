package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
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
	public void addTeamInvite(Team team, String inviteMessage, String createdOn, final String inviteId, Widget joinButtonWidget) {
		mainContainer.setVisible(true);
		FlowPanel lc = new FlowPanel();
		BigTeamBadge teamRenderer = ginInjector.getBigTeamBadgeWidget();
		teamRenderer.configure(team, inviteMessage);
		Column teamBadgeColumn = new Column(ColumnSize.XS_12, ColumnSize.SM_7, ColumnSize.MD_8);
		teamBadgeColumn.addStyleName("margin-top-15");
		teamBadgeColumn.add(teamRenderer.asWidget());
		Span createdOnSpan = new Span();
		createdOnSpan.setText(createdOn);
		teamBadgeColumn.add(createdOnSpan);
		joinButtonWidget.addStyleName("right margin-top-15 margin-right-15");
		
		Button deleteButton = new Button("Remove");
		deleteButton.setSize(ButtonSize.LARGE);
		deleteButton.setType(ButtonType.DANGER);
		deleteButton.setPull(Pull.RIGHT);
		deleteButton.setMarginRight(5);
		deleteButton.setMarginTop(15);
		deleteButton.addClickHandler(event -> {
			presenter.deleteInvitation(inviteId);
		});
		
		Column buttonContainer = new Column(ColumnSize.XS_12, ColumnSize.SM_5, ColumnSize.MD_4);
		buttonContainer.add(joinButtonWidget);
		buttonContainer.add(deleteButton);
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
