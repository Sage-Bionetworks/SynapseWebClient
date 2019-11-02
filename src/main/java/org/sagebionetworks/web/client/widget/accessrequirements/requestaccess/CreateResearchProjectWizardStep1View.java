package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the first step of the wizard
 * 
 * @author Jay
 *
 */
public interface CreateResearchProjectWizardStep1View extends IsWidget {

	String getProjectLead();

	void setProjectLead(String text);

	String getInstitution();

	void setInstitution(String text);

	String getIntendedDataUseStatement();

	void setIntendedDataUseStatement(String text);

	void setIDUPublicNoteVisible(boolean visible);
}
