package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordResetViewImpl extends Composite implements PasswordResetView {

	public interface PasswordResetViewImplUiBinder extends UiBinder<Widget, PasswordResetViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel contentPanel;	
	@UiField 
	SpanElement contentHtml;
	@UiField 
	SpanElement pageTitle;
	@UiField
	SimplePanel loadingPanel;	

	private Presenter presenter;
	private FormPanel requestFormPanel;
	private FormPanel resetFormPanel;
	private FormData formData;  
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private SageImageBundle sageImageBundle;
	TextField<String> newPassword, newPasswordConfirm;
	
	@Inject
	public PasswordResetViewImpl(PasswordResetViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget,
			IconsImageBundle iconsImageBundle, QueryFilter filter,
			SageImageBundle sageImageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		
		
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		
		formData = new FormData("-20");  
		createRequestForm();
		createResetForm();			
		
		loadingPanel.setVisible(false);
		contentPanel.add(requestFormPanel);
	}


	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.refresh();
	}


	 private void createRequestForm() {
	     requestFormPanel = new FormPanel();  
	     requestFormPanel.setFrame(true);
	     requestFormPanel.setHeaderVisible(false);  
	     requestFormPanel.setWidth(350);  
	   	   
	     FormLayout layout = new FormLayout();  
	     layout.setLabelWidth(100);  
	     requestFormPanel.setLayout(layout);  
	   
	     final TextField<String> emailAddress = new TextField<String>();  
	     emailAddress.setFieldLabel(DisplayConstants.EMAIL_ADDRESS);  
	     emailAddress.setAllowBlank(false);
	     emailAddress.setId(DisplayConstants.ID_INP_EMAIL_ADDRESS2);
	     requestFormPanel.add(emailAddress, formData);    
	   
	     requestFormPanel.setButtonAlign(HorizontalAlignment.CENTER);  
	     final Button sendChangeRequest = new Button("Send", new SelectionListener<ButtonEvent>() {				
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.requestPasswordReset(emailAddress.getValue());
				}
		 });
	     requestFormPanel.addButton(sendChangeRequest);

	     FormButtonBinding binding = new FormButtonBinding(requestFormPanel);
		 binding.addButton(sendChangeRequest);

			// Enter key submits form 
			new KeyNav<ComponentEvent>(requestFormPanel) {
				@Override
				public void onEnter(ComponentEvent ce) {
					super.onEnter(ce);
					sendChangeRequest.fireEvent(Events.Select);
				}
			};
	   }  

	 private void createResetForm() {
	     resetFormPanel = new FormPanel();  
	     resetFormPanel.setFrame(true);
	     resetFormPanel.setHeaderVisible(false);  
	     resetFormPanel.setWidth(350);    
	   
	     FormLayout layout = new FormLayout();  
	     layout.setLabelWidth(100);  
	     resetFormPanel.setLayout(layout);  
	   
	     newPassword = new TextField<String>();  
	     newPassword.setFieldLabel("New Password");  
	     newPassword.setAllowBlank(false);
	     newPassword.setPassword(true);
	     newPassword.setId(DisplayConstants.ID_INP_NEWPASSWORD);
	     resetFormPanel.add(newPassword, formData);  
	   
	     newPasswordConfirm = new TextField<String>();  
	     newPasswordConfirm.setFieldLabel("Confirm Password");  
	     newPasswordConfirm.setAllowBlank(false);
	     newPasswordConfirm.setPassword(true);
	     newPasswordConfirm.setId(DisplayConstants.ID_INP_CONFIRMPASSWORD);
	     resetFormPanel.add(newPasswordConfirm, formData);    
	   
	     resetFormPanel.setButtonAlign(HorizontalAlignment.CENTER);  
	     Button sendReset = new Button("Submit");
	     sendReset.addSelectionListener(new SelectionListener<ButtonEvent>() {				
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(newPassword.getValue() != null && newPasswordConfirm.getValue() != null && newPassword.getValue().equals(newPasswordConfirm.getValue())) {				
					presenter.resetPassword(newPassword.getValue());
				} else {
					MessageBox.alert("Error", "Passwords do not match. Please re-enter your new password.", null);
				}
				
			}
	     });
	     sendReset.setId(DisplayConstants.ID_BTN_SUBMIT);
	     resetFormPanel.addButton(sendReset);  		       		  
	   }


	@Override
	public void showRequestForm() {
		loadingPanel.setVisible(false);
		pageTitle.setInnerHTML(DisplayConstants.SEND_PASSWORD_CHANGE_REQUEST);
		contentHtml.setInnerHTML("");		
		contentPanel.clear();
		contentPanel.add(requestFormPanel);
	}


	@Override
	public void showResetForm() {
		loadingPanel.setVisible(false);
		pageTitle.setInnerHTML(DisplayConstants.SET_PASSWORD);
		contentHtml.setInnerHTML("");
		contentPanel.clear();
		contentPanel.add(resetFormPanel);
	}

	
	@Override
	public void clear() {
		loadingPanel.setVisible(false);
		contentHtml.setInnerHTML("");
		contentPanel.clear();
		if (newPassword != null)
			newPassword.clear();
		if (newPasswordConfirm != null)
			newPasswordConfirm.clear();

	}

	@Override
	public void showPasswordResetSuccess() {
		loadingPanel.setVisible(false);
		pageTitle.setInnerHTML(DisplayConstants.SUCCESS);
		contentPanel.clear();		
		contentHtml.setInnerHTML("Your password has been changed.");
	}


	@Override
	public void showErrorMessage(String errorMessage) {		
		MessageBox.info("Error", errorMessage, null);
	}


	@Override
	public void showRequestSentSuccess() {
		loadingPanel.setVisible(false);
		pageTitle.setInnerHTML(DisplayConstants.REQUEST_SENT);
		contentPanel.clear();		
		contentHtml.setInnerHTML("Your password reset request has been sent. Please check your Email.");
	}

	@Override
	public void showMessage(String message) {
		contentHtml.setInnerHTML(message);
	}

	@Override
	public void showLoading() {
		loadingPanel.setWidget(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
		loadingPanel.setVisible(true);		
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}


	@Override
	public void showExpiredRequest() {
		loadingPanel.setVisible(false);
		pageTitle.setInnerHTML(DisplayConstants.REQUEST_EXPIRED);
		contentHtml.setInnerHTML(DisplayConstants.SET_PASSWORD_EXPIRED);
	}  
}
