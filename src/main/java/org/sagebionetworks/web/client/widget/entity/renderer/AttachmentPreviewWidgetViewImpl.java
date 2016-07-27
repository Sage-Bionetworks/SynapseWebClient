package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentPreviewWidgetViewImpl extends FlowPanel implements AttachmentPreviewWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private AuthenticationController authController;
	@Inject
	public AttachmentPreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils,
			AuthenticationController authController) {
		this.synapseJsniUtils= synapseJsniUtils;
		this.authController = authController;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName) {
		this.clear();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		this.setStyleName("displayInline");
		StringBuilder sb = new StringBuilder();
		sb.append("<a class=\"link\" href=\"");
		sb.append(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false, authController.getCurrentXsrfToken()));
		sb.append("\">");
		int lastDotIndex = fileName.lastIndexOf(".");
		boolean isPreviewed = false;
		if (lastDotIndex > -1) {
			String extension = fileName.substring(lastDotIndex+1);
			if (DisplayUtils.isRecognizedImageContentType("image/"+extension)) {
				sb.append("<img class=\"imageDescriptor\" ");
				sb.append(" src=\"");
				sb.append(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName, true, authController.getCurrentXsrfToken()));
				sb.append("\"></img>");
				isPreviewed = true;
			}
		}
		if (!isPreviewed){
			sb.append(fileName);
		}
		sb.append("</a>");
		HTMLPanel htmlPanel = new HTMLPanel(sb.toString());
		htmlPanel.setStyleName("displayInline");
		add(htmlPanel);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}
