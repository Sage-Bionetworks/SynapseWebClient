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
			final Callback loginCallback,
			final RESTRICTION_LEVEL restrictionLevel, 
			final IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils,
			ClickHandler aboutLinkClickHandler,
			boolean showFlagLink,
			boolean showChangeLink) {

		ImageResource shieldIcon = getShieldIcon(restrictionLevel, iconsImageBundle);
		String description = restrictionDescriptor(restrictionLevel);
		String tooltip = DisplayConstants.DATA_ACCESS_RESTRICTIONS_TOOLTIP;
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(DisplayUtils.getIconHtml(shieldIcon));
		shb.appendHtmlConstant("<span style=\"margin-left: 3px;\">"+description+"</span>");
		
		//form the html
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		htmlPanel.addStyleName("inline-block");
		DisplayUtils.addTooltip(synapseJSNIUtils, htmlPanel, tooltip, TOOLTIP_POSITION.BOTTOM);
		
		FlowPanel lc = new FlowPanel();
		lc.addStyleName("inline-block");
		
		lc.add(htmlPanel);
		
		//show the info link if there are any restrictions, or if we are supposed to show the flag link (to allow people to flag or  admin to "change" the data access level).
		boolean isChangeLink = restrictionLevel==RESTRICTION_LEVEL.OPEN && hasAdministrativeAccess;
		boolean isRestricted = restrictionLevel!=RESTRICTION_LEVEL.OPEN;
		if ((isChangeLink && showChangeLink) || isRestricted) {
			String infoHyperlinkText = isChangeLink ? DisplayConstants.CHANGE : DisplayConstants.SHOW_LC;
			//default to 'show', i.e. you can find out details, but can't change anything
			//If CONTROLLED or RESTRICTED data:  if the user hasFulfilledAccessRequirements, requirement details will be shown.  If not, user can accept requirements and will gain access to data
			
			Anchor aboutLink = new Anchor(infoHyperlinkText);
			aboutLink.addStyleName("link");
			
			DisplayUtils.surroundWidgetWithParens(lc, aboutLink);
			
			aboutLink.addClickHandler(aboutLinkClickHandler);
		}
		if (showFlagLink) {
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
		}
			
	    return lc;
	}

	
}
