package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends LayoutContainer implements ImageWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	
	@Inject
	public ImageWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils) {
		this.synapseJsniUtils = synapseJsniUtils;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName,
			String explicitWidth, String alignment) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		StringBuilder sb = new StringBuilder();
		sb.append("<img class=\"imageDescriptor\" ");
		if (explicitWidth != null && explicitWidth.trim().length() > 0) {
			sb.append(" width=\"");
			sb.append(explicitWidth);
			sb.append("\"");
		}
		if (alignment != null && alignment.trim().length() > 0) {
			sb.append(" align=\"");
			sb.append(alignment);
			sb.append("\" style=\"margin:10px;\"");
		}
		
		sb.append(" src=\"");
		sb.append(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false));
		sb.append("\"></img>");
		
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
