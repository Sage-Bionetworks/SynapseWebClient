package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityListRowBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntityLink(String name, String url);

	void showLoadError();
	
	void setCreatedOn(String modifiedOnString);
	
	void setCreatedByWidget(Widget w);
	void setIcon(IconType iconType);
	void setFileDownloadButton(Widget w);
	boolean isInViewport();
	boolean isAttached();
	void setError(String error);
	void showErrorIcon();
	void setNote(String note);
	void setDescription(String description);
	void setDescriptionVisible(boolean visible);
	void setVersion(String version);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void viewAttached();
	}
}
