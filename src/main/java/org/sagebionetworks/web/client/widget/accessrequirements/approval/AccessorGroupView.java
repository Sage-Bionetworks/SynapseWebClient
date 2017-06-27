package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import com.google.gwt.user.client.ui.IsWidget;

public interface AccessorGroupView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void addStyleNames(String styleNames);
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
	void clearAccessors();
	void addAccessor(IsWidget w);
	void setSubmittedBy(IsWidget w);
	void setAccessRequirementWidget(IsWidget w);
	void showAccessRequirementDialog();
	void setEmailAddresses(String emailAddresses);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onShowAccessRequirement();
		void onRevoke();
	}

}
