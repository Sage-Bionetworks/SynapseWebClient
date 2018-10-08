package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateDataAccessSubmissionWizardStep2ViewImpl implements CreateDataAccessSubmissionWizardStep2View {

	public interface Binder extends UiBinder<Widget, CreateDataAccessSubmissionWizardStep2ViewImpl> {}
	
	Widget widget;
	@UiField
	Div accessorsContainer;
	@UiField
	Div peopleSuggestContainer;
	@UiField
	Div ducTemplateContainer;
	@UiField
	Div ducUploadContainer;
	@UiField
	Div ducUploadedContainer;
	@UiField
	Div irbUploadContainer;
	@UiField
	Div irbUploadedContainer;
	@UiField
	Div otherUploadedContainer;

	@UiField
	FormGroup publicationsUI;
	@UiField
	TextArea publicationsField;
	@UiField
	FormGroup summaryOfUseUI;
	@UiField
	TextArea summaryOfUseField;
	@UiField
	Div ducUI;
	@UiField
	FormGroup irbUI;
	@UiField
	FormGroup otherUploadUI;
	@UiField
	FormGroup ducTemplateUI;
	@UiField
	Div validatedUserProfileNote;
	@UiField
	Div ducDataRequestorsNote;
	
	@Inject
	public CreateDataAccessSubmissionWizardStep2ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void setDUCVisible(boolean visible) {
		ducUI.setVisible(visible);
		ducDataRequestorsNote.setVisible(visible);
	}
	@Override
	public void setDUCTemplateFileWidget(IsWidget w) {
		ducTemplateContainer.clear();
		ducTemplateContainer.add(w);
	}
	@Override
	public void setIRBVisible(boolean visible) {
		irbUI.setVisible(visible);
	}
	@Override
	public void setOtherDocumentUploadVisible(boolean visible) {
		otherUploadUI.setVisible(visible);
	}
	@Override
	public void setAccessorListWidget(IsWidget w) {
		accessorsContainer.clear();
		accessorsContainer.add(w);
	}
	@Override
	public void setDUCUploadWidget(IsWidget w) {
		ducUploadContainer.clear();
		ducUploadContainer.add(w);
	}
	@Override
	public void setIRBUploadWidget(IsWidget w) {
		irbUploadContainer.clear();
		irbUploadContainer.add(w);
	}
	@Override
	public void setOtherDocumentUploaded(IsWidget w) {
		otherUploadedContainer.clear();
		otherUploadedContainer.add(w);
	}
	@Override
	public void setPublicationsVisible(boolean visible) {
		publicationsUI.setVisible(visible);
	}
	@Override
	public String getPublications() {
		return publicationsField.getText();
	}
	@Override
	public void setSummaryOfUseVisible(boolean visible) {
		summaryOfUseUI.setVisible(visible);
	}
	@Override
	public String getSummaryOfUse() {
		return summaryOfUseField.getText();
	}
	@Override
	public void setSummaryOfUse(String text) {
		summaryOfUseField.setText(text);
	}
	@Override
	public void setPublications(String text) {
		publicationsField.setText(text);
	}

	@Override
	public void setDUCUploadedFileWidget(IsWidget w) {
		ducUploadedContainer.clear();
		ducUploadedContainer.add(w);
	}
	
	@Override
	public void setIRBUploadedFileWidget(IsWidget w) {
		irbUploadedContainer.clear();
		irbUploadedContainer.add(w);
	}
	
	@Override
	public void setPeopleSuggestWidget(IsWidget w) {
		peopleSuggestContainer.clear();
		peopleSuggestContainer.add(w);
	}
	@Override
	public void setDUCTemplateVisible(boolean visible) {
		ducTemplateUI.setVisible(visible);
	}
	@Override
	public void setValidatedUserProfileNoteVisible(boolean visible) {
		validatedUserProfileNote.setVisible(visible);	
	}
}
