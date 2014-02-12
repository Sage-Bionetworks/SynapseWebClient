package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntityViewUtils {

	public static String restrictionDescriptor(RESTRICTION_LEVEL restrictionLevel) {
		switch (restrictionLevel) {
		case OPEN:
			return DisplayConstants.ANY_USE;
		case RESTRICTED:
		case CONTROLLED:
			return DisplayConstants.RESTRICTED_USE;
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
	
	public static ImageResource getShieldIcon(RESTRICTION_LEVEL restrictionLevel, IconsImageBundle iconsImageBundle) {
		switch (restrictionLevel) {
		case OPEN:
			return iconsImageBundle.sheildGreen16();
		case RESTRICTED:
		case CONTROLLED:
			return iconsImageBundle.shieldYellow16();
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
		
		
		ImageResource shieldIcon = getShieldIcon(restrictionLevel, iconsImageBundle);
		String description = restrictionDescriptor(restrictionLevel);
		String tooltip = DisplayConstants.DATA_ACCESS_RESTRICTIONS_TOOLTIP;
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span style=\"margin-right: 5px;\" class=\"boldText\">"+DisplayConstants.DATA_ACCESS_RESTRICTIONS_TEXT+"</span>" + DisplayUtils.getIconHtml(shieldIcon));
		shb.appendHtmlConstant("<span style=\"margin-left: 3px;\">"+description+"</span>");
		
		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		DisplayUtils.addTooltip(synapseJSNIUtils, htmlPanel, tooltip, TOOLTIP_POSITION.BOTTOM);
		
		FlowPanel lc = new FlowPanel();
		
		lc.add(htmlPanel);
		
		String infoHyperlinkText = DisplayConstants.INFO;
		if (restrictionLevel==RESTRICTION_LEVEL.OPEN) { // OPEN data
			if (hasAdministrativeAccess) {
				infoHyperlinkText=DisplayConstants.CHANGE;
			} // else default to 'info', i.e. you can find out details, but can't change anything
		} else { // CONTROLLED or RESTRICTED data
			if (hasFulfilledAccessRequirements) {
				// default = 'info', i.e. nothing more to do, you can just find out details
			} else {
				infoHyperlinkText = DisplayConstants.GAIN_ACCESS; // note, this applies to 'anonymous' too.  the path leads the user to logging in.
			}
		}
		
		
		Anchor aboutLink = new Anchor(infoHyperlinkText);
		aboutLink.addStyleName("link");
		
		DisplayUtils.surroundWidgetWithParens(lc, aboutLink);
		
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
		Anchor flagLink = new Anchor(DisplayUtils.getIconHtml(iconsImageBundle.flagSmall16())+DisplayConstants.REPORT_ISSUE, true);
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
	    return lc;
	}

	
}
