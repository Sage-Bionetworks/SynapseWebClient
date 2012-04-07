package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * This panel is a simple disposable attachments widget.
 * @author John
 *
 */
public class AttachmentPanel extends ContentPanel {
	
	List<AttachmentData> attachments;
	String baseUrl;
	String entityId;
	
	public AttachmentPanel(String baseUrl, String entityId, List<AttachmentData> attachments){
		this.baseUrl = baseUrl;
		this.entityId = entityId;
		this.attachments = attachments;
	}

	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		setHeaderVisible(false);
		Widget attachmentBody = null;
		if(attachments == null){
			attachmentBody = new Html("No attachments");
			this.setHeight(150);
		}else{
			UnorderedListPanel ulp = new UnorderedListPanel();
			
			for(AttachmentData data: attachments){
				StringBuilder builder = new StringBuilder();
				builder.append("<a href=\"");
				builder.append(DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getTokenId(), data.getTokenId()));
				builder.append("\" target=\"_blank\" name=\"");
				builder.append(data.getName());
				builder.append("\"");
				builder.append(">");
				String iconNumber = DisplayUtils.getAttachmentIcon(data.getName());
				builder.append("<div class=\"icon-white-small icon"+iconNumber+"-white\">");
				builder.append("<div style=\"margin-left:20px\">");
				builder.append(DisplayUtils.replaceWhiteSpace(data.getName()));
				builder.append("</div>");
				builder.append("</div>");
				builder.append("</a>");
				Html listItem = new Html(builder.toString());
				ToolTipConfig config = new ToolTipConfig();  
//			    config.setTitle("Information");
			    // If we have a preview then show it as a tooltip.
			    if(data.getPreviewId() != null){
			    	StringBuilder imageBuilder = new StringBuilder();
			    	imageBuilder.append("<div style=\"width:160px; height:160px; float:center;\">");
			    	imageBuilder.append("<img style=\"margin:auto; display:block;\" src=\"");
			    	imageBuilder.append(DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getPreviewId(), null));
			    	imageBuilder.append("\"");
			    	imageBuilder.append(" alt=\"Downloading image...\"");
			    	imageBuilder.append("/>");
			    	imageBuilder.append("</div>");
			    	config.setText(imageBuilder.toString());  
			    }else{
				    config.setText(data.getName());  
			    }
			    config.setMouseOffset(new int[] {0, 0});  
			    config.setAnchor("left");  
			    listItem.setToolTip(config); 
				ulp.add(listItem);
			}
			attachmentBody = ulp;
		}
		this.add(attachmentBody, new MarginData(5));
	}

	
}
