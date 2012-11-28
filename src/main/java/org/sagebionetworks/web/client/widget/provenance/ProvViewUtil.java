package org.sagebionetworks.web.client.widget.provenance;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.provenance.ActivityTreeNode;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ExpandTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.extjs.gxt.ui.client.Style.AnchorPosition;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;

public class ProvViewUtil {	
	
	private static final String PROV_ENTTITY_NODE_STYLE = "provEntityNode";
	private static final String PROV_ACTIVITY_NODE_STYLE = "provActivityNode";
	private static final String PROV_EXPAND_NODE_STYLE = "provExpandNode";

	private static final String PROV_ACTIVITY_LABEL_STYLE = "provActivityLabel";
	private static final String PROV_ACTIVITY_UNDEFINED_ICON_STYLE = "provActivityUndefinedIcon";
	private static final String PROV_VERSION_DISPLAY_STYLE = "provVersionDisplay";
	private static final String PROV_ACTIVITY_MANUAL_STYLE = "provManualChange";
	private static final String PROV_ACTIVITY_UNDEFINED_STYLE = "provUndefinedChange";
	
	private static final int ENTITY_LINE_NUMBER_CHARS = 17;
	private static final int MAX_TOOL_TIP_VALUE_CHAR = 43;	 	
	private static final int MAX_ACT_CODE_NAME_CHAR = 17;
	private static final int MAX_ACT_MANUAL_NAME_CHAR = 17;
	private static LayoutContainer UNDEFINED_SUB_NODE;
		
	public static LayoutContainer createActivityContainer(ActivityTreeNode node, IconsImageBundle iconsImageBundle) {
		LayoutContainer container = new LayoutContainer();
		container.setId(node.getId());
		container.setStyleName(PROV_ACTIVITY_NODE_STYLE);
		LayoutContainer label = new LayoutContainer();
		label.setStyleName(PROV_ACTIVITY_LABEL_STYLE);
		HTML activityLabel = null;
		LayoutContainer subNode = null;
		
		switch(node.getType()) {
		case CODE_EXECUTION:			
			activityLabel = new HTML(node.getActivityName() != null ? stubEntityString(node.getActivityName(), MAX_ACT_CODE_NAME_CHAR) : DisplayConstants.CODE_EXECUTION);
			subNode = createEntityContainer(null, node.getSubEntityId(), node.getSubName(),
					node.getSubVersionLabel(),
					node.getSubVersionNumber(),
					node.getSubEntityType(), iconsImageBundle);		
			break;
		case MANUAL:
			container.addStyleName(PROV_ACTIVITY_MANUAL_STYLE); 
			activityLabel = new HTML(node.getActivityName() != null ? stubEntityString(node.getActivityName(), MAX_ACT_CODE_NAME_CHAR) : DisplayConstants.MANUAL);
			break;
		case UNDEFINED:
			container.addStyleName(PROV_ACTIVITY_UNDEFINED_STYLE);			
			activityLabel = new HTML(DisplayConstants.UNDEFINED);
			subNode = getUndefinedSubNode(iconsImageBundle);
			break;
		default:
				break;
		}
		label.add(activityLabel);
		container.add(label);
		
		if(subNode != null) {
			container.add(subNode);
		}
		
		setPosition(node, container);
		return container;		
	}
	
	public static LayoutContainer createEntityContainer(EntityTreeNode node, IconsImageBundle iconsImageBundle) {
		LayoutContainer container = createEntityContainer(node.getId(), node.getEntityId(),
				node.getName(), node.getVersionLabel(),
				node.getVersionNumber(), node.getEntityType(),
				iconsImageBundle);		
		setPosition(node, container);		
		return container;
	}
	
