package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityBadgeView extends IsWidget, SupportsLazyLoadInterface {
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);
	
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
	void showUnlinkIcon();
	void setError(String error);
	void setPresenter(Presenter p);
	String getFriendlySize(Long contentSize, boolean b);

	void showDiscussionThreadIcon();
	public interface Presenter {
		void onUnlink();
	}

}
