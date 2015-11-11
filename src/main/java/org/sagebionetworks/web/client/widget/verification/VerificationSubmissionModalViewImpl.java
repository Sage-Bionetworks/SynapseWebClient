package org.sagebionetworks.web.client.widget.verification;

import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VerificationSubmissionModalViewImpl implements VerificationSubmissionModalView {

	public interface Binder extends UiBinder<Widget, VerificationSubmissionModalViewImpl> {}
	
	VerificationSubmissionModalView.Presenter presenter;
	
	Widget widget;

	@UiField
	Div publicallyVisible;
	
	@UiField
	TextBox firstName;
	@UiField
	TextBox lastName;
	@UiField
	TextBox currentAffiliation;
	@UiField
	TextBox location;
	@UiField
	Anchor orcIdAnchor;
	
	@UiField
	Div actOnly;
	@UiField
	Div emailAddresses;
	@UiField
	Div filesContainer;
	
	@UiField
	Button submitButton;
	@UiField
	Button approveButton;
	@UiField
	Button rejectButton;
	@UiField
	Button suspendButton;
	@UiField
	Button cancelButton;
	@UiField
	Button okButton;
	@UiField
	Modal dialog;
	@UiField
	Div synAlertContainer;
	@UiField
	Div wikiPageContainer;
	
	@UiField
	Heading reasonHeading;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public VerificationSubmissionModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
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
	public void clear() {
		reasonHeading.setText("");
		firstName.setText("");
		lastName.setText("");
		currentAffiliation.setText("");
		location.setText("");
		orcIdAnchor.setText("");
		emailAddresses.clear();
		cancelButton.setVisible(false);
		submitButton.setVisible(false);
		okButton.setVisible(false);
		approveButton.setVisible(false);
		rejectButton.setVisible(false);
		suspendButton.setVisible(false);
	}

	@Override
	public void show() {
		dialog.show();
	}

	@Override
	public void hide() {
		dialog.hide();
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
	public void setWikiPage(Widget w) {
		wikiPageContainer.clear();
		wikiPageContainer.add(w);
	}

	@Override
	public void setWikiPageVisible(boolean visible) {
		wikiPageContainer.setVisible(visible);
	}

	@Override
	public void setTitle(String title) {
		dialog.setTitle(title);
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
		firstName.setValue(fname);
	}

	@Override
	public void setLastName(String lname) {
		lastName.setValue(lname);
	}

	@Override
	public void setOrganization(String organization) {
		currentAffiliation.setValue(organization);
	}

	@Override
	public void setLocation(String l) {
		location.setValue(l);
	}

	@Override
	public void setOrcID(String href) {
		orcIdAnchor.setText(href);
		orcIdAnchor.setHref(href);
	}

	@Override
	public void setSubmitButtonVisible(boolean visible) {
		submitButton.setVisible(visible);
	}

	@Override
	public void setCancelButtonVisible(boolean visible) {
		cancelButton.setVisible(visible);
	}

	@Override
	public void setOKButtonVisible(boolean visible) {
		okButton.setVisible(visible);
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
	public void setSuspendedReason(String reason) {
		reasonHeading.setText(reason);
	}

	@Override
	public void popupError(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void openWindow(String url) {
		DisplayUtils.newWindow(url, "", "");
	}
	
}