	public static LayoutContainer createExpandContainer(ExpandTreeNode node, SageImageBundle sageImageBundle) {
		LayoutContainer container = new LayoutContainer();
		container.setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE + " " + PROV_EXPAND_NODE_STYLE);
		container.add(new HTML(AbstractImagePrototype.create(sageImageBundle.expand()).getHTML()));
		setPosition(node, container);
		return container;
	}	


	/*
	 * Private utils
	 */
	private static void setPosition(ProvTreeNode node, LayoutContainer container) {
		container.setStyleAttribute("top", node.getyPos() + "px");
		container.setStyleAttribute("left", node.getxPos() + "px");
	}
	
	private static LayoutContainer createEntityContainer(String id, String entityId, String name, String versionLabel, Long versionNumber, String entityType, IconsImageBundle iconsImageBundle) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		Anchor link = new Anchor();
		if(versionNumber != null) {
			link.setHref(DisplayUtils.getSynapseHistoryToken(entityId, versionNumber));
		} else {
			link.setHref(DisplayUtils.getSynapseHistoryToken(entityId));
		}
			
		
		// icon
		ImageResource icon = DisplayUtils.getSynapseIconForEntityClassName(entityType, IconSize.PX16, iconsImageBundle);
		builder.appendHtmlConstant(AbstractImagePrototype.create(icon).getHTML());
		builder.appendHtmlConstant("<br/>");
		
		// name
		String stubName = stubEntityString(name, ENTITY_LINE_NUMBER_CHARS);
		builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");
		
		// version
		HTML versionHtml;
		if(versionNumber != null) {			
			String versionNumberStr = " (#" + versionNumber + ")";
			String versionLabelStub = stubEntityString(versionLabel, ENTITY_LINE_NUMBER_CHARS-versionNumberStr.length());			
			versionHtml = new HTML(SafeHtmlUtils.fromString(versionLabelStub + versionNumberStr));
		} else {
			versionHtml = new HTML(DisplayConstants.NOT_VERSIONED);			
		}
		versionHtml.setStyleName(PROV_VERSION_DISPLAY_STYLE);
		builder.appendHtmlConstant(versionHtml.toString());		
		
		link.setHTML(builder.toSafeHtml());

		LayoutContainer node = new LayoutContainer();
		if(id != null) node.setId(id);
		node.setStyleName(PROV_ENTTITY_NODE_STYLE);
		node.add(link);
		node.layout();
		return node;
	}

	private static String stubEntityString(String contents, int maxLength) {
		String stub = contents;
		if(contents != null && contents.length() > maxLength) {
			stub = contents.substring(0, maxLength-3);
			stub += "...";
		}
		return stub; 
	}

	private static LayoutContainer getUndefinedSubNode(IconsImageBundle iconsImageBundle) {
		if (UNDEFINED_SUB_NODE == null) {
			UNDEFINED_SUB_NODE = new LayoutContainer();
			UNDEFINED_SUB_NODE.setStyleName(PROV_ACTIVITY_UNDEFINED_ICON_STYLE);
			UNDEFINED_SUB_NODE.add(new HTML(AbstractImagePrototype.create(iconsImageBundle.warning16()).getHTML()));			
		}
		return UNDEFINED_SUB_NODE;
	}

	public static SafeHtml createEntityPopoverHtml(KeyValueDisplay<String> kvDisplay) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		if(kvDisplay != null) {
			Map<String,String> map = kvDisplay.getMap();
			for(String key : kvDisplay.getKeyDisplayOrder()) {
				String val = map.get(key);
				if(val == null) val = "";
				val = val.length() > MAX_TOOL_TIP_VALUE_CHAR ? val.substring(0, MAX_TOOL_TIP_VALUE_CHAR-3) + "..." : val;
				sb.appendHtmlConstant("<span class=\"boldText\">")
				.appendEscaped(key + ":")
				.appendHtmlConstant("</span> ")
				.appendEscaped(val)
				.appendHtmlConstant("<br/>");
			}
		}		
		return sb.toSafeHtml();
	}

	public static ToolTipConfig createTooltipConfig(String title, String text) {
		ToolTipConfig config = new ToolTipConfig();
	    config.setTitle(title);
	    config.setText(text);
	    config.setMouseOffset(new int[] {0,0});
	    config.setAnchor(AnchorPosition.RIGHT.toString());	   
	    config.setShowDelay(200);
	    config.setDismissDelay(10000);	    
		return config;
	}

	public static ToolTipConfig createMessageConfig(String title, String text) {
		ToolTipConfig config = new ToolTipConfig();
	    config.setTitle(title);
	    config.setText(text);
	    config.setAnchor(AnchorPosition.BOTTOM.toString());	    	    
	    config.setDismissDelay(0);
	    config.setShowDelay(200);
	    config.setCloseable(true);
		return config;
	}

}
