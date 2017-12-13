package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProjectBadgeView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setLastActivityText(String text);
	void setLastActivityVisible(boolean isVisible);
	String getSimpleDateString(Date date);
	void setFavoritesWidget(Widget widget);
	boolean isAttached();
	void addStyleName(String style);
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		String getProjectTooltip();
	}

	void configure(String projectName, String projectId, String tooltip);
}
