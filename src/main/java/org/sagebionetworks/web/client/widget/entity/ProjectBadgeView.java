package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProjectBadgeView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setProject(String projectName, String projectId);
	void setLastActivityText(String text);
	void setLastActivityVisible(boolean isVisible);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void getInfo(final AsyncCallback<KeyValueDisplay<String>> callback);
		void entityClicked();
	}
}
