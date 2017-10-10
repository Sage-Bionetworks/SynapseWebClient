package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityBadgeView extends IsWidget, SynapseView, SupportsLazyLoadInterface {
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);
	
	void showLoadingIcon();
	
	void hideLoadingIcon();
	
	void addClickHandler(ClickHandler handler);
	
	void setModifiedOn(String modifiedOnString);
	
	void setModifiedByWidget(Widget w);
	void setModifiedByWidgetVisible(boolean visible);
	void setIcon(IconType iconType);
	void setFileDownloadButton(Widget w);
	void setAnnotations(String html);
	void setSize(String s);
	void setMd5(String s);
	void showPublicIcon();
	void showPrivateIcon();
	void showSharingSetIcon();
	void showHasWikiIcon();
	void showAnnotationsIcon();
	void showDeleteIcon();
	void setError(String error);
	void showErrorIcon();
	void setPresenter(Presenter p);
	String getFriendlySize(Long contentSize, boolean b);

	void setDiscussionThreadIconVisible(boolean visible);
	public interface Presenter {
		void onDelete();
	}

}
