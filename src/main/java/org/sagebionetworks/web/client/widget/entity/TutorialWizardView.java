package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.Widget;

public interface TutorialWizardView extends SynapseView {

	void showWizard(String ownerObjectId, List<WikiHeader> headers);
	Widget getTutorialLink(String buttonText);
	
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
		void userClickedTutorialButton();
	}
}
