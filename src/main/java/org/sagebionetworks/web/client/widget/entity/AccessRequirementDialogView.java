package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface AccessRequirementDialogView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void showModal();
	
	void open(String url);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void requestACTClicked();
		void signTermsOfUseClicked();
		void anonymousOkClicked();
		void imposeRestrictionClicked();
	}

}
