package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;

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
	void setProject(String projectName, String href);
	void setLastActivityText(String text);
	void setLastActivityVisible(boolean isVisible);
	String getSimpleDateString(Date date);
	void setFavoritesWidget(Widget widget);
	boolean isAttached();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void getInfo(final AsyncCallback<KeyValueDisplay<String>> callback);
	}
}
