package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
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
		public void showPreview(final Long versionToPreview, Long currentVersion);
		void configureHistoryWidget(WikiPageKey wikiKey, boolean canEdit);
		void configureWikiSubpagesWidget(WikiPageKey wikiKey,
				boolean isEmbeddedInOwnerPage);
		void configureBreadcrumbs(WikiPageKey wikiKey, boolean isRootWiki,
				String ownerObjectName);
		void restoreConfirmed();
		void resetWikiMarkdown(String markdown);
		void configureCreatedModifiedBy();
		void reloadWikiPage();
		void showRestoreWarning(Long versionToRestore);
		public void restoreClicked();
	}
	
	public void showNoteInPage(String message);
	public void show404();
	public void show403();
	void setWikiHistoryWidget(IsWidget historyWidget);
	void setWikiSubpagesWidget(IsWidget historyWidget);
	void setWikiSubpagesContainers(WikiSubpagesWidget wikiSubpages);
	void showPopup(String title, String message, MessagePopup popupType,
			org.sagebionetworks.web.client.utils.Callback okCallback,
			org.sagebionetworks.web.client.utils.Callback cancelCallback);
	void hideLoading();
	void showCreatedBy(boolean isVisible);
	void showModifiedBy(boolean isVisible);
	void showWikiHistory(boolean isVisible);
	void setMarkdownWidget(IsWidget markdownWidget);
	void setBreadcrumbWidget(IsWidget breadcrumb);
	void setSynapseAlertWidget(IsWidget synapseAlert);
	void showDiffVersionAlert();
	public void showRestoreButton();
	void setModifiedByBadge(IsWidget modifiedByUserBadge);
	void setModifiedByText(String modifiedByText);
	void setCreatedByBadge(IsWidget createdByUserBadge);
	void setCreatedByText(String createdByText);
	void hideDiffVersionAlert();
}
