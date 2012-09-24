package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EntityViewUtils {

	public static Widget createTitleWidget(
			EntityBundle bundle, 
			Widget restrictionsWidget,
			String entityTypeDisplay, 
			IconsImageBundle iconsImageBundle, 
			boolean canEdit, 
			boolean readOnly,
			SynapseJSNIUtils synapseJSNIUtils) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h2>")
		.appendHtmlConstant(AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(bundle.getEntity(), DisplayUtils.IconSize.PX24, iconsImageBundle)).getHTML()) 
		.appendHtmlConstant("&nbsp;")
		.appendEscaped(bundle.getEntity().getName())
		.appendHtmlConstant("&nbsp;(")
		.appendEscaped(bundle.getEntity().getId())
		.appendHtmlConstant(")")		
		.appendHtmlConstant("</h2>");		
		lc.add(new HTML(shb.toSafeHtml()));  
		
	    // Metadata
	    lc.add(createMetadata(bundle.getEntity()));
	    // the headers for description and property
	   	if (restrictionsWidget!=null) lc.add(restrictionsWidget);  
		if(canEdit && readOnly) {
			HTML roContainer = new HTML("<h4 class=\"colored\"> " + DisplayConstants.READ_ONLY + " " +AbstractImagePrototype.create(iconsImageBundle.help16()).getHTML() + "</h4>");
			roContainer.setWidth("100px");			
			
			Map<String, String> optionsMap = new TreeMap<String, String>();
			optionsMap.put("title", DisplayConstants.WHY_VERSION_READ_ONLY_MODE);
			optionsMap.put("data-placement", "right");
			DisplayUtils.addTooltip(synapseJSNIUtils, roContainer, optionsMap);
			lc.add(roContainer);
		}
	    lc.layout();
		return lc;
	}
	
	public static String restrictionDescriptor(APPROVAL_REQUIRED restrictionLevel) {
		switch (restrictionLevel) {
		case NONE:
			return DisplayConstants.OPEN;
		case LICENSE_ACCEPTANCE:
			return DisplayConstants.RESTRICTED;
		case ACT_APPROVAL:
			return DisplayConstants.CONTROLLED;
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
	
	public static String shieldStyleName(APPROVAL_REQUIRED restrictionLevel) {
		switch (restrictionLevel) {
		case NONE:
			return "green-shield";
		case LICENSE_ACCEPTANCE:
			return "yellow-shield";
		case ACT_APPROVAL:
			return "red-shield";
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}

	public static Widget createRestrictionsWidget(
			final String jiraFlagLink, 
			final boolean isAnonymous, 
			final boolean hasAdministrativeAccess,
			final String accessRequirementText,
			final Callback touAcceptanceCallback,
			final Callback requestACTCallback,
			final Callback imposeRestrictionsCallback,
			final Callback loginCallback,
			final APPROVAL_REQUIRED restrictionLevel, 
			final boolean hasFulfilledAccessRequirements,
			final IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		LayoutContainer lc = new HorizontalPanel();
		lc.setStyleAttribute("font-size", "80%");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		TableData td = new TableData();
		td.setVerticalAlign(VerticalAlignment.BOTTOM);
		lc.setLayout(new ColumnLayout());
		
		String shieldStyleName = shieldStyleName(restrictionLevel);

		String dataRestrictionType = "Data Access: "+restrictionDescriptor(restrictionLevel);
		
		
		SimplePanel shieldPanel = new SimplePanel();
		shieldPanel.setStyleName("left "+shieldStyleName);
		lc.add(shieldPanel, td);
		
		{
			SafeHtmlBuilder shb = new SafeHtmlBuilder();
			shb.appendHtmlConstant("<span class=\"strong\" style=\"margin-right: 10px; margin-left: 7px;\">"+dataRestrictionType+"</span>");
			lc.add(new HTML(shb.toSafeHtml()), td);
		}
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		String infoHyperlinkText = DisplayConstants.INFO;
		if (restrictionLevel==APPROVAL_REQUIRED.NONE) { // OPEN data
			if (hasAdministrativeAccess) {
				infoHyperlinkText=DisplayConstants.MODIFY;
			} // else default to 'info', i.e. you can find out details, but can't change anything
		} else { // CONTROLLED or RESTRICTED data
			if (hasFulfilledAccessRequirements) {
				// default = 'info', i.e. nothing more to do, you can just find out details
			} else {
				infoHyperlinkText = DisplayConstants.GAIN_ACCESS; // note, this applies to 'anonymous' too.  the path leads the user to logging in.
			}
		}
		shb.appendHtmlConstant("<span style=\"padding-right:30px;\"> (<a class=\"link\">"+infoHyperlinkText+"</a>)</span>");
		Anchor aboutLink = new Anchor(shb.toSafeHtml());
		lc.add(aboutLink, td);
		aboutLink.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				GovernanceDialogHelper.showAccessRequirement(
						restrictionLevel,
						isAnonymous,
						hasAdministrativeAccess,
						hasFulfilledAccessRequirements,
						iconsImageBundle,
						accessRequirementText,
						imposeRestrictionsCallback,
						touAcceptanceCallback,
						requestACTCallback,
						loginCallback,
						jiraFlagLink);

			}
		});
		Anchor flagLink = new Anchor(DisplayUtils.getIconHtml(iconsImageBundle.flagSmall16())+DisplayConstants.FLAG, true);
		flagLink.setStyleName("link");
		flagLink.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if (isAnonymous) {
					GovernanceDialogHelper.showAnonymousFlagDialog(loginCallback, iconsImageBundle);
				} else {
					GovernanceDialogHelper.showLoggedInFlagDialog(jiraFlagLink, iconsImageBundle);
				}
			}
		});		
		lc.add(flagLink, td);
		Map<String,String> optionsMap = new HashMap<String,String>();
		optionsMap.put("title", DisplayConstants.FLAG_TOOL_TIP);
		optionsMap.put("data-placement", "right");
		DisplayUtils.addTooltip(synapseJSNIUtils, flagLink, optionsMap);
	    
	    lc.layout();
		return lc;
	}

	/*
	 * Private Methods
	 */

	/**
	 * Basic meata data about this entity
	 * @param entity
	 * @return
	 */
	private static Html createMetadata(Entity entity) {
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<div style=\"font-size: 80%\">");
		builder.appendHtmlConstant("Added by: ");
		builder.appendEscaped(entity.getCreatedBy());
		builder.appendHtmlConstant(" on: ");
		builder.appendEscaped(String.valueOf(entity.getCreatedOn()));
		builder.appendHtmlConstant("<br/>Last updated by: ");
		builder.appendEscaped(entity.getModifiedBy());
		builder.appendHtmlConstant(" on: ");
		builder.appendEscaped(String.valueOf(entity.getModifiedOn()));
		builder.appendHtmlConstant("<br/>");
		if(entity instanceof Versionable){
			Versionable vb = (Versionable) entity;
			builder.appendHtmlConstant("Version: ");
			builder.appendEscaped(vb.getVersionLabel());
			builder.appendHtmlConstant(" (");
			builder.append(vb.getVersionNumber());
			builder.appendHtmlConstant(")");
		}
		builder.appendHtmlConstant("</div>");		
		
	    return new Html(builder.toSafeHtml().asString());
	}	

	
}
