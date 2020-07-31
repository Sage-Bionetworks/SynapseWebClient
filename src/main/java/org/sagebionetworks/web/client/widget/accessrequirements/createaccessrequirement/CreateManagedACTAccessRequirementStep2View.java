package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the second step of the wizard for ACT AR
 * 
 * @author Jay
 *
 */
public interface CreateManagedACTAccessRequirementStep2View extends IsWidget {
	void setAreOtherAttachmentsRequired(boolean value);

	void setExpirationPeriod(String days);

	void setIsCertifiedUserRequired(boolean value);

	void setIsDUCRequired(boolean value);

	void setIsIDUPublic(boolean value);

	void setIsIRBApprovalRequired(boolean value);

	void setIsValidatedProfileRequired(boolean value);

	boolean areOtherAttachmentsRequired();

	String getExpirationPeriod();

	boolean isCertifiedUserRequired();

	boolean isDUCRequired();

	boolean isIDUPublic();

	boolean isIRBApprovalRequired();

	boolean isValidatedProfileRequired();

	void setWikiPageRenderer(IsWidget w);

	void setDUCTemplateUploadWidget(IsWidget w);

	void setDUCTemplateWidget(IsWidget w);

	public void setPresenter(Presenter p);

	/*
	 * Presenter interface
	 */
	public interface Presenter {
		void onEditWiki();
	}
}
