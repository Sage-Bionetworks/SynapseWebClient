package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiPageWidgetView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public void configure(WikiPageKey inWikiKey, Boolean canEdit, Callback callback, boolean isEmbeddedInOwnerPage, int spanWidth);
		public void insertButtonClicked(boolean isFirstPage);
		public void deleteButtonClicked();
		public void refreshWikiAttachments(final String updatedTitle, final String updatedMarkdown, final Callback pageUpdatedCallback);
		public void saveClicked(String title, String md);
		public void cancelClicked();
	}
	
	public void configure(WikiPage newPage, WikiPageKey wikiKey, String ownerObjectName, Boolean canEdit, boolean isEmbeddedInOwnerPage, int spanWidth);
	public void showNoWikiAvailableUI();
	public void updateWikiPage(WikiPage newPage);
}
