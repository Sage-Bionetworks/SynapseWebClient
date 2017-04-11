package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionWidgetViewImpl implements ACTDataAccessSubmissionWidgetView {

	@UiField
	Div synAlertContainer;
	@UiField
	Label createdOnField;
	@UiField
	Label stateField;
	@UiField
	Label institutionField;
	@UiField
	Label projectLeadField;
	@UiField
	TextBox intendedDataUseField;
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
	TextBox publicationsField;
	@UiField
	TextBox summaryOfUseField;
	@UiField
	Button rejectButton;
	@UiField
	Button approveButton;
	@UiField
	Span promptModalContainer;
	@UiField
	TableData ducColumn;
	@UiField
	TableData irbColumn;
	@UiField
	TableData otherAttachmentsColumn;
	@UiField
	TableData isRenewalColumn;
	@UiField
	TableData publicationsColumn;
	@UiField
	TableData summaryOfUseColumn;
	public interface Binder extends UiBinder<Widget, ACTDataAccessSubmissionWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public ACTDataAccessSubmissionWidgetViewImpl(Binder binder){
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
	public void setAccessors(IsWidget w) {
		accessorsContainer.clear();
		accessorsContainer.add(w);
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
	}
	@Override
	public void setSummaryOfUse(String s) {
		summaryOfUseField.setText(s);
	}
	
	@Override
	public void setDucColumnVisible(boolean visible) {
		ducColumn.setVisible(visible);
	}
	@Override
	public void setIrbColumnVisible(boolean visible) {
		irbColumn.setVisible(visible);
	}
	@Override
	public void setOtherAttachmentsColumnVisible(boolean visible) {
		otherAttachmentsColumn.setVisible(visible);
	}
	@Override
	public void setRenewalColumnsVisible(boolean visible) {
		isRenewalColumn.setVisible(visible);
		publicationsColumn.setVisible(visible);
		summaryOfUseColumn.setVisible(visible);
	}
	@Override
	public void hideActions() {
		rejectButton.setVisible(false);
		approveButton.setVisible(false);
	}
}
