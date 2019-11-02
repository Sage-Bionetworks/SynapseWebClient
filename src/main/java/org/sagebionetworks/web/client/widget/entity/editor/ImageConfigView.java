package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.widget.WidgetEditorView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ImageConfigView extends IsWidget, WidgetEditorView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * True if user just wants to insert a reference to an image from the web
	 * 
	 * @return
	 */
	public boolean isExternal();

	public boolean isSynapseEntity();

	public String getImageUrl();

	public String getExternalAltText();

	public void setImageUrl(String url);

	void setFileInputWidget(Widget fileInputWidget);

	void setWikiAttachmentsWidget(Widget widget);

	void setWikiAttachmentsWidgetVisible(boolean visible);

	void showUploadFailureUI(String error);

	void showUploadSuccessUI(String fileName);

	public String getAlignment();

	public Integer getScale();

	public void setScale(Integer scale);

	String getAltText();

	void setAltText(String altText);

	public String getSynapseId();

	public Long getVersion();

	public void setSynapseId(String synapseId);

	public void setAlignment(String alignment);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

	void setWikiFilesTabVisible(boolean visible);

	void setExternalTabVisible(boolean visible);

	void setSynapseTabVisible(boolean visible);

	void showWikiFilesTab();

	void showExternalTab();

	void showSynapseTab();
}
