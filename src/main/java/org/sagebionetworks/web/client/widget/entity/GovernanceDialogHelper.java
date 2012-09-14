package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ProfilePanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

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
	
	// for an anonymous user attempting to 'flag' a data object
	public static void showAnonymousFlagDialog() {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        dialog.addText(DisplayConstants.FLAG_DIALOG_PREFIX+DisplayConstants.FLAG_DIALOG_ANONYMOUS_SUFFIX);
        dialog.setHeading(DisplayConstants.FLAG_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a logged-in user trying to 'flag' a data object
	public static void showLoggedInFlagDialog(final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
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
	
		
	// for a user having admin access to a data object which does not currently have access restrictions
	public static void showImposeRestrictionsDialog(final String jiraRestrictionsLink, final Callback lockdownCallback) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_1);
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_2);
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_3);
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_4);
    	dialog.addText(sb.toString());
        dialog.setHeading(DisplayConstants.UNRESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = DisplayConstants.BUTTON_TEXT_CONTACT_ACT;
        dialog.setButtons(Dialog.OKCANCEL);
        Button okButton = dialog.getButtonById(Dialog.OK);
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				// lock down data object
				lockdownCallback.invoke();
				// open jira page to make 'flag' issue
				Window.open(jiraRestrictionsLink, "_blank", "");
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();			
	}
	
	// for a user lacking admin access to a data object which does not currently have access restrictions
	public static void showUnrestrictedDataDialog(final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_1);
       	sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
       	sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(DisplayConstants.UNRESTRICTED_DATA_DIALOG_TITLE); 
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
	
	// for an anonymous user reviewing a data object which does not have access restrictions
	public static void showAnonymousUnrestrictedDataDialog() {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append(DisplayConstants.RESTRICTION_DIALOG_TEXT_1);
       	sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
       	sb.append(DisplayConstants.FLAG_DIALOG_ANONYMOUS_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(DisplayConstants.UNRESTRICTED_DATA_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
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
	
	// for a data object having a terms-of-use access requirement which the user has not signed
	// note: jiraFlagLink is optional
	public static void showTermsOfUseAccessRequirement(String touText, final Callback callback, final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        ContentPanel panel = createTextPanel(dialog);
       	panel.addText("<p>"+DisplayConstants.TOU_RESTRICTION_HEADER+"</p>");
       	panel.add(createLicenseTextContainer(touText), standardPadding);
      	if (jiraFlagLink!=null) {
        	StringBuilder sb = new StringBuilder();
      		sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
       		sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1);
       		sb.append(jiraFlagLink);
       		sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2);
        	panel.addText(sb.toString());
       	}
    	dialog.setHeading(DisplayConstants.RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = DisplayConstants.BUTTON_TEXT_ACCEPT_TERMS_OF_USE;
        dialog.setButtons(Dialog.OKCANCEL);
        Button touButton = dialog.getButtonById(Dialog.OK);
        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				callback.invoke();
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a data object having an ACT (tier 3) access requirement for which the user has not been approved
	public static void showACTAccessRequirement(String arText, final String jiraFlagLink, final String requestAccessLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        ContentPanel panel = createTextPanel(dialog);
        panel.addText("<p>"+DisplayConstants.RESTRICTED_DATA_DIALOG_PROMPT+"</p>");
       	panel.add(createLicenseTextContainer(arText), standardPadding);
       	if (jiraFlagLink!=null) {
           StringBuilder sb = new StringBuilder();
           sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
           sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1);
           sb.append(jiraFlagLink);
           sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2);
           panel.addText(sb.toString());
       	}
        dialog.setHeading(DisplayConstants.RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = DisplayConstants.BUTTON_TEXT_REQUEST_ACCESS_FROM_ACT;
        dialog.setButtons(Dialog.OKCANCEL);
        Button okButton = dialog.getButtonById(Dialog.OK);
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				// open jira page to make 'flag' issue
				Window.open(requestAccessLink, "_blank", "");
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a data object having access requirement(s) which the user has fulfilled
	public static void showFulfilledAccessRequirement(String arText, final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        ContentPanel panel = createTextPanel(dialog);
       	panel.addText("<p>"+DisplayConstants.RESTRICTION_FULFILLED_STATEMENT+"</p>");
       	panel.add(createLicenseTextContainer(arText), standardPadding);
       	StringBuilder sb = new StringBuilder();
       	sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
        sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_1);
        sb.append(jiraFlagLink);
        sb.append(DisplayConstants.FLAG_DIALOG_LOGGED_IN_SUFFIX_WITH_HYPERLINK_2);
    	panel.addText(sb.toString());
        dialog.setHeading(DisplayConstants.RESTRICTION_FULFILLED_DATA_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a data object having access requirements, when the user is not logged in
	public static void showAnonymousAccessRequirement(String arText, boolean includeFlag) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        ContentPanel panel = createTextPanel(dialog);
       	panel.addText("<p>"+DisplayConstants.RESTRICTED_DATA_DIALOG_PROMPT+"</p>");
       	panel.addText("<p>"+DisplayConstants.ACT_PROMPT+"</p>");
       	panel.add(createLicenseTextContainer(arText), standardPadding);
       	panel.addText(DisplayConstants.RESTRICTED_DATA_LOGIN_WARNING);
       	panel.addText("<p/>");
       	if (includeFlag) {
       		StringBuilder sb = new StringBuilder();
       		sb.append(DisplayConstants.FLAG_DIALOG_PREFIX);
       		sb.append(DisplayConstants.FLAG_DIALOG_ANONYMOUS_SUFFIX);
       		panel.addText(sb.toString());
       	}
        dialog.setHeading(DisplayConstants.RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}

}
