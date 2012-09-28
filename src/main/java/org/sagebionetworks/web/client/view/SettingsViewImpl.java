package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SettingsViewImpl extends Composite implements SettingsView {

	public interface SettingsViewImplUiBinder extends UiBinder<Widget, SettingsViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel changePasswordPanel;
	@UiField
	SimplePanel setupPasswordButtonPanel;
	@UiField
	SimplePanel breadcrumbsPanel;
	@UiField
	SimplePanel storageUsagePanel;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private FormPanel resetFormPanel;
	private Button createPasswordButton;
	private SageImageBundle sageImageBundle;
	private Button changePasswordButton;
	private HTML changePasswordLabel;
	private Breadcrumb breadcrumb;
	private Footer footerWidget;

	private Html storageUsageWidget;
	
	private static final double BASE = 1024, KB = BASE, MB = KB*BASE, GB = MB*BASE, TB = GB*BASE;
	
	@Inject
	public SettingsViewImpl(SettingsViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle, SageImageBundle sageImageBundle,Breadcrumb breadcrumb) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		this.breadcrumb = breadcrumb;
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}


	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		header.clear();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();				
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void render() {
		//set the Settings page breadcrumb
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget("Settings"));
		
		createResetForm();
		
		changePasswordLabel.setHTML(SafeHtmlUtils.fromSafeConstant(""));
		
		changePasswordPanel.clear();
		changePasswordPanel.add(resetFormPanel);
		setChangePasswordDefaultIcon();
		
		createPasswordButton = new Button(DisplayConstants.BUTTON_SETUP_API_PASSWORD, AbstractImagePrototype.create(iconsImageBundle.addSquare16()), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				createPasswordButton.setIcon(AbstractImagePrototype.create(sageImageBundle.loading16()));
				createPasswordButton.disable();
				presenter.createSynapsePassword();
			}
		});
		createPasswordButton.enable();
		
		setupPasswordButtonPanel.clear();
		setupPasswordButtonPanel.add(createPasswordButton);

		storageUsageWidget = new Html();
		storageUsagePanel.add(storageUsageWidget);
	}

	@Override
	public void showPasswordChangeSuccess() {
		changePasswordButton.setText(DisplayConstants.BUTTON_CHANGE_PASSWORD);
		changePasswordButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.checkGreen16()));
		changePasswordLabel.setHTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.informationBalloon16()) + " Password Changed"));
	}

	@Override
	public void passwordChangeFailed() {
		changePasswordButton.setText(DisplayConstants.BUTTON_CHANGE_PASSWORD);
		changePasswordButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.error16()));		
	}

	@Override
	public void showRequestPasswordEmailSent() {
		createPasswordButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.checkGreen16()));
		createPasswordButton.setText("Email Sent");		
	}
	
	@Override
	public void requestPasswordEmailFailed() {
		createPasswordButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.error16()));
		createPasswordButton.setText("Email Send Failed");
	}

	/*
	 * Private Methods
	 */	
	 private void createResetForm() {  		 
		 resetFormPanel = new FormPanel();
		 FormData formData = new FormData("-20");
		   
	     resetFormPanel.setFrame(true);
	     resetFormPanel.setHeaderVisible(false);  
	     resetFormPanel.setWidth(350);  
	     resetFormPanel.setLayout(new FlowLayout());  
	   
	     FieldSet fieldSet = new FieldSet();  
	     fieldSet.setHeading(" ");  
	     fieldSet.setCheckboxToggle(false);	    
	     fieldSet.setCollapsible(false);
	   
	     FormLayout layout = new FormLayout();  
	     layout.setLabelWidth(100);  
	     fieldSet.setLayout(layout);  
	   
	     final TextField<String> username = new TextField<String>();  
	     username.setFieldLabel(DisplayConstants.LOGIN_USERNAME_LABEL);  
	     username.setAllowBlank(false);
	     fieldSet.add(username, formData);  

	     final TextField<String> currentPassword = new TextField<String>();  
	     currentPassword.setFieldLabel("Current Password");  
	     currentPassword.setAllowBlank(false);
	     currentPassword.setPassword(true);
	     fieldSet.add(currentPassword, formData);  
	   
	     final TextField<String> newPassword = new TextField<String>();  
	     newPassword.setFieldLabel("New Password");  
	     newPassword.setAllowBlank(false);
	     newPassword.setPassword(true);
	     fieldSet.add(newPassword, formData);  
	   
	     final TextField<String> newPasswordConfirm = new TextField<String>();  
	     newPasswordConfirm.setFieldLabel("Confirm Password");  
	     newPasswordConfirm.setAllowBlank(false);
	     newPasswordConfirm.setPassword(true);
	     fieldSet.add(newPasswordConfirm, formData);  

	     changePasswordLabel = new HTML();
	     fieldSet.add(changePasswordLabel);
	     
	     resetFormPanel.add(fieldSet);  
	   
	     resetFormPanel.setButtonAlign(HorizontalAlignment.CENTER);  
	     changePasswordButton = new Button(DisplayConstants.BUTTON_CHANGE_PASSWORD);
	     changePasswordButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	 @Override
	    	 public void componentSelected(ButtonEvent ce) {
	    		 if(newPassword.getValue() != null && newPasswordConfirm.getValue() != null && newPassword.getValue().equals(newPasswordConfirm.getValue())) {
	    			 changePasswordLabel.setHTML(SafeHtmlUtils.fromSafeConstant(""));
	    			 DisplayUtils.changeButtonToSaving(changePasswordButton, sageImageBundle);
	    			 presenter.resetPassword(username.getValue(), currentPassword.getValue(), newPassword.getValue());
	    		 } else {
	    			 MessageBox.alert("Error", "Passwords do not match. Please re-enter your new password.", null);
	    		 }
		
	    	 }
	     });	     
	     setChangePasswordDefaultIcon();
	     resetFormPanel.addButton(changePasswordButton);

	     // Enter key submits
	     new KeyNav<ComponentEvent>(resetFormPanel) {
	    	 @Override
	    	 public void onEnter(ComponentEvent ce) {
	    		 super.onEnter(ce);
	    		 if (changePasswordButton.isEnabled())
	    			 changePasswordButton.fireEvent(Events.Select);
	    	 }
	     };

	     // form binding so submit button is greyed out until all fields are filled 
	     final FormButtonBinding binding = new FormButtonBinding(resetFormPanel);
	     binding.addButton(changePasswordButton);
	 }
	 	 
	 private void setChangePasswordDefaultIcon() {
		 changePasswordButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.arrowCurve16()));			
	 }
	 
	@Override
	public void refreshHeader() {
		headerWidget.refresh();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clearStorageUsageUI() {
		storageUsageWidget.setHtml(DisplayConstants.STORAGE_USAGE_FAILED_TEXT);
	}
	
	public static String getFriendlySize(double size) {
		NumberFormat df = NumberFormat.getDecimalFormat();
		if(size >= TB) {
            return df.format(size/TB) + " Terabytes";
        }
		if(size >= GB) {
            return df.format(size/GB) + " Gigabytes";
        }
		if(size >= MB) {
            return df.format(size/MB) + " Megabytes";
        }
		if(size >= KB) {
            return df.format(size/KB) + " Kilobytes";
        }
        return df.format(size) + " Bytes";
    }
	
	@Override
	public void updateStorageUsage(Long grandTotal) {
		if (grandTotal == null){
			clearStorageUsageUI();
		}
		else {
			storageUsageWidget.setHtml("<h4>You are currently using " + getFriendlySize(grandTotal.doubleValue()) + "</h4>");
		}
	}
	

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void clear() {
		changePasswordPanel.clear();
		setupPasswordButtonPanel.clear();
		storageUsagePanel.clear();
	}

}
