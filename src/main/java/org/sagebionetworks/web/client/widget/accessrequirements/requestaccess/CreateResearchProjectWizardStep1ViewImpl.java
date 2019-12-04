package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Italic;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateResearchProjectWizardStep1ViewImpl implements CreateResearchProjectWizardStep1View {

	public interface Binder extends UiBinder<Widget, CreateResearchProjectWizardStep1ViewImpl> {
	}

	@UiField
	TextBox projectLeadField;
	@UiField
	TextBox institutionField;
	@UiField
	TextArea intendedDataUseStatementField;
	@UiField
	Italic visibleToPublicNote;
	Widget widget;

	@Inject
	public CreateResearchProjectWizardStep1ViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public String getInstitution() {
		return institutionField.getText();
	}

	@Override
	public void setInstitution(String text) {
		institutionField.setText(text);
	}

	@Override
	public String getIntendedDataUseStatement() {
		return intendedDataUseStatementField.getText();
	}

	@Override
	public void setIntendedDataUseStatement(String text) {
		intendedDataUseStatementField.setText(text);
	}

	@Override
	public String getProjectLead() {
		return projectLeadField.getText();
	}

	@Override
	public void setProjectLead(String text) {
		projectLeadField.setText(text);
	}

	@Override
	public void setIDUPublicNoteVisible(boolean visible) {
		visibleToPublicNote.setVisible(visible);
	}
}
