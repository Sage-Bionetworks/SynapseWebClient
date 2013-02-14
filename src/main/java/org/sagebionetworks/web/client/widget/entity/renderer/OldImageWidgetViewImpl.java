package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OldImageWidgetViewImpl extends LayoutContainer implements OldImageWidgetView {

	private Presenter presenter;
	
	@Inject
	public OldImageWidgetViewImpl() {
	}
	
	@Override
	public void configure(String entityId, AttachmentData uploadedAttachmentData, String explicitWidth) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		String attachmentBaseUrl = GWT.getModuleBaseURL()+"attachment";
		StringBuilder sb = new StringBuilder();
		sb.append("<img class=\"imageDescriptor\" ");
		if (explicitWidth != null && explicitWidth.trim().length() > 0) {
			sb.append(" width=\"");
			sb.append(explicitWidth);
			sb.append("\"");
		}
			
		sb.append(" src=\"");
		sb.append(createAttachmentUrl(attachmentBaseUrl, entityId, uploadedAttachmentData.getTokenId(), DisplayUtils.ENTITY_PARAM_KEY));
		sb.append("\"></img>");
		
		add(new HTMLPanel(sb.toString()));
		this.layout(true);
	}
	
	/**
	 * Create the url to an attachment image.
	 * @param baseURl
	 * @param id
	 * @param tokenId
	 * @param fileName
	 * @return
	 */
	public static String createAttachmentUrl(String baseURl, String id, String tokenId, String paramKey){
	        StringBuilder builder = new StringBuilder();
	        builder.append(baseURl);
	        builder.append("?"+paramKey+"=");
	        builder.append(id);
	        builder.append("&"+DisplayUtils.TOKEN_ID_PARAM_KEY+"=");
	        builder.append(tokenId);
	        builder.append("&"+DisplayUtils.WAIT_FOR_URL+"=true");
	        return builder.toString();
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
