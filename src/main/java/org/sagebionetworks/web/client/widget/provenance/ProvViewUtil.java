package org.sagebionetworks.web.client.widget.provenance;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView.Presenter;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.extjs.gxt.ui.client.Style.AnchorPosition;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ProvViewUtil {	
	
	private static final String PROV_ENTTITY_NODE_STYLE = "provEntityNode";
	private static final String PROV_ACTIVITY_NODE_STYLE = "provActivityNode";
	private static final String PROV_EXPAND_NODE_STYLE = "provExpandNode";

	private static final String PROV_ACTIVITY_LABEL_STYLE = "provActivityLabel";
	private static final String PROV_ACTIVITY_TIME_STYLE = "provActivityLabelTime";	
	private static final String PROV_VERSION_DISPLAY_STYLE = "provVersionDisplay";
	private static final String PROV_ACTIVITY_UNDEFINED_STYLE = "provUndefinedChange";
	
	private static final int ENTITY_LINE_NUMBER_CHARS = 17;
	private static final int MAX_TOOL_TIP_VALUE_CHAR = 43;	 	
	private static final int MAX_ACT_CODE_NAME_CHAR = 30;
	private static final int MAX_DISPLAY_NAME_CHAR = 13;
	private static MarginData ACT_MARGIN_USER = new MarginData(0, 0, 1, 10);
	private static MarginData ACT_MARGIN_TIME = new MarginData(0, 0, 3, 10);
	private static final MarginData ACT_MARGIN_NAME = new MarginData(5, 4, 8, 10);
		
	public static LayoutContainer createActivityContainer(ActivityGraphNode node, IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		LayoutContainer container = new LayoutContainer();
		container.setAutoHeight(true);
		container.setAutoWidth(true);
		container.setId(node.getId());
		container.setStyleName(PROV_ACTIVITY_NODE_STYLE);
		LayoutContainer label = new LayoutContainer();
		label.setStyleName(PROV_ACTIVITY_LABEL_STYLE);

		
		if(node.getType() == ActivityType.UNDEFINED) {		
			container.addStyleName(PROV_ACTIVITY_UNDEFINED_STYLE);						
			label.add(new HTML(DisplayConstants.UNDEFINED));
			container.add(label, ACT_MARGIN_NAME);
		} else {
			// display user profile and name if defined
			if(node.getActivityName() != null) {				
				label.add(new HTML(DisplayUtils.stubStrPartialWord(node.getActivityName(), MAX_ACT_CODE_NAME_CHAR)));
			}
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.setMaxNameLength(MAX_DISPLAY_NAME_CHAR);
			badge.configure(node.getModifiedBy());
			HTML time = new HTML(DisplayUtils.converDataToPrettyString(node.getModifiedOn()));
			time.addStyleName(PROV_ACTIVITY_TIME_STYLE);

			
			container.add(label, ACT_MARGIN_NAME);
			container.add(badge.asWidget(), ACT_MARGIN_USER);
			container.add(time, ACT_MARGIN_TIME);
		}
		
		setPosition(node, container);
		return container;		
	}
	
	public static LayoutContainer createEntityContainer(EntityGraphNode node, IconsImageBundle iconsImageBundle) {
		LayoutContainer container = createEntityContainer(node.getId(), node.getEntityId(),
				node.getName(), node.getVersionLabel(),
				node.getVersionNumber(), node.getEntityType(),
				iconsImageBundle);		
		setPosition(node, container);		
		return container;
	}
	
	public static LayoutContainer createExpandContainer(final ExpandGraphNode node, final SageImageBundle sageImageBundle, final Presenter presenter) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();		
		builder.appendHtmlConstant(AbstractImagePrototype.create(sageImageBundle.expand()).getHTML());
		
		final Anchor link = new Anchor();
		link.setHTML(builder.toSafeHtml());
		link.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				link.setHTML(AbstractImagePrototype.create(sageImageBundle.loading16()).getHTML());
				presenter.expand(node);
			}
		});
		
		LayoutContainer container = new LayoutContainer();
		container.setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE + " " + PROV_EXPAND_NODE_STYLE);
		container.add(link);
		container.layout();
		setPosition(node, container);
		return container;
	}	

	public static LayoutContainer createExternalUrlContainer(ExternalGraphNode node, IconsImageBundle iconsImageBundle) {
		if(node.getName() == null) node.setName("");
		if(node.getUrl() == null) node.setUrl("");
		
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		Anchor link = new Anchor();		
		link.setHref(node.getUrl());
		link.setTarget("_blank");

		// icon
		ImageResource icon;
		if(node.getUrl().contains("github.com")) icon = iconsImageBundle.github16();
		else if(node.getUrl().contains("genomespace.org")) icon = iconsImageBundle.genomespace16();
		else icon = iconsImageBundle.documentExternal16();		
		builder.appendHtmlConstant(AbstractImagePrototype.create(icon).getHTML());
		builder.appendHtmlConstant("<br/>");
		
		// name
		String stubName = DisplayUtils.stubStrPartialWord(node.getName(), ENTITY_LINE_NUMBER_CHARS);
		builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");
				
		link.setHTML(builder.toSafeHtml());

		LayoutContainer container = new LayoutContainer();
		if(node.getId() != null) container.setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE);
		container.add(link);
		container.layout();
		setPosition(node, container);
		return container;
	}


	/*
	 * Private utils
	 */
	private static void setPosition(ProvGraphNode node, LayoutContainer container) {
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
		String stubName = DisplayUtils.stubStrPartialWord(name, ENTITY_LINE_NUMBER_CHARS);
		if(stubName != null) builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");
		
		// version
		HTML versionHtml;
		if(versionNumber != null) {					
			String versionNumberStr = "v." + versionNumber;			
			String versionDisplay; 			
			if(versionNumber.toString().equals(versionLabel)) {
				versionDisplay = versionNumberStr;
			} else {
				versionDisplay = DisplayUtils.stubStrPartialWord(versionLabel + " (" + versionNumberStr + ")", ENTITY_LINE_NUMBER_CHARS-versionNumberStr.length());
			}			
			versionHtml = new HTML(SafeHtmlUtils.fromString(DisplayUtils.stubStrPartialWord(versionDisplay, ENTITY_LINE_NUMBER_CHARS-versionNumberStr.length())));
		} else {
			versionHtml = new HTML("");			
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

	public static SafeHtml createEntityPopoverHtml(KeyValueDisplay<String> kvDisplay) {
//		SafeHtmlBuilder sb = new SafeHtmlBuilder();
//		if(kvDisplay != null) {
//			Map<String,String> map = kvDisplay.getMap();
//			for(String key : kvDisplay.getKeyDisplayOrder()) {
//				String val = map.get(key);
//				if(val == null) val = "";
//				val = val.length() > MAX_TOOL_TIP_VALUE_CHAR ? val.substring(0, MAX_TOOL_TIP_VALUE_CHAR-3) + "..." : val;
//				sb.appendHtmlConstant("<span class=\"boldText\">")
//				.appendEscaped(key + ":")
//				.appendHtmlConstant("</span> ")
//				.appendEscaped(val)
//				.appendHtmlConstant("<br/>");
//			}
//		}		
//		return sb.toSafeHtml();
		String str = "";
		if(kvDisplay != null) {
			Map<String,String> map = kvDisplay.getMap();
			for(String key : kvDisplay.getKeyDisplayOrder()) {
				String val = map.get(key);
				if(val == null) val = "";
				val = val.length() > MAX_TOOL_TIP_VALUE_CHAR ? val.substring(0, MAX_TOOL_TIP_VALUE_CHAR-3) + "..." : val;
				str += key + ": " + val + "\n";
			}
		}		
		return SafeHtmlUtils.fromString(str);
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
