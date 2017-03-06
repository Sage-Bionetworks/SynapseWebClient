package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 * 
 * @author Jay
 *
 */
public interface CreateDataAccessSubmissionWizardStep2View extends IsWidget {
	void setDUCVisible(boolean visible);
	void setDUCTemplateFileWidget(IsWidget w);
	void setIRBVisible(boolean visible);
	void setOtherDocumentUploadVisible(boolean visible);
	void setAccessorListWidget(IsWidget w);
	
}
