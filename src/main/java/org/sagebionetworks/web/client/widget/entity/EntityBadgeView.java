package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.schema.ValidationResults;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityBadgeView extends IsWidget, SupportsLazyLoadInterface {
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);

	void setClickHandler(ClickHandler handler);

	void showAddToDownloadList();

	/**
	 *
	 * @param html may be null iff there are no annotations
	 * @param hasSchema
	 * @param validationResults is null iff hasSchema is false
	 */
	void setAnnotations(String html, boolean hasSchema, ValidationResults validationResults);

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

	void setModifiedByUserBadgeClickHandler(ClickHandler handler);

	void showDiscussionThreadIcon();

	void showMinimalColumnSet();

	void clearIcons();
	void clearEntityInformation();

	public interface Presenter {
		void onUnlink();

		void onAddToDownloadList();
	}

}
