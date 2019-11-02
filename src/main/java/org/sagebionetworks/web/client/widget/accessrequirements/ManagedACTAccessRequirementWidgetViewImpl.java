package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ManagedACTAccessRequirementWidgetViewImpl implements ManagedACTAccessRequirementWidgetView {

	@UiField
	Div approvedHeading;
	@UiField
	Div unapprovedHeading;
	@UiField
	SimplePanel wikiContainer;
	@UiField
	Alert requestSubmittedMessage;
	@UiField
	Alert requestApprovedMessage;
	@UiField
	Alert requestRejectedMessage;
	@UiField
	Button cancelRequestButton;
	@UiField
	Button updateRequestButton;
	@UiField
	Button requestAccessButton;
	@UiField
	Button loginButton;
	@UiField
	Div requestDataAccessWizardContainer;
	@UiField
	Div editAccessRequirementContainer;
	@UiField
	Div deleteAccessRequirementContainer;
	@UiField
	Div reviewAccessRequestsContainer;
	@UiField
	Div manageAccessContainer;

	@UiField
	Div subjectsWidgetContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div requestSubmittedByOther;
	@UiField
	Div submitterUserBadgeContainer;
	@UiField
	Div cancelRequestButtonContainer;
	@UiField
	Div updateRequestButtonContainer;
	@UiField
	Div requestAccessButtonContainer;
	@UiField
	Div iduReportButtonContainer;
	@UiField
	Span expirationUI;
	@UiField
	Text expirationDateText;

	Callback onAttachCallback;

	public interface Binder extends UiBinder<Widget, ManagedACTAccessRequirementWidgetViewImpl> {
	}

	Widget w;
	Presenter presenter;

	@Inject
	public ManagedACTAccessRequirementWidgetViewImpl(Binder binder, GlobalApplicationState globalAppState) {
		this.w = binder.createAndBindUi(this);
		cancelRequestButton.addClickHandler(event -> {
			presenter.onCancelRequest();
		});
		updateRequestButton.addClickHandler(event -> {
			presenter.onRequestAccess();
		});
		requestAccessButton.addClickHandler(event -> {
			presenter.onRequestAccess();
		});
		w.addAttachHandler(event -> {
			if (event.isAttached()) {
				onAttachCallback.invoke();
			}
		});
		loginButton.addClickHandler(event -> {
			globalAppState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		});
	}

	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setWikiTermsWidget(Widget wikiWidget) {
		wikiContainer.setWidget(wikiWidget);
	}

	@Override
	public void setWikiTermsWidgetVisible(boolean visible) {
		wikiContainer.setVisible(visible);
	}

	@Override
	public void showApprovedHeading() {
		approvedHeading.setVisible(true);
	}

	@Override
	public void showUnapprovedHeading() {
		unapprovedHeading.setVisible(true);
	}

	@Override
	public void showCancelRequestButton() {
		cancelRequestButton.setVisible(true);
	}

	@Override
	public void showRequestAccessButton() {
		requestAccessButton.setVisible(true);
	}

	@Override
	public void showRequestApprovedMessage() {
		requestApprovedMessage.setVisible(true);
	}

	@Override
	public void showRequestRejectedMessage(String reason) {
		requestRejectedMessage.setText("Rejected : " + reason);
		requestRejectedMessage.setVisible(true);
	}

	@Override
	public void showRequestSubmittedMessage() {
		requestSubmittedMessage.setVisible(true);
	}

	@Override
	public void showUpdateRequestButton() {
		updateRequestButton.setVisible(true);
	}

	@Override
	public void setDataAccessRequestWizard(IsWidget w) {
		requestDataAccessWizardContainer.clear();
		requestDataAccessWizardContainer.add(w);
	}

	@Override
	public void resetState() {
		approvedHeading.setVisible(false);
		unapprovedHeading.setVisible(false);
		requestSubmittedMessage.setVisible(false);
		requestApprovedMessage.setVisible(false);
		requestRejectedMessage.setVisible(false);
		cancelRequestButton.setVisible(false);
		updateRequestButton.setVisible(false);
		requestAccessButton.setVisible(false);
		requestSubmittedByOther.setVisible(false);
		expirationUI.setVisible(false);
		loginButton.setVisible(false);
	}

	@Override
	public void setSubmitterUserBadge(IsWidget w) {
		submitterUserBadgeContainer.clear();
		submitterUserBadgeContainer.add(w);
	}

	@Override
	public void showRequestSubmittedByOtherUser() {
		requestSubmittedByOther.setVisible(true);
	}

	@Override
	public void setEditAccessRequirementWidget(IsWidget w) {
		editAccessRequirementContainer.clear();
		editAccessRequirementContainer.add(w);
	}

	@Override
	public void setDeleteAccessRequirementWidget(IsWidget w) {
		deleteAccessRequirementContainer.clear();
		deleteAccessRequirementContainer.add(w);
	}

	@Override
	public void setSubjectsWidget(IsWidget w) {
		subjectsWidgetContainer.clear();
		subjectsWidgetContainer.add(w);
	}

	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}

	@Override
	public void setManageAccessWidget(IsWidget w) {
		manageAccessContainer.clear();
		manageAccessContainer.add(w);
	}

	@Override
	public void setReviewAccessRequestsWidget(IsWidget w) {
		reviewAccessRequestsContainer.clear();
		reviewAccessRequestsContainer.add(w);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(w);
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}

	@Override
	public void setReviewAccessRequestsWidgetContainerVisible(boolean visible) {
		reviewAccessRequestsContainer.setVisible(visible);
	}

	@Override
	public void hideButtonContainers() {
		manageAccessContainer.setVisible(false);
		reviewAccessRequestsContainer.setVisible(false);
		editAccessRequirementContainer.setVisible(false);
		deleteAccessRequirementContainer.setVisible(false);
		cancelRequestButtonContainer.setVisible(false);
		updateRequestButtonContainer.setVisible(false);
		requestAccessButtonContainer.setVisible(false);
	}

	@Override
	public void showExpirationDate(String dateString) {
		expirationDateText.setText(dateString);
		expirationUI.setVisible(true);
	}

	@Override
	public void showLoginButton() {
		loginButton.setVisible(true);
	}

	@Override
	public void setIDUReportButton(IsWidget w) {
		iduReportButtonContainer.clear();
		iduReportButtonContainer.add(w);
	}
}
