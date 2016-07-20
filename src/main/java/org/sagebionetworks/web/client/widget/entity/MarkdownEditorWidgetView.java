package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface MarkdownEditorWidgetView extends IsWidget,SynapseView {
	
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
	public void setAttachmentCommandsVisible(boolean visible);
	void setAlphaCommandsVisible(boolean visible);
	boolean isEditorAttachedAndVisible();
	int getClientHeight();
	void setMarkdownTextAreaHeight(int heightPx);
	void setFocus(boolean focused);
	void setEditorEnabled(boolean enabled);
	void setMarkdownPreviewWidget(Widget markdownPreviewWidget);
	void showPreview();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void handleCommand(MarkdownEditorAction action);
		void markdownEditorClicked();
		void onKeyPress(char c);
	}
	
	void addTextAreaKeyUpHandler(KeyUpHandler keyUpHandler);

	void addTextAreaClickHandler(ClickHandler clickHandler);

	void confirm(String string, ConfirmCallback confirmCallback);

	void setFormattingGuideWidget(Widget formattingGuideWidget);

	void configure(String markdown);

	void setImageCommandsVisible(boolean visible);

	void setVideoCommandsVisible(boolean visible);

	void setExternalImageButtonVisible(boolean visible);
}
