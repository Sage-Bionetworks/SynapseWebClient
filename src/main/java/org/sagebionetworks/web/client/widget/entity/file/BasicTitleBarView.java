package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BasicTitleBarView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void setFavoritesWidget(Widget favoritesWidget);
	void setFavoritesWidgetVisible(boolean visible);
	void setTitle(String name);
	void setIconType(IconType iconType);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
