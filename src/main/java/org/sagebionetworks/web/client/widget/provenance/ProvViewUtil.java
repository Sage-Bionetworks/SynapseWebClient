package org.sagebionetworks.web.client.widget.provenance;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
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
	private static final int MAX_TOOL_TIP_VALUE_CHAR = 300;	 	
	private static final int MAX_ACT_CODE_NAME_CHAR = 30;
	private static final int MAX_DISPLAY_NAME_CHAR = 13;
	private static String ACT_MARGIN_USER = "margin-bottom-1 margin-left-10";
	private static String ACT_MARGIN_TIME = "margin-bottom-3 margin-left-10";
	private static final String ACT_MARGIN_NAME = "margin-top-5 margin-right-4 margin-bottom-10 margin-left-10";
	
	public static ProvNodeContainer createActivityContainer(ActivityGraphNode node, IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		ProvNodeContainer container = new ProvNodeContainer();
		container.getElement().setId(node.getId());
		container.addStyleName(PROV_ACTIVITY_NODE_STYLE);
		FlowPanel label = new FlowPanel();
		label.addStyleName(PROV_ACTIVITY_LABEL_STYLE);

		
		if(node.getType() == ActivityType.UNDEFINED) {		
			container.addStyleName(PROV_ACTIVITY_UNDEFINED_STYLE);						
			label.add(new HTML(DisplayConstants.UNDEFINED));
			label.addStyleName(ACT_MARGIN_NAME);
			container.add(label);
		} else {
			// display user profile and name if defined
			if(node.getActivityName() != null) {				
				label.add(new HTML(DisplayUtils.stubStrPartialWord(node.getActivityName(), MAX_ACT_CODE_NAME_CHAR)));
			}
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.setMaxNameLength(MAX_DISPLAY_NAME_CHAR);
			badge.configure(node.getModifiedBy());
			HTML time = new HTML(DisplayUtils.convertDataToPrettyString(node.getModifiedOn()));
			time.addStyleName(PROV_ACTIVITY_TIME_STYLE);

			FlowPanel content = new FlowPanel();
			label.addStyleName(ACT_MARGIN_NAME);
			content.add(label);
			Widget userBadgeWidget = badge.asWidget();
			userBadgeWidget.addStyleName(ACT_MARGIN_USER);
			content.add(userBadgeWidget);
			time.addStyleName(ACT_MARGIN_TIME);
			content.add(time);
			container.addContent(content);
		}
		
		setPosition(node, container);
		return container;		
	}
	
	public static ProvNodeContainer createEntityContainer(EntityGraphNode node, IconsImageBundle iconsImageBundle) {
		ProvNodeContainer container = createEntityContainer(node.getId(), node.getEntityId(),
				node.getName(), node.getVersionLabel(),
				node.getVersionNumber(), node.getEntityType(),
				iconsImageBundle);		
		setPosition(node, container);		
		return container;
	}
	
	public static ProvNodeContainer createExpandContainer(final ExpandGraphNode node, final SageImageBundle sageImageBundle, final Presenter presenter, final ProvenanceWidgetView view) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();		
		builder.appendHtmlConstant(AbstractImagePrototype.create(sageImageBundle.expand()).getHTML());
		
		final Anchor link = new Anchor();
		link.setHTML(builder.toSafeHtml());
		link.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				view.setBlockCloseFullscreen(true);
				link.setHTML(AbstractImagePrototype.create(sageImageBundle.loading16()).getHTML());
				presenter.expand(node);
			}
		});
		
		ProvNodeContainer container = new ProvNodeContainer();
		container.getElement().setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE + " " + PROV_EXPAND_NODE_STYLE);
		container.add(link);
		setPosition(node, container);
		return container;
	}	

	public static ProvNodeContainer createExternalUrlContainer(ExternalGraphNode node, IconsImageBundle iconsImageBundle) {
		if(node.getName() == null) node.setName("");
		if(node.getUrl() == null) node.setUrl("");
		
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		Anchor link = new Anchor();		
		link.setHref(node.getUrl());
		link.setTarget("_blank");

		// icon
		ImageResource icon;
		if(node.getUrl().contains("github.com") || node.getUrl().contains("githubusercontent.com")) icon = iconsImageBundle.github16();
		else if(node.getUrl().contains("genomespace.org")) icon = iconsImageBundle.genomespace16();
		else icon = iconsImageBundle.documentExternal16();		
		builder.appendHtmlConstant(AbstractImagePrototype.create(icon).getHTML());
		builder.appendHtmlConstant("<br/>");
		
		// name
		String stubName = DisplayUtils.stubStrPartialWord(node.getName(), ENTITY_LINE_NUMBER_CHARS);
		builder.appendEscaped(stubName);
		builder.appendHtmlConstant("<br/>");
				
		link.setHTML(builder.toSafeHtml());

		ProvNodeContainer container = new ProvNodeContainer();
		if(node.getId() != null) container.getElement().setId(node.getId());
		container.setStyleName(PROV_ENTTITY_NODE_STYLE);
		container.addContent(link);
		setPosition(node, container);
		return container;
	}


	/*
	 * Private utils
	 */
	private static void setPosition(ProvGraphNode node, FlowPanel container) {
		container.getElement().getStyle().setTop(node.getyPos(), Unit.PX);
		container.getElement().getStyle().setLeft(node.getxPos(), Unit.PX);
	}
	
	private static ProvNodeContainer createEntityContainer(String id, String entityId, String name, String versionLabel, Long versionNumber, String entityType, IconsImageBundle iconsImageBundle) {
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

		ProvNodeContainer node = new ProvNodeContainer();
		if(id != null) node.getElement().setId(id);
		node.setStyleName(PROV_ENTTITY_NODE_STYLE);
		node.addContent(link);
		return node;
	}

	public static SafeHtml createEntityPopoverHtml(KeyValueDisplay<String> kvDisplay) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		if(kvDisplay != null) {
			Map<String,String> map = kvDisplay.getMap();
			for(String key : kvDisplay.getKeyDisplayOrder()) {
				if (!DisplayConstants.DESCRIPTION.equalsIgnoreCase(key)) {
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
		}
		return sb.toSafeHtml();
	}

	/**
	 * Create a closable popover
	 * @param title
	 * @param text
	 * @param widget
	 */
	public static void createMessageConfig(String title, String text, Widget widget) {
		Tooltip tip = new Tooltip(widget);
		tip.setIsHtml(true);
		tip.setTitle(text);
		tip.setPlacement(Placement.BOTTOM);
		tip.setShowDelayMs(200);
	}

}
