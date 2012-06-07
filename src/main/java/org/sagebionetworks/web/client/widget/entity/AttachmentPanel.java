package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
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
			attachmentBody = new HTML(SafeHtmlUtils.fromSafeConstant("No attachments"));
			this.setHeight(150);
		}else{
			UnorderedListPanel ulp = new UnorderedListPanel();
			
			for(AttachmentData data: attachments){
				SafeHtmlBuilder builder = new SafeHtmlBuilder();				
				SafeHtml safeName = SafeHtmlUtils.fromString(data.getName()); 
				builder.appendHtmlConstant("<a href=\"" + DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getTokenId(), data.getTokenId()) 
						+ "\" target=\"_blank\" name=\"" + safeName.asString() + "\">");
				SafeHtml iconNumber = SafeHtmlUtils.fromSafeConstant(DisplayUtils.getAttachmentIcon(data.getName()));
				builder.appendHtmlConstant("<div class=\"icon-white-small icon"+iconNumber.asString()+"-white\">");
				builder.appendHtmlConstant("<div style=\"margin-left:20px\">");
				builder.appendEscaped(DisplayUtils.replaceWhiteSpace(data.getName()));
				builder.appendHtmlConstant("</div>");
				builder.appendHtmlConstant("</div>");
				builder.appendHtmlConstant("</a>");
				Html listItem = new Html(builder.toSafeHtml().asString());
				ToolTipConfig config = new ToolTipConfig();
			    // If we have a preview then show it as a tooltip.
			    if(data.getPreviewId() != null){
			    	SafeHtml previewToolip = SafeHtmlUtils.fromSafeConstant("<div class=\"preview-image-loading\" >"
			    		+ DisplayUtils.IMAGE_CENTERING_TABLE_START
			    		+ "<img style=\"margin:auto; display:block;\" src=\"" 
			    		+ DisplayUtils.createAttachmentUrl(baseUrl, entityId, data.getPreviewId(), null) + "\"/>"
			    		+ DisplayUtils.IMAGE_CENTERING_TABLE_END
			    		+ "</div>");
			    	config.setText(previewToolip.asString());  
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
