package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityListRowBadgeView extends IsWidget, SupportsLazyLoadInterface {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntityLink(String name, String url);

	void setCreatedOn(String modifiedOnString);
	
	void setCreatedByWidget(Widget w);
	void setIcon(IconType iconType);
	void setFileDownloadButton(Widget w);
	void setNote(String note);
	String getNote();
	void setDescription(String description);
	void setDescriptionVisible(boolean visible);
	void setVersion(String version);
	void setSynAlert(Widget w);
	void setIsSelectable(boolean isSelectable);
	boolean isSelected();
	void setSelected(boolean selected);
	void showSynAlert();
	void showRow();
	void showLoading();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSelectionChanged();
	}
}
