package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateACTAccessRequirementStep2ViewImpl implements CreateACTAccessRequirementStep2View {

	public interface Binder extends UiBinder<Widget, CreateACTAccessRequirementStep2ViewImpl> {}
	
	Widget widget;
	@UiField
	FormGroup oldInstructionsUI;
	@UiField
	Paragraph oldInstructions;
	@UiField
	Div wikiPageContainer;
	@UiField
	Button editWikiButton;
	@UiField
	CheckBox certifiedCheckbox;
	@UiField
	CheckBox validatedCheckbox;
	@UiField
	CheckBox ducCheckbox;
	@UiField
	Div ducTemplateFileContainer;
	@UiField
	Div ducTemplateFileUploadContainer;
	@UiField
	CheckBox irbCheckbox;
	@UiField
	CheckBox otherAttachmentsCheckbox;
	@UiField
	CheckBox annualRenewalCheckbox;
	@UiField
	CheckBox intendedDataUsePublicCheckbox;
	
	Presenter presenter;
	
	@Inject
	public CreateACTAccessRequirementStep2ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		editWikiButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditWiki();
			}
		});
		
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public void setAreOtherAttachmentsRequired(boolean value) {
		otherAttachmentsCheckbox.setValue(value);
	}
	@Override
	public void setIsAnnualReviewRequired(boolean value) {
		annualRenewalCheckbox.setValue(value);
	}
	@Override
	public void setIsCertifiedUserRequired(boolean value) {
		certifiedCheckbox.setValue(value);
	}
	@Override
	public void setIsDUCRequired(boolean value) {
		ducCheckbox.setValue(value);
	}
	@Override
	public void setIsIDUPublic(boolean value) {
		intendedDataUsePublicCheckbox.setValue(value);
	}
	@Override
	public void setIsIRBApprovalRequired(boolean value) {
		irbCheckbox.setValue(value);
	}
	@Override
	public void setIsValidatedProfileRequired(boolean value) {
		validatedCheckbox.setValue(value);
	}
	@Override
	public void setOldTermsVisible(boolean visible) {
		oldInstructionsUI.setVisible(visible);
	}
	@Override
	public void setOldTerms(String terms) {
		oldInstructions.setText(terms);
	}
	@Override
	public boolean areOtherAttachmentsRequired() {
		return otherAttachmentsCheckbox.getValue();
	}
	@Override
	public boolean isAnnualReviewRequired() {
		return annualRenewalCheckbox.getValue();
	}
	@Override
	public boolean isCertifiedUserRequired() {
		return certifiedCheckbox.getValue();
	}
	@Override
	public boolean isDUCRequired() {
		return ducCheckbox.getValue();
	}
	@Override
	public boolean isIDUPublic() {
		return intendedDataUsePublicCheckbox.getValue();
	}
	@Override
	public boolean isIRBApprovalRequired() {
		return irbCheckbox.getValue();
	}
	@Override
	public boolean isValidatedProfileRequired() {
		return validatedCheckbox.getValue();
	}
	@Override
	public void setWikiPageRenderer(IsWidget w) {
		wikiPageContainer.clear();
		wikiPageContainer.add(w);
	}
	@Override
	public void setDUCTemplateUploadWidget(IsWidget w) {
		ducTemplateFileUploadContainer.clear();
		ducTemplateFileUploadContainer.add(w);
	}
	@Override
	public void setDUCTemplateWidget(IsWidget w) {
		ducTemplateFileContainer.clear();
		ducTemplateFileContainer.add(w);
	}
	
}
