package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
public interface ImageConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * True if user just wants to insert a reference to an image from the web
	 * @return
	 */
	public boolean isExternal();
	public boolean isSynapseEntity();
	
	public String getImageUrl();
	public String getAltText();
	public void setImageUrl(String url);
	void setFileInputWidget(Widget fileInputWidget);
	void showUploadFailureUI(String error);
	void showUploadSuccessUI();
	void setUploadButtonEnabled(boolean enabled);
	public String getAlignment();
	public String getSynapseId();
	public void setExternalVisible(boolean visible);
	
	public void configure(WikiPageKey wikiKey, DialogCallback dialogCallback);
	
	public void setSynapseId(String synapseId);
	public void setAlignment(String alignment);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addFileHandleId(String fileHandleId);
		void uploadFileClicked();
	}
}
