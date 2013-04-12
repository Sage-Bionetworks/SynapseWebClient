package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EntityViewUtils {

	public static String restrictionDescriptor(RESTRICTION_LEVEL restrictionLevel) {
		switch (restrictionLevel) {
		case OPEN:
			return DisplayConstants.ANY_USE;
		case RESTRICTED:
			return DisplayConstants.RESTRICTED_USE;
		case CONTROLLED:
			return DisplayConstants.CONTROLLED_USE;
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
	
	public static ImageResource getShieldIcon(RESTRICTION_LEVEL restrictionLevel, IconsImageBundle iconsImageBundle) {
		switch (restrictionLevel) {
		case OPEN:
			return iconsImageBundle.sheildGreen16();
		case RESTRICTED:
			return iconsImageBundle.shieldYellow16();
		case CONTROLLED:
			return iconsImageBundle.shieldRed16();
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
			final RESTRICTION_LEVEL restrictionLevel, 
			final APPROVAL_TYPE approvalType,
			final boolean hasFulfilledAccessRequirements,
			final IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils) {
		
		final SimplePanel div = new SimplePanel();
		ImageResource shieldIcon = getShieldIcon(restrictionLevel, iconsImageBundle);
		String description = restrictionDescriptor(restrictionLevel);
		String tooltip = DisplayConstants.DATA_ACCESS_RESTRICTIONS_TOOLTIP;
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span style=\"margin-right: 5px;\" class=\"boldText\">"+DisplayConstants.DATA_ACCESS_RESTRICTIONS_TEXT+"</span>" + DisplayUtils.getIconHtml(shieldIcon) + "</div>");
		shb.appendHtmlConstant("<span style=\"margin-right: 10px; margin-left: 3px;\">"+description+"</span>");
		
		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		DisplayUtils.addTooltip(synapseJSNIUtils, htmlPanel, tooltip, TOOLTIP_POSITION.BOTTOM);
		div.add(htmlPanel);
		
		LayoutContainer lc = new HorizontalPanel();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		
		lc.add(div);
		shb = new SafeHtmlBuilder();
		String infoHyperlinkText = DisplayConstants.INFO;
		if (restrictionLevel==RESTRICTION_LEVEL.OPEN) { // OPEN data
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
		lc.add(aboutLink);
		aboutLink.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				GovernanceDialogHelper.showAccessRequirement(
						restrictionLevel,
						approvalType,
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
		lc.add(flagLink);
		DisplayUtils.addTooltip(synapseJSNIUtils, flagLink, DisplayConstants.FLAG_TOOL_TIP, TOOLTIP_POSITION.BOTTOM);
	    
	    lc.layout();
		return lc;
	}

	
}
