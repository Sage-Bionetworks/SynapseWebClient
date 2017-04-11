package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessRequirementWidgetViewImpl implements ACTAccessRequirementWidgetView {

	@UiField
	Div approvedHeading;
	@UiField
	Div unapprovedHeading;
	@UiField
	BlockQuote wikiTermsUI;
	@UiField
	SimplePanel wikiContainer;
	@UiField
	BlockQuote termsUI;
	@UiField
	HTML terms;
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
	Div requestDataAccessWizardContainer;
	@UiField
	Div editAccessRequirementContainer;
	@UiField
	Div deleteAccessRequirementContainer;
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
	
	Callback onAttachCallback;
	public interface Binder extends UiBinder<Widget, ACTAccessRequirementWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public ACTAccessRequirementWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
		cancelRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancelRequest();
			}
		});
		updateRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRequestAccess();
			}
		});
		requestAccessButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRequestAccess();
			}
		});
		w.addAttachHandler(new AttachEvent.Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttachCallback.invoke();
				}
			}
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
	public void setTerms(String arText) {
		terms.setHTML(arText);
	}
	@Override
	public void showTermsUI() {
		termsUI.setVisible(true);
	}
	@Override
	public void showWikiTermsUI() {
		wikiTermsUI.setVisible(true);
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
}
