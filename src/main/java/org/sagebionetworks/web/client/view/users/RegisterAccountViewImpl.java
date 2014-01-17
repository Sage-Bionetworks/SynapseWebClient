package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.filter.QueryFilter;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.KeyNav;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterAccountViewImpl extends Composite implements RegisterAccountView {

	public interface RegisterAccountViewImplUiBinder extends UiBinder<Widget, RegisterAccountViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;	
	@UiField
	SimplePanel registerAccountPanel;
	@UiField
	SpanElement contentHtml;

	private Presenter presenter;
	private FormPanel formPanel;
	private FormData formData;
	private IconsImageBundle iconsImageBundle;
	private Button registerButton;
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private TextField<String> username, email;
	@Inject
	public RegisterAccountViewImpl(RegisterAccountViewImplUiBinder binder, Header headerWidget, Footer footerWidget, IconsImageBundle iconsImageBundle, QueryFilter filter, SageImageBundle imageBundle, SageImageBundle sageImageBundle) {		
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = iconsImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.sageImageBundle = sageImageBundle;
		
		// header setup
		header.clear();
		footer.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());		
	}


	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure(false);
		headerWidget.refresh();
	}

	@Override
	public void showDefault() {
		this.clear();
		formData = new FormData("-20");  
		createForm();
		registerAccountPanel.clear();
		registerAccountPanel.add(formPanel);
	}


	@Override
	public void showAccountCreated() {
		this.clear();		
		contentHtml.setInnerHTML(DisplayUtils.getIconHtml(iconsImageBundle.informationBalloon16()) + " Your Synapse account has been created. We have sent you an email with instructions on how to setup a password for your account. Follow the directions in the email, and then <a href=\"#!LoginPlace:0\">login here</a>.");				
	}

	@Override
	public void showErrorMessage(String errorMessage) {
		MessageBox.info("Error", errorMessage, null);
	}


	@Override
	public void clear() {		
		if(registerAccountPanel != null) registerAccountPanel.clear();
		if(contentHtml != null) contentHtml.setInnerHTML("");	
	}

	@Override
	public void showAccountCreationFailed() {
		if(registerButton != null) {
			registerButton.enable();
			setRegisterButtonDefaultTextAndIcon();
		}
	}

	@Override
	public void markUsernameUnavailable() {
		username.markInvalid(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
	}
	
	@Override
	public void markEmailUnavailable() {
		email.markInvalid(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
	}
	
	/*
	 * Private Methods
	 */
	 private void createForm() {  
		     formPanel = new FormPanel();  
		     formPanel.setFrame(true);  
		     formPanel.setHeaderVisible(false);
		     formPanel.setWidth(515);  
		     formPanel.setLayout(new FlowLayout());		     
		   
		     FieldSet fieldSet = new FieldSet();  
		     fieldSet.setHeading("User Information&nbsp;");  
		   
		     FormLayout layout = new FormLayout();  
		     layout.setLabelWidth(100);  
		     fieldSet.setLayout(layout);  
		     
		     username = new TextField<String>();
		     username.setValidator(new Validator() {
				@Override
				public String validate(Field<?> field, String value) {
					//validate format up front.  if looks ok, ask presenter to check if alias is taken
					if (LoginPresenter.isValidUsername(username.getValue())) {
						presenter.checkUsernameAvailable(username.getValue());
						return null;
					}
					else {
						return DisplayConstants.USERNAME_FORMAT_ERROR;
					}
				}
			});
		     username.setFieldLabel("Username");
		     username.setAllowBlank(false);
		     username.setId(DisplayConstants.ID_INP_USERNAME);
		     fieldSet.add(username, formData);  
		     
		     email = new TextField<String>();
		     email.setValidator(new Validator() {
					@Override
					public String validate(Field<?> field, String value) {
						//validate format up front.  if looks ok, ask presenter to check if alias is taken
						if (LoginPresenter.isValidEmail(email.getValue())) {
							presenter.checkEmailAvailable(email.getValue());
							return null;
						}
						else {
							return WebConstants.INVALID_EMAIL_MESSAGE;
						}
					}
				});
		     
		     email.setFieldLabel("Email Address");
		     email.setAllowBlank(false);
		     email.setId(DisplayConstants.ID_INP_EMAIL_ADDRESS);
		     fieldSet.add(email, formData);  

		     final TextField<String> firstName = new TextField<String>();  
		     firstName.setFieldLabel("First Name");  
		     firstName.setAllowBlank(true);
		     firstName.setId(DisplayConstants.ID_INP_FIRSTNAME);
		     fieldSet.add(firstName, formData);  
		   
		     final TextField<String> lastName = new TextField<String>();  
		     lastName.setFieldLabel("Last Name");
		     lastName.setAllowBlank(true);
		     lastName.setId(DisplayConstants.ID_INP_LASTNAME);
		     fieldSet.add(lastName, formData);
		     
		     Label passwordLabel = new Label(DisplayUtils.getIconHtml(iconsImageBundle.lock16()) + " Password setup instructions will be sent via email.");
		     fieldSet.add(passwordLabel);
		   		   		   
		     formPanel.add(fieldSet);  
		     formPanel.setButtonAlign(HorizontalAlignment.CENTER);  
		     		     
		     registerButton = new Button(DisplayConstants.BUTTON_REGISTER, new SelectionListener<ButtonEvent>(){
					@Override
					public void componentSelected(ButtonEvent ce) {
						if(validateForm(username, email, firstName, lastName)) {
							DisplayUtils.changeButtonToSaving(registerButton, sageImageBundle);						
							presenter.registerUser(username.getValue(), email.getValue(), firstName.getValue(), lastName.getValue());
						} else {
							showErrorMessage(DisplayConstants.ERROR_ALL_FIELDS_REQUIRED);
						}
					}
		     });
		     registerButton.setId(DisplayConstants.ID_BTN_REGISTER2);
		     setRegisterButtonDefaultTextAndIcon();
		     formPanel.addButton(registerButton);
		     
			// Enter key submits form 
			new KeyNav<ComponentEvent>(formPanel) {
				@Override
				public void onEnter(ComponentEvent ce) {
					super.onEnter(ce);
					if(registerButton.isEnabled()) {
						registerButton.fireEvent(Events.Select);
					}
				}
			};

	 }

	private boolean validateForm(TextField<String> username,TextField<String> email, TextField<String> firstName, TextField<String> lastName) {
		if (email.getValue() != null && email.getValue().length() > 0 && email.isValid() 
				&& username.getValue() != null && username.getValue().trim().length() > 0 && username.isValid()) {
			return true;
		}
		return false;
	}

	private void setRegisterButtonDefaultTextAndIcon() {
		if(registerButton != null) {
			registerButton.setText(DisplayConstants.BUTTON_REGISTER);
			registerButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.mailArrow16()));
		}
	}
	
}
