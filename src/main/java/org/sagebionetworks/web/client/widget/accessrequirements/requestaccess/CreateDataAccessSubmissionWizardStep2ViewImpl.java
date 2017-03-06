package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateDataAccessSubmissionWizardStep2ViewImpl implements CreateDataAccessSubmissionWizardStep2View {

	public interface Binder extends UiBinder<Widget, CreateDataAccessSubmissionWizardStep2ViewImpl> {}
	
	Widget widget;
	
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
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setDUCTemplateFileWidget(IsWidget w) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setIRBVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setOtherDocumentUploadVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setAccessorListWidget(IsWidget w) {
		// TODO Auto-generated method stub
		
	}
}
