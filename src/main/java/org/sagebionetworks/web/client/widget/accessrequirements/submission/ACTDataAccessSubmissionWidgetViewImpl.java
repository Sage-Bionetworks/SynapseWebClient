package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionWidgetViewImpl implements ACTDataAccessSubmissionWidgetView {

	@UiField
	Div synAlertContainer;
	@UiField
	Label submittedOnField;
	@UiField
	Label submittedOnField2;
	@UiField
	Label stateField;
	@UiField
	Label stateField2;
	@UiField
	BlockQuote rejectedReasonField;

	@UiField
	Label institutionField;
	@UiField
	Label institutionField2;
	@UiField
	Label projectLeadField;
	@UiField
	TextArea intendedDataUseField;
	@UiField
	Div accessorsContainer;
	@UiField
	Div ducContainer;
	@UiField
	Div irbContainer;
	@UiField
	Div otherAttachmentsContainer;
	@UiField
	CheckBox renewalCheckbox;
	@UiField
	TextArea publicationsField;
	@UiField
	TextArea summaryOfUseField;
	@UiField
	Button rejectButton;
	@UiField
	Button approveButton;
	@UiField
	Span promptModalContainer;
	@UiField
	FormGroup ducUI;
	@UiField
	FormGroup irbUI;
	@UiField
	FormGroup otherAttachmentsUI;
	@UiField
	FormGroup isRenewalUI;
	@UiField
	FormGroup publicationsUI;
	@UiField
	FormGroup summaryOfUseUI;
	@UiField
	Button closeButton;
	@UiField
	Modal dialog;
	@UiField
	Div submittedByContainer;
	@UiField
	Button moreInfoButton;

	public interface Binder extends UiBinder<Widget, ACTDataAccessSubmissionWidgetViewImpl> {
	}

	Widget w;
	Presenter presenter;

	@Inject
	public ACTDataAccessSubmissionWidgetViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
		rejectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onReject();
			}
		});
		approveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onApprove();
			}
		});
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
		moreInfoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMoreInfo();
			}
		});
	}

	@Override
	public void showMoreInfoDialog() {
		dialog.show();
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
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setPromptModal(IsWidget w) {
		promptModalContainer.clear();
		promptModalContainer.add(w);
	}

	@Override
	public void addAccessors(IsWidget w, String username) {
		Div div = new Div();
		div.add(w);
		w.asWidget().addStyleName("inline-block margin-right-15");
		div.add(new Text(":"));
		TextBoxWithCopyToClipboardWidget emailTextBox = new TextBoxWithCopyToClipboardWidget();
		emailTextBox.setText(username + "@synapse.org");
		emailTextBox.setAddStyleNames("margin-left-15 movedown-2");
		div.add(emailTextBox);
		accessorsContainer.add(div);
	}

	@Override
	public void clearAccessors() {
		accessorsContainer.clear();
	}

	@Override
	public void setOtherAttachmentWidget(IsWidget w) {
		otherAttachmentsContainer.clear();
		otherAttachmentsContainer.add(w);
	}

	@Override
	public void showApproveButton() {
		approveButton.setVisible(true);
	}

	@Override
	public void setDucWidget(IsWidget w) {
		ducContainer.clear();
		ducContainer.add(w);
	}

	@Override
	public void setInstitution(String s) {
		institutionField.setText(s);
		institutionField2.setText(s);
	}

	@Override
	public void setIntendedDataUse(String s) {
		intendedDataUseField.setText(s);
	}

	@Override
	public void setIrbWidget(IsWidget w) {
		irbContainer.clear();
		irbContainer.add(w);
	}

	@Override
	public void setIsRenewal(boolean b) {
		renewalCheckbox.setValue(b);
	}

	@Override
	public void setProjectLead(String s) {
		projectLeadField.setText(s);
	}

	@Override
	public void setPublications(String s) {
		publicationsField.setText(s);
	}

	@Override
	public void showRejectButton() {
		rejectButton.setVisible(true);
	}

	@Override
	public void setState(String s) {
		stateField.setText(s);
		stateField2.setText(s);
	}

	@Override
	public void setSummaryOfUse(String s) {
		summaryOfUseField.setText(s);
	}

	@Override
	public void setDucColumnVisible(boolean visible) {
		ducUI.setVisible(visible);
	}

	@Override
	public void setIrbColumnVisible(boolean visible) {
		irbUI.setVisible(visible);
	}

	@Override
	public void setOtherAttachmentsColumnVisible(boolean visible) {
		otherAttachmentsUI.setVisible(visible);
	}

	@Override
	public void setRenewalColumnsVisible(boolean visible) {
		isRenewalUI.setVisible(visible);
		publicationsUI.setVisible(visible);
		summaryOfUseUI.setVisible(visible);
	}

	@Override
	public void hideActions() {
		dialog.hide();
		rejectButton.setVisible(false);
		approveButton.setVisible(false);
	}

	@Override
	public void setSubmittedOn(String s) {
		submittedOnField.setText(s);
		submittedOnField2.setText(s);
	}

	@Override
	public void setSubmittedBy(IsWidget w) {
		submittedByContainer.clear();
		submittedByContainer.add(w);
	}

	@Override
	public void setRejectedReason(String reason) {
		rejectedReasonField.clear();
		rejectedReasonField.add(new Text(reason));
	}

	@Override
	public void setRejectedReasonVisible(boolean visible) {
		rejectedReasonField.setVisible(visible);
	}
}
