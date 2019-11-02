package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateManagedACTAccessRequirementStep2ViewImpl implements CreateManagedACTAccessRequirementStep2View {

	public interface Binder extends UiBinder<Widget, CreateManagedACTAccessRequirementStep2ViewImpl> {
	}

	Widget widget;
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
	TextBox expirationPeriodTextbox;
	@UiField
	CheckBox intendedDataUsePublicCheckbox;

	Presenter presenter;

	@Inject
	public CreateManagedACTAccessRequirementStep2ViewImpl(Binder binder) {
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
	public void setExpirationPeriod(String value) {
		expirationPeriodTextbox.setValue(value);
	}

	@Override
	public String getExpirationPeriod() {
		return expirationPeriodTextbox.getValue();
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
	public boolean areOtherAttachmentsRequired() {
		return otherAttachmentsCheckbox.getValue();
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
