package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.CloseHandler;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget.ManagementHandler;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextArea;

public interface MarkdownEditorWidgetView extends IsWidget,SynapseView {

	void configure(final WikiPageKey wikiKey, 
			WikiPageKey formattingGuideWikiPageKey,
			final TextArea markdownTextArea, 
			LayoutContainer formPanel,
			boolean showFieldLabel, 
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
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void showPreview(String descriptionMarkdown, final boolean isWiki);
	}
}
