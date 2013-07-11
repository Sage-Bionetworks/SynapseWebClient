package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.SynapseView;

public interface TutorialWizardView extends SynapseView {

	void showWizard(String ownerObjectId, List<WikiHeader> headers);
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void userSkippedTutorial();
		void userFinishedTutorial();
	}
}
