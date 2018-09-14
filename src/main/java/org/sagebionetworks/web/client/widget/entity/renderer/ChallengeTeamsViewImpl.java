package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a list of challenge teams
 * @author jayhodgson
 *
 */
public class ChallengeTeamsViewImpl implements ChallengeTeamsView {
	
	public interface Binder extends	UiBinder<Widget, ChallengeTeamsViewImpl> {}

	private Presenter presenter;
	PortalGinInjector ginInjector;
	
	@UiField
	Div paginationWidgetContainer;
	@UiField
	Div dialogWidgetContainer;
	@UiField
	Div challengeTeamsContainer;
	
	@UiField
	Div loadingUI;
	
	@UiField
	Alert errorUI;
	@UiField
	Text errorText;
	@UiField
	Panel challengeTeamsUI;
	
	Widget widget;
	
	@Inject
	public ChallengeTeamsViewImpl(Binder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void clearTeams() {
		challengeTeamsContainer.clear();
	}
	
	@Override
	public void addChallengeTeam(final String teamId, String message, boolean showEditButton) {
		Div div = new Div();
		div.addStyleName("margin-bottom-5");
		TeamBadge newTeamBadge = ginInjector.getTeamBadgeWidget();
		newTeamBadge.configure(teamId);
		Span messageSpan = new Span();
		messageSpan.addStyleName("greyText-imp margin-left-5");
		messageSpan.setText(DisplayUtils.replaceWithEmptyStringIfNull(message));
		
		Widget teamBadgeWidget = newTeamBadge.asWidget();
		teamBadgeWidget.addStyleName("displayInline");
		div.add(teamBadgeWidget);
		div.add(messageSpan.asWidget());
		if (showEditButton) {
			Anchor editButton = new Anchor();
			editButton.setIcon(IconType.EDIT);
			editButton.addStyleName("margin-left-10");
			editButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onEdit(teamId);
				}
			});
			div.add(editButton);
		}
		challengeTeamsContainer.add(div);
	}
	
	@Override
	public void showNoTeams() {
		challengeTeamsContainer.add(new Paragraph(DisplayConstants.EMPTY));
	}
	
	@Override
	public void setEditRegisteredTeamDialog(Widget dialogWidget) {
		dialogWidgetContainer.clear();
		dialogWidgetContainer.add(dialogWidget);
	}
	@Override
	public void setPaginationWidget(Widget paginationWidget) {
		paginationWidgetContainer.clear();
		paginationWidgetContainer.add(paginationWidget);
	}
	
	@Override
	public void clear() {
		clearTeams();
		hideErrors();
	}
	
	@Override
	public void showErrorMessage(String message) {
		errorText.setText(message);
		errorUI.setVisible(true);
		challengeTeamsUI.setVisible(false);
	}
	@Override
	public void hideErrors() {
		errorUI.setVisible(false);
		challengeTeamsUI.setVisible(true);
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}
	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);
	}
}
