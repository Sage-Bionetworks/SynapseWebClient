package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ChallengeBadgeView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setChallenge(ChallengeSummary header);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onClick();
		void onParticipantsClick();
	}

	

}
