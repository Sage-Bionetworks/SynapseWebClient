package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

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
		public void previewClicked(final Long versionToPreview, Long currentVersion);
		public WikiPage getWikiPage();
		public CallbackP<WikiPageKey> getReloadWikiPageCallback();
		public void toggleHistory();
		void configureHistoryWidget(WikiPageKey wikiKey, boolean canEdit);
		void configureWikiSubpagesWidget(WikiPageKey wikiKey,
				boolean isEmbeddedInOwnerPage);
		void configureBreadcrumbs(WikiPageKey wikiKey, boolean isRootWiki,
				String ownerObjectName);
		void restoreConfirmed();
		void restoreClicked();
		void reloadWikiPage();
		void resetWikiMarkdown(String markdown);
	}
	
	public void configure(String markdown, WikiPageKey wikiKey, String ownerObjectName, Boolean canEdit, boolean isRootPage, boolean isCurrentVersion, Long versionInView, boolean isEmbeddedInOwnerPage);
	public void showNoteInPage(String message);
	public void show404();
	public void show403();

	public void setSynapseAlertWidget(Widget asWidget);
	void setWikiHistoryWidget(IsWidget historyWidget);
	void setWikiSubpagesWidget(IsWidget historyWidget);
	void setHistoryToggleButtonText(String text);
	void setWikiSubpagesContainers(WikiSubpagesWidget wikiSubpages);
	void showPopup(String title, String message, MessagePopup popupType,
			org.sagebionetworks.web.client.utils.Callback okCallback,
			org.sagebionetworks.web.client.utils.Callback cancelCallback);
	void hideLoading();
	void showCreatedBy(boolean isVisible);
	void showModifiedBy(boolean isVisible);
	void showWikiHistory(boolean isVisible);
}
