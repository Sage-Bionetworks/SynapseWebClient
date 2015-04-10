package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;

public interface MarkdownEditorWidgetView extends IsWidget,SynapseView {

	void configure(WikiPageKey formattingGuideWikiPageKey,
			String markdown);
	
	void showPreviewHTML(String result, WikiPageKey wikiKey, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException;
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	String getMarkdown();
	void setMarkdown(String markdown);
	int getCursorPos();
	void setCursorPos(int pos);
	void setMarkdownFocus();
	int getSelectionLength();
	void setSelectionRange(int pos, int length);
	
	void setEditButtonEnabled(boolean enabled);
	void setSaving(boolean isSaving);
	
	void setDeleteVisible(boolean visible);
	public void setAttachmentCommandsVisible(boolean visible);
	void setSaveVisible(boolean visible);
	void setCancelVisible(boolean visible);
	void setAlphaCommandsVisible(boolean visible);
	void setTitleEditorVisible(boolean visible);
	String getTitle();
	void setTitle(String title);
	
	void showEditorModal();
	void hideEditorModal();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void handleCommand(MarkdownEditorAction action);
		void markdownEditorClicked();
	}
}
