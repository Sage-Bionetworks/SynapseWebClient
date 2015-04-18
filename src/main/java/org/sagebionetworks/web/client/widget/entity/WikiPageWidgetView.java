package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
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
		public void previewClicked(final Long versionToPreview, Long currentVersion);
		public void restoreClicked(final Long wikiVersion);
		public WikiPage getWikiPage();
		public CallbackP<WikiPageKey> getReloadWikiPageCallback();
	}
	
	public void configure(String markdown, WikiPageKey wikiKey, String ownerObjectName, Boolean canEdit, boolean isRootPage, boolean isDescription, boolean isCurrentVersion, Long versionInView, boolean isEmbeddedInOwnerPage);
	public void showWarningMessageInPage(String message);
	public void show404();
	public void show403();
	
	public void showWikiHistory(boolean isVisible);
	public void showCreatedBy(boolean isVisible);
	public void showModifiedBy(boolean isVisible);
}
