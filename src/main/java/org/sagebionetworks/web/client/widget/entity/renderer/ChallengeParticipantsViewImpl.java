package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a list of challenge participants
 * @author jayhodgson
 *
 */
public class ChallengeParticipantsViewImpl implements ChallengeParticipantsView {
	
	public interface Binder extends	UiBinder<Widget, ChallengeParticipantsViewImpl> {}

	private Presenter presenter;
	PortalGinInjector ginInjector;
	
	@UiField
	Div paginationWidgetContainer;
	@UiField
	Div participantsContainer;
	
	@UiField
	Div loadingUI;
	
	@UiField
	Alert errorUI;
	@UiField
	Text errorText;
	
	Widget widget;
	
	@Inject
	public ChallengeParticipantsViewImpl(Binder binder, PortalGinInjector ginInjector) {
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
	public void clearParticipants() {
		participantsContainer.clear();
	}
	@Override
	public void addParticipant(UserProfile profile) {
		UserBadge userBadge = ginInjector.getUserBadgeWidget();
		userBadge.configure(profile);
		participantsContainer.add(userBadge.asWidget());
	}

	@Override
	public void setPaginationWidget(Widget paginationWidget) {
		paginationWidgetContainer.clear();
		paginationWidgetContainer.add(paginationWidget);
	}
	
	@Override
	public void clear() {
		clearParticipants();
		hideErrors();
	}
	
	@Override
	public void showErrorMessage(String message) {
		errorUI.setText(message);
		errorUI.setVisible(true);
	}
	@Override
	public void hideErrors() {
		errorUI.setVisible(false);
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
