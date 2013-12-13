package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiPageWidgetView extends IsWidget, SynapseView {

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
		public void createPage(String name);
		public void deleteButtonClicked();
		public void refreshWikiAttachments(final String updatedTitle, final String updatedMarkdown, final Callback pageUpdatedCallback);
		public void saveClicked(String title, String md);
		public void cancelClicked();
		public void editClicked();
		public void previewClicked(Long wikiVersion);
		public void restoreClicked(final Long wikiVersion);
	}
	
	public void configure(WikiPage newPage, WikiPageKey wikiKey, String ownerObjectName, Boolean canEdit, boolean isEmbeddedInOwnerPage, int spanWidth, boolean isDescription);
	public void showNoWikiAvailableUI(boolean isDescription);
	public void show404();
	public void show403();
	public void updateWikiPage(WikiPage newPage);
}
