package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMinValidator;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.form.validator.Validator.Priority;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
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
	TextBox expirationPeriodTextbox;
	@UiField
	CheckBox intendedDataUsePublicCheckbox;
	@UiField
	Div hasRequestUI;
	@UiField
	Radio hasRequestButton;
	@UiField
	Radio hasNoRequestButton;
	
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
		hasRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showHasRequestUI(true);
			}
		});
		hasNoRequestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showHasRequestUI(false);
			}
		});
		expirationPeriodTextbox.addValidator(new Validator<String>() {
			public List<EditorError> validate(Editor<String> editor, String value) {
				List<EditorError> result = new ArrayList<EditorError>();
				try {
					Long.parseLong(value.toString());
				} catch (Throwable th) {
					result.add(new BasicEditorError(editor, value, "Value must be >= 0"));
				}
				return result;
			};

			@Override
			public int getPriority() {
				return Priority.MEDIUM;
			}
		});
	}
	
	@Override
	public void showHasRequestUI(boolean hasRequest) {
		hasRequestUI.setVisible(hasRequest);
		hasRequestButton.setValue(hasRequest, false);
		hasNoRequestButton.setValue(!hasRequest, false);
	}
	@Override
	public boolean getHasRequests() {
		return hasRequestButton.getValue();
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
	public void setExpirationPeriod(Long value) {
		expirationPeriodTextbox.setValue(value.toString());	
	}
	@Override
	public long getExpirationPeriod() {
		return Long.parseLong(expirationPeriodTextbox.getValue());
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
