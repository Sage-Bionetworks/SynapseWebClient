package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ProfilePanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.Window;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GovernanceDialogHelper {
	// TODO move these constants to the Constants class
	public static final String FLAG_DIALOG_PREFIX = 
		"If you feel this data is inappropriate, requires restrictions, or otherwise requires "+
		"review by the Synapse Access and Compliance Team, ";
	public static final String FLAG_DIALOG_ANONYMOUS_SUFFIX = "log in, then return here to contact us.";
	public static final String FLAG_DIALOG_LOGGED_IN_SUFFIX = "click 'Contact ACT' below.";
	public static final String BUTTON_TEXT_CONTACT_ACT = "Contact ACT";
	public static final String BUTTON_TEXT_ACCEPT_TERMS_OF_USE = "Accept ToU";
	public static final String FLAG_DIALOG_TITLE = "Flag Data";
	public static String UNRESTRICTED_DATA_DIALOG_TITLE = "Unrestricted Data";
	public static String RESTRICTED_DATA_DIALOG_TITLE = "Restricted Data";
	public static String RESTRICTION_FULFILLED_DATA_DIALOG_TITLE = "Access Requirements Fulfilled";
	public static String RESTRICTION_DIALOG_TEXT_1 = "Certain kinds of data require additional approval prior to access.  ";
	public static String RESTRICTION_DIALOG_TEXT_2 = "<a href=\"#Governance:0\"  target=\"_blank\" class=\"link\">Click here for more information on restricted data.</a>  ";
	public static String RESTRICTION_DIALOG_TEXT_3 = "If you feel this data requires such restrictions, contact the Synapse Access and Compliance Team by clicking 'Contact ACT' below.  ";
	public static String RESTRICTION_DIALOG_TEXT_4 = "<b>Note:  Download will be temporarily restricted pending review by the ACT.</b>";
	public static final String TOU_RESTRICTION_HEADER = "Terms of Use:";
	public static final String RESTRICTION_FULFILLED_STATEMENT = "You have fulfilled the requirements for accessing this data:";
		
	private static void configureDialog(Dialog dialog) {
	       	dialog.setMaximizable(false);
	        dialog.setSize(ProfilePanel.WIDTH, 600);
	        dialog.setPlain(true); 
	        dialog.setModal(true); 
	        dialog.setBlinkModal(true); 
	        dialog.setAutoHeight(true);
	}
	
	// for an anonymous user attempting to 'flag' a data object
	public static void showAnonymousFlagDialog() {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
        dialog.addText(FLAG_DIALOG_PREFIX+FLAG_DIALOG_ANONYMOUS_SUFFIX);
        dialog.setHeading(FLAG_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a logged-in user trying to 'flag' a data object
	public static void showLoggedInFlagDialog(final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder(FLAG_DIALOG_PREFIX);
    	sb.append(FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(FLAG_DIALOG_TITLE); 
        dialog.okText = BUTTON_TEXT_CONTACT_ACT;
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
       	sb.append(RESTRICTION_DIALOG_TEXT_1);
       	sb.append(RESTRICTION_DIALOG_TEXT_2);
       	sb.append(RESTRICTION_DIALOG_TEXT_3);
       	sb.append(RESTRICTION_DIALOG_TEXT_4);
    	dialog.addText(sb.toString());
        dialog.setHeading(UNRESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = BUTTON_TEXT_CONTACT_ACT;
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
       	sb.append(RESTRICTION_DIALOG_TEXT_1);
       	sb.append(RESTRICTION_DIALOG_TEXT_2);
       	sb.append(FLAG_DIALOG_PREFIX);
       	sb.append(FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(UNRESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = BUTTON_TEXT_CONTACT_ACT;
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
       	sb.append(RESTRICTION_DIALOG_TEXT_1);
       	sb.append(RESTRICTION_DIALOG_TEXT_2);
       	sb.append(FLAG_DIALOG_PREFIX);
       	sb.append(FLAG_DIALOG_ANONYMOUS_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(UNRESTRICTED_DATA_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a data object having a terms-of-use access requirement which the user has not signed
	public static void showTermsOfUseAccessRequirement(String touText, final Callback callback, final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append("<p>"+TOU_RESTRICTION_HEADER+"</p>");
       	sb.append(touText);
       	sb.append(FLAG_DIALOG_PREFIX);
      	sb.append(FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.yesText = BUTTON_TEXT_ACCEPT_TERMS_OF_USE;
        dialog.noText = BUTTON_TEXT_CONTACT_ACT;
        dialog.setButtons(Dialog.YESNOCANCEL);
        Button touButton = dialog.getButtonById(Dialog.NO);
        touButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				callback.invoke();
			}
        });
        Button actButton = dialog.getButtonById(Dialog.NO);
        actButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				// open jira page to make 'flag' issue
				Window.open(jiraFlagLink, "_blank", "");
			}
        });
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}
	
	// for a data object having an ACT (tier 3) access requirement for which the user has not been approved
	public static void showACTAccessRequirement(String arText, final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append("<p>"+RESTRICTED_DATA_DIALOG_TITLE+"</p>");
       	sb.append(arText);
       	sb.append(FLAG_DIALOG_PREFIX);
      	sb.append(FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.okText = BUTTON_TEXT_CONTACT_ACT;
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
	
	// for a data object having access requirement(s) which the user has fulfilled
	public static void showFulfilledAccessRequirement(String arText, final String jiraFlagLink) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append("<p>"+RESTRICTION_FULFILLED_STATEMENT+"</p>");
       	sb.append(arText);
       	sb.append(FLAG_DIALOG_PREFIX);
      	sb.append(FLAG_DIALOG_LOGGED_IN_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(RESTRICTION_FULFILLED_DATA_DIALOG_TITLE); 
        dialog.okText = BUTTON_TEXT_CONTACT_ACT;
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
	
	// for a data object having access requirements, when the user is not logged in
	public static void showAnonymousAccessRequirement(String arText) {
        final Dialog dialog = new Dialog();
        configureDialog(dialog);
    	StringBuilder sb = new StringBuilder();
       	sb.append("<p>"+RESTRICTED_DATA_DIALOG_TITLE+"</p>");
       	sb.append("<p>"+arText+"</p>");
       	sb.append(FLAG_DIALOG_PREFIX);
       	sb.append(FLAG_DIALOG_ANONYMOUS_SUFFIX);
    	dialog.addText(sb.toString());
        dialog.setHeading(RESTRICTED_DATA_DIALOG_TITLE); 
        dialog.setButtons(Dialog.CLOSE);
        dialog.setHideOnButtonClick(true);		
		dialog.show();		
	}

}
