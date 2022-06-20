package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.schema.ValidationResults;
import org.sagebionetworks.web.client.jsinterop.EntityBadgeIconsProps;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SynapseContextProviderProps;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityBadgeView extends IsWidget, SupportsLazyLoadInterface {
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);

	void setClickHandler(ClickHandler handler);

	void showAddToDownloadList();

	void setSize(String s);

	void setMd5(String s);

	void setIcons(EntityBadgeIconsProps props, SynapseContextProviderProps contextProps);

	void setError(String error);

	void setPresenter(Presenter p);

	String getFriendlySize(Long contentSize, boolean b);

	void setModifiedByUserBadgeClickHandler(ClickHandler handler);

	void showMinimalColumnSet();

	void clearIcons();
	void clearEntityInformation();

	public interface Presenter {
		void onAddToDownloadList();
	}

}
