package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.WikiUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentPreviewWidgetViewImpl extends LayoutContainer implements AttachmentPreviewWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	@Inject
	public AttachmentPreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils) {
		this.synapseJsniUtils= synapseJsniUtils;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		StringBuilder sb = new StringBuilder();
		sb.append("<a class=\"link\" href=\"");
		sb.append(WikiUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false));
		sb.append("\">");
		int lastDotIndex = fileName.lastIndexOf(".");
		boolean isPreviewed = false;
		if (lastDotIndex > -1) {
			String extension = fileName.substring(lastDotIndex+1);
			if (DisplayUtils.isRecognizedImageContentType("image/"+extension)) {
				sb.append("<img class=\"imageDescriptor\" ");
				sb.append(" src=\"");
				sb.append(WikiUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName, true));
				sb.append("\"></img>");
				isPreviewed = true;
			}
		}
		if (!isPreviewed){
			sb.append(fileName);
		}
		sb.append("</a>");
	
		add(new HTMLPanel(sb.toString()));
		this.layout(true);
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
