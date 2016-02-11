package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface WikiMarkdownEditorView extends IsWidget,SynapseView {
	
	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void setSaving(boolean isSaving);
	void setTitleEditorVisible(boolean visible);
	String getTitle();
	void setTitle(String title);
	
	void showEditorModal();
	void hideEditorModal();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void cancelClicked();
		void deleteClicked();
		void saveClicked();
		void previewClicked();
	}
	void confirm(String string, ConfirmCallback confirmCallback);
	void setMarkdownPreviewWidget(Widget markdownPreviewWidget);
	void showPreviewModal();
	void setMarkdownEditorWidget(Widget markdownEditorWidget);
}
