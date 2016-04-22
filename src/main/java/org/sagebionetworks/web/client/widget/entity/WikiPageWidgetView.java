package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils.MessagePopup;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiPageWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		public void configure(WikiPageKey inWikiKey, Boolean canEdit, Callback callback, boolean showSubpages);
		public void showPreview(final Long versionToPreview, Long currentVersion);
		void restoreConfirmed();
		void resetWikiMarkdown(String markdown);
		void reloadWikiPage();
		void showRestoreWarning(Long versionToRestore);
		public void restoreClicked();
		void configureWikiSubpagesWidget();
		void configureHistoryWidget(boolean canEdit);
		void configureBreadcrumbs(boolean isRootWiki, String ownerObjectName);
	}
	
	void setWikiHistoryWidget(IsWidget historyWidget);
	void setWikiSubpagesWidget(IsWidget historyWidget);
	void setWikiSubpagesWidgetVisible(boolean isVisible);
	void setWikiSubpagesContainers(WikiSubpagesWidget wikiSubpages);
	void showPopup(String title, String message, MessagePopup popupType,
			org.sagebionetworks.web.client.utils.Callback okCallback,
			org.sagebionetworks.web.client.utils.Callback cancelCallback);

	void setMarkdownWidget(IsWidget markdownWidget);
	void setBreadcrumbWidget(IsWidget breadcrumb);
	void setSynapseAlertWidget(IsWidget synapseAlert);
	void setWikiHeadingText(String title);
	void scrollWikiHeadingIntoView();
	public void setRestoreButtonVisible(boolean isVisible);
	public void setDiffVersionAlertVisible(boolean isVisible);
	public void setBreadcrumbsVisible(boolean isVisible);
	public void setModifiedCreatedByHistoryPanelVisible(boolean isVisible);
	public void setNoWikiCannotEditMessageVisible(boolean isVisible);
	public void setMarkdownVisible(boolean isVisible);
	public void setMainPanelVisible(boolean isVisible);
	public void setWikiHistoryVisible(boolean isVisible);
	public void setNoWikiCanEditMessageVisible(boolean b);
	void setLoadingVisible(boolean isVisible);
	public void showErrorMessage(String message);
	void showInfo(String title, String message);
	public void clear();
	void addStyleName(String style);
	public void setModifiedCreatedBy(IsWidget modifiedCreatedBy);
}
