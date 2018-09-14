package org.sagebionetworks.web.client.widget.verification;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VerificationSubmissionRowViewImpl implements VerificationSubmissionWidgetView {

	public interface Binder extends UiBinder<Widget, VerificationSubmissionRowViewImpl> {}
	
	VerificationSubmissionWidgetView.Presenter presenter;
	
	Widget widget;
	
	@UiField
	Span firstName;
	@UiField
	Span lastName;
	@UiField
	Span currentAffiliation;
	@UiField
	Span location;
	@UiField
	Anchor orcIdAnchor;
	@UiField
	Anchor profileAnchor;
	@UiField
	Span state;
	
	@UiField
	Div emailAddresses;
	@UiField
	Div filesContainer;
	
	@UiField
	Button approveButton;
	@UiField
	Button rejectButton;
	@UiField
	Button suspendButton;
	@UiField
	Button deleteButton;
	
	@UiField
	Span reasonAlertText;
	@UiField
	Div synAlertContainer;
	@UiField
	Div promptModalContainer;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public VerificationSubmissionRowViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		
		approveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.approveVerification();
			}
		});
		rejectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.rejectVerification();
			}
		});
		suspendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.suspendVerification();
			}
		});
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteVerification();
			}
		});
		
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		firstName.setText("");
		lastName.setText("");
		currentAffiliation.setText("");
		location.setText("");
		orcIdAnchor.setText("");
		emailAddresses.clear();
		approveButton.setVisible(false);
		rejectButton.setVisible(false);
		suspendButton.setVisible(false);
		deleteButton.setVisible(false);
		reasonAlertText.setText("");
	}

	@Override
	public void show() {
		//Not used in this view implementation
	}

	@Override
	public void hide() {
		//Not used in this view implementation
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setFileHandleList(Widget w) {
		filesContainer.clear();
		filesContainer.add(w);
	}

	@Override
	public void setTitle(String title) {
		//Not used in this view implementation
	}

	@Override
	public void setEmails(List<String> emails) {
		emailAddresses.clear();
		for (String email : emails) {
			Paragraph p = new Paragraph();
			p.setText(email);
			emailAddresses.add(p);
		}
	}

	@Override
	public void setFirstName(String fname) {
		firstName.setText(fname);
	}

	@Override
	public void setLastName(String lname) {
		lastName.setText(lname);
	}

	@Override
	public void setOrganization(String organization) {
		currentAffiliation.setText(organization);
	}

	@Override
	public void setLocation(String l) {
		location.setText(l);
	}

	@Override
	public void setOrcID(String href) {
		orcIdAnchor.setText(href);
		orcIdAnchor.setHref(href);
	}

	@Override
	public void setSubmitButtonVisible(boolean visible) {
		//Not used in this view implementation
	}

	@Override
	public void setCancelButtonVisible(boolean visible) {
		//Not used in this view implementation
	}

	@Override
	public void setOKButtonVisible(boolean visible) {
		//Not used in this view implementation
	}

	@Override
	public void setApproveButtonVisible(boolean visible) {
		approveButton.setVisible(visible);
	}

	@Override
	public void setRejectButtonVisible(boolean visible) {
		rejectButton.setVisible(visible);
	}

	@Override
	public void setSuspendButtonVisible(boolean visible) {
		suspendButton.setVisible(visible);
	}

	@Override
	public void setSuspendedAlertVisible(boolean visible) {
	}
	@Override
	public void setSuspendedReason(String reason) {
		reasonAlertText.setText(reason);
	}
	
	@Override
	public void popupError(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void openWindow(String url) {
		DisplayUtils.newWindow(url, "_self", "");
	}
	
	@Override
	public void setPromptModal(Widget w) {
		promptModalContainer.clear();
		promptModalContainer.add(w);
	}
	@Override
	public void setDeleteButtonVisible(boolean visible) {
		deleteButton.setVisible(visible);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void setProfileLink(String profileId, String href) {
		profileAnchor.setText(profileId);
		profileAnchor.setHref(href);
	}
	
	@Override
	public void setState(VerificationStateEnum s) {
		state.setText(s.toString());
	}
	@Override
	public void setProfileFieldsEditable(boolean editable) {
		//Not used in this view implementation
	}
	@Override
	public void setResubmitButtonVisible(boolean visible) {
		//Not used in this view implementation
	}
	@Override
	public String getFirstName() {
		return firstName.getText();
	}
	@Override
	public String getLastName() {
		return lastName.getText();
	}
	@Override
	public String getLocation() {
		return location.getText();
	}
	@Override
	public String getOrganization() {
		return currentAffiliation.getText();
	}
	@Override
	public void setCloseButtonVisible(boolean visible) {
		//Not used in this view implementation
	}
}
