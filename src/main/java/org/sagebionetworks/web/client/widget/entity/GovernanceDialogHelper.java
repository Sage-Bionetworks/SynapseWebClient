package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ProfilePanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class GovernanceDialogHelper {

	
	private static RowData standardPadding;
	static {
		standardPadding = new RowData();
		int horizontal = 15;
		int horizontalDelta = 10;
		int bottom = 0;
		int vertical = 10;
		int verticalDelta = 5;
		standardPadding.setMargins(new Margins(vertical+verticalDelta, horizontal, bottom, horizontal));
		RowData h1Padding = new RowData();
		h1Padding.setMargins(new Margins(vertical, horizontal, bottom, horizontal));
		RowData h2Padding = new RowData();
		h2Padding.setMargins(new Margins(vertical, horizontal, bottom, horizontal+horizontalDelta));
	}
	
	private static void configureDialog(Dialog dialog) {
	       	dialog.setMaximizable(false);
	        dialog.setSize(ProfilePanel.WIDTH, 600);
	        dialog.setPlain(true); 
	        dialog.setModal(true); 
	        dialog.setBlinkModal(true); 
	        dialog.setAutoHeight(true);
	        dialog.setResizable(false);		
	}
	
	private static ImageResource flagIcon(IconsImageBundle iconsImageBundle) {
		return iconsImageBundle.flagSmall16();
	}
	
	// for an anonymous user attempting to 'flag' a data object
	public static void showAnonymousFlagDialog(final Callback loginCallback, IconsImageBundle iconsImageBundle) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        dialog.setIcon(AbstractImagePrototype.create(flagIcon(iconsImageBundle)));
        dialog.addText(DisplayConstants.FLAG_DIALOG_PREFIX+DisplayConstants.FLAG_DIALOG_ANONYMOUS_SUFFIX);
        dialog.setHeading(DisplayConstants.FLAG_DIALOG_TITLE); 
        dialog.okText = DisplayConstants.BUTTON_TEXT_LOGIN;
        dialog.setButtons(Dialog.OKCANCEL);
        Button okButton = dialog.getButtonById(Dialog.OK);
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				loginCallback.invoke();
			}
        });       
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a logged-in user trying to 'flag' a data object
	public static void showLoggedInFlagDialog(final String jiraFlagLink, IconsImageBundle iconsImageBundle) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        dialog.setIcon(AbstractImagePrototype.create(flagIcon(iconsImageBundle)));
    	StringBuilder sb = new StringBuilder(DisplayConstants.FLAG_DIALOG_PREFIX);
    	sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(DisplayConstants.FLAG_DIALOG_TITLE); 
        dialog.okText = DisplayConstants.BUTTON_TEXT_CONTACT_ACT;
        dialog.setButtons(Dialog.OKCANCEL);
        Button okButton = dialog.getButtonById(Dialog.OK);
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){

			@Override
			public void componentSelected(ButtonEvent ce) {
				// open jira page to make 'flag' issue
				Window.open(jiraFlagLink, "_blank", "");
			}
        	
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
		
	
	private static ContentPanel createTextPanel(Dialog dialog) {
		ContentPanel panel = new ContentPanel();		
		panel.setLayoutData(new RowLayout(Orientation.VERTICAL));		
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.setHeaderVisible(false);		
		panel.setBodyStyle("backgroundColor: #e8e8e8");
		dialog.add(panel);
		return panel;
	}
	
	private static LayoutContainer createLicenseTextContainer(String licenseText) {
		LayoutContainer licenseTextContainer;
		licenseTextContainer = new LayoutContainer();
		licenseTextContainer.setHeight(200);
		licenseTextContainer.addStyleName("pad-text");
		licenseTextContainer.setStyleAttribute("backgroundColor", "white");
		licenseTextContainer.setBorders(true);
		licenseTextContainer.setScrollMode(Style.Scroll.AUTOY);
		licenseTextContainer.removeAll();
		StringBuilder sb = new StringBuilder();
		sb.append("<p style=\"font-style:italic\">");
		sb.append(licenseText);
		sb.append("</p>");
		licenseTextContainer.add(new HTML(sb.toString()));
		licenseTextContainer.layout(true);
		return licenseTextContainer;
	}
	
	private static ImageResource restrictionLevelIcon(APPROVAL_REQUIRED restrictionLevel, IconsImageBundle iconsImageBundle) {
		switch (restrictionLevel) {
		case NONE:
			return iconsImageBundle.flagSmall16(); // TODO use correct icon here
		case LICENSE_ACCEPTANCE:
			return iconsImageBundle.flagSmall16(); // TODO use correct icon here
		case ACT_APPROVAL:
			return iconsImageBundle.flagSmall16(); // TODO use correct icon here
		default:
			throw new IllegalArgumentException(restrictionLevel.toString());
		}
	}
	
	public static void showAccessRequirement(
			APPROVAL_REQUIRED restrictionLevel, 
			boolean isAnonymous, 
			boolean hasAdministrativeAccess,
			boolean accessApproved, 
			IconsImageBundle iconsImageBundle,
			String arText,
			final Callback imposeRestrictionsCallback,
			final Callback touAcceptanceCallback,
			final Callback requestACTCallback,
			final Callback loginCallback,
			final String jiraFlagLink) {
		if (restrictionLevel!=APPROVAL_REQUIRED.NONE && isAnonymous && accessApproved) 
			throw new IllegalArgumentException("restrictionLevel!=APPROVAL_REQUIRED.NONE && isAnonymous && accessApproved");
		boolean imposeRestrictionsAllowed = (restrictionLevel==APPROVAL_REQUIRED.NONE && hasAdministrativeAccess);
		final Dialog dialog = new Dialog();
        configureDialog(dialog);
        ContentPanel panel = createTextPanel(dialog);
 		// title and icon are based on restriction level, e.g. "Data Access: Restricted"
        dialog.setIcon(AbstractImagePrototype.create(restrictionLevelIcon(restrictionLevel, iconsImageBundle)));
        dialog.setHeading("Data Access: "+EntityViewUtils.restrictionDescriptor(restrictionLevel)); 
		// next comes the restriction descriptor, e.g. "Access to the data is Restricted." (Bold)
      	panel.addText("<p class=\"strong\">Access to the data is "+EntityViewUtils.restrictionDescriptor(restrictionLevel)+".</p>");
      	if (restrictionLevel==APPROVAL_REQUIRED.NONE) {
      		panel.addText("<p>"+DisplayConstants.UNRESTRICTED_DESCRIPTION+"</p>");
      	} else {
         	// next, if you are approved, comes "You have access to this data under the following..."
    		// or if you are not approved, "In order to Access..."
     		if (accessApproved) {
      			panel.addText(DisplayConstants.RESTRICTION_FULFILLED_STATEMENT);
      		} else {
      			if (restrictionLevel==APPROVAL_REQUIRED.LICENSE_ACCEPTANCE) {
          			panel.addText(DisplayConstants.TOU_PROMPT);
     			} else { //restrictionLevel==APPROVAL_REQUIRED.ACT_APPROVAL
          			panel.addText(DisplayConstants.ACT_PROMPT);
     			}
      		}
    		// next comes the Terms of Use or ACT info, in its own box
           	panel.add(createLicenseTextContainer(arText), standardPadding);
    		// if not logged in there's an extra line "Note:  You must log in to access restricted data."
           	if (isAnonymous) {
           		panel.addText(DisplayConstants.RESTRICTED_DATA_LOGIN_WARNING);
           	}
      	}
		// next there's a prompt with a link to the Governance page
      	if (imposeRestrictionsAllowed) {
           	StringBuilder sb = new StringBuilder();
        	sb.append("<p>"+DisplayConstants.ADMIN_GOVERNANCE_REFERENCE);
        	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_3);
        	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_4);
        	sb.append("</p>");
        	panel.addText(sb.toString());
     	} else {
          	panel.addText("<p>"+DisplayConstants.GOVERNANCE_REFERENCE+"</p>");
     	}
		// finally there the Flag notice and hyperlink
      	// (but not for a user having admin access to their own dataaset
      	if (isAnonymous) {
            panel.addText(DisplayConstants.FLAG_DIALOG_PREFIX+
            		DisplayConstants.FLAG_DIALOG_ANONYMOUS_SUFFIX);
      	} else if (!imposeRestrictionsAllowed) {
           	StringBuilder sb = new StringBuilder();
           	sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
            sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1);
            sb.append(jiraFlagLink);
            sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2);
        	panel.addText(sb.toString());
      	}
		// buttons:
     	if (isAnonymous) {
      		// login or cancel
            dialog.okText = DisplayConstants.BUTTON_TEXT_LOGIN;
            dialog.setButtons(Dialog.OKCANCEL);
            Button okButton = dialog.getButtonById(Dialog.OK);
            okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
    			@Override
    			public void componentSelected(ButtonEvent ce) {loginCallback.invoke();}
            });       
     	} else {
      		if (restrictionLevel==APPROVAL_REQUIRED.NONE) {
      			if (hasAdministrativeAccess) {
      				// button to contact act, cancel
      				// Note:  "contact act" should become "Add Restriction"
      		        dialog.okText = DisplayConstants.BUTTON_TEXT_RESTRICT_DATA;
      		        dialog.setButtons(Dialog.OKCANCEL);
      		        Button okButton = dialog.getButtonById(Dialog.OK);
      		        okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
      					@Override
      					public void componentSelected(ButtonEvent ce) {
      						imposeRestrictionsCallback.invoke();
      					}
      		        });
      			} else {
        			// just a close button
      		        dialog.setButtons(Dialog.CLOSE);
     			}
    		} else {
     			if (accessApproved) {
     				// just a close button
     		        dialog.setButtons(Dialog.CLOSE);
     			} else {
	     			if (restrictionLevel==APPROVAL_REQUIRED.LICENSE_ACCEPTANCE) {
	     				// agree to TOU, cancel
	     		        dialog.okText = DisplayConstants.BUTTON_TEXT_ACCEPT_TERMS_OF_USE;
	     		        dialog.setButtons(Dialog.OKCANCEL);
	     		        Button touButton = dialog.getButtonById(Dialog.OK);
	     		        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
	     					@Override
	     					public void componentSelected(ButtonEvent ce) {
	     						touAcceptanceCallback.invoke();
	     					}
	     		        });
	     			} else { //ACT_APPROVAL
	     				// request access, cancel
	     		        dialog.okText = DisplayConstants.BUTTON_TEXT_REQUEST_ACCESS_FROM_ACT;
	     		        dialog.setButtons(Dialog.OKCANCEL);
	     		        Button touButton = dialog.getButtonById(Dialog.OK);
	     		        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
	     					@Override
	     					public void componentSelected(ButtonEvent ce) {
	     						requestACTCallback.invoke();
	     					}
	     		        });
	     			}
     			}
      		}
      	}
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
}
