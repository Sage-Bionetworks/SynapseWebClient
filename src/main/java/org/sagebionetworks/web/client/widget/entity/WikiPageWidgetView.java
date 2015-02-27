package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

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
		public void configure(WikiPageKey inWikiKey, Boolean canEdit, Callback callback, boolean isEmbeddedInOwnerPage);
		public void createPage(String name);
		public void deleteButtonClicked();
		public void saveClicked(String title, String md);
		public void cancelClicked();
		public void editClicked();
		public void previewClicked(final Long versionToPreview, Long currentVersion);
		public void restoreClicked(final Long wikiVersion);
		public void addFileHandles(List<String> fileHandleIds);
		public void removeFileHandles(List<String> fileHandleIds);
		public WikiPage getWikiPage();
	}
	
	public void configure(String markdown, WikiPageKey wikiKey, String ownerObjectName, Boolean canEdit, boolean isRootPage, boolean isDescription, boolean isCurrentVersion, Long versionInView, boolean isEmbeddedInOwnerPage);
	public void showCreateWiki(boolean isDescription);
	public void showWarningMessageInPage(String message);
	public void show404();
	public void show403();
	
	public void showWikiHistory(boolean isVisible);
	public void showCreatedBy(boolean isVisible);
	public void showModifiedBy(boolean isVisible);
}
