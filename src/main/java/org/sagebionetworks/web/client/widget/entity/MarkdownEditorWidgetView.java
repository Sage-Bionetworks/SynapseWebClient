package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface MarkdownEditorWidgetView extends IsWidget,SynapseView {

	void configure(final WikiPageKey wikiKey, 
			WikiPageKey formattingGuideWikiPageKey,
			String markdown, 
			final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback,
			final CloseHandler saveHandler,
			final ManagementHandler managementHandler);
	
	void deleteMarkdown(String md);
	void insertMarkdown(String md);
	
	void showPreviewHTML(String result, boolean isWiki) throws JSONObjectAdapterException;
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	String getMarkdown();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void showPreview(String descriptionMarkdown, final boolean isWiki);
	}
}
