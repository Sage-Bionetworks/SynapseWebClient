package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityAccessRequirementsWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void showWizard();
	void hideWizard();
	
	void updateWizardProgress(int currentPage, int totalPages);
	
	void showAccessRequirement(
			String arText,
			final Callback acceptanceCallback);
	void showInfo(String title, String message);
	public interface Presenter extends SynapsePresenter {
		void wizardCanceled();
	}
}
