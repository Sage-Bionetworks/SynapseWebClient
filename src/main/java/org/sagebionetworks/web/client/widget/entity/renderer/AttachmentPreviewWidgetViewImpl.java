package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentPreviewWidgetViewImpl extends LayoutContainer implements AttachmentPreviewWidgetView {

	private Presenter presenter;
	
	@Inject
	public AttachmentPreviewWidgetViewImpl() {
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		StringBuilder sb = new StringBuilder();
		
		sb.append("<a href=\"");
		sb.append(ImageWidgetViewImpl.createWikiAttachmentUrl(wikiKey, fileName,false));
		sb.append("\"><img class=\"imageDescriptor\" ");
		sb.append(" src=\"");
		sb.append(ImageWidgetViewImpl.createWikiAttachmentUrl(wikiKey, fileName, true));
		sb.append("\"></img></a>");
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
