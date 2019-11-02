package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenTeamInvitationsWidgetViewImpl implements OpenTeamInvitationsWidgetView {

	public interface Binder extends UiBinder<Widget, OpenTeamInvitationsWidgetViewImpl> {
	}

	@UiField
	Div mainContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Table invitations;
	Widget widget;
	private OpenTeamInvitationsWidgetView.Presenter presenter;
	private PortalGinInjector ginInjector;

	@Inject
	public OpenTeamInvitationsWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		mainContainer.addStyleName("highlight-box");
		mainContainer.getElement().setAttribute("highlight-box-title", DisplayConstants.PENDING_TEAM_INVITATIONS);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void clear() {
		mainContainer.setVisible(false);
		invitations.clear();
	}

	@Override
	public void addTeamInvite(Team team, String inviteMessage, String createdOn, final String inviteId, Widget joinButtonWidget) {
		mainContainer.setVisible(true);
		OpenTeamInvitationWidget openTeamInviteWidget = ginInjector.getOpenTeamInvitationWidget();

		TeamBadge teamBadge = ginInjector.getTeamBadgeWidget();
		teamBadge.configure(team);
		openTeamInviteWidget.badgeTableData.add(teamBadge);

		openTeamInviteWidget.joinButtonContainer.add(joinButtonWidget);

		if (inviteMessage != null) {
			openTeamInviteWidget.messageTableData.add(new Text(inviteMessage));
		}
		openTeamInviteWidget.createdOnTableData.add(new Italic(createdOn));

		openTeamInviteWidget.cancelButton.addClickHandler(event -> {
			presenter.deleteInvitation(inviteId);
		});

		invitations.add(openTeamInviteWidget);
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
