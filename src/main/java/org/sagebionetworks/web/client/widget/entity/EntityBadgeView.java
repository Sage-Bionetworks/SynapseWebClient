package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setEntity(EntityQueryResult header);

	void showLoadError(String entityId);
	
	void showLoadingIcon();
	
	void hideLoadingIcon();
	
	void setClickHandler(ClickHandler handler);
	
	void setModifiedOn(String modifiedOnString);
	
	void setModifiedByWidget(Widget w);
	void setModifiedByWidgetVisible(boolean visible);
	void setIcon(IconType iconType);
	
	void setAnnotations(String html);
	void setSize(String s);
	void showPublicIcon();
	void showPrivateIcon();
	void showSharingSetIcon();
	void showHasWikiIcon();
	boolean isInViewport();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void entityClicked(EntityQueryResult entityHeader);
	}
	String getFriendlySize(Long contentSize, boolean b);

}
