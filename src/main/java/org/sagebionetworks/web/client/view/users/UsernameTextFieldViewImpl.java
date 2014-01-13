package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UsernameTextFieldViewImpl implements UsernameTextFieldView {

	private TextField<String> usernameTextField;
	private Presenter presenter;
	private boolean isUnique, isValidated;
	private Validator usernameTextFieldValidator;
	
	@Inject
	public UsernameTextFieldViewImpl(IconsImageBundle iconsImageBundle) {
	}

	@Override
	public Widget asWidget() {
		return usernameTextField;
	}
	
	@Override
	public void configure(String initUsername) {
		usernameTextField = new TextField<String>();
		if (initUsername != null) {
			usernameTextField.setValue(initUsername);
		}
		usernameTextField.setFieldLabel("Username");
		usernameTextField.setAllowBlank(false);
	    usernameTextField.setId(DisplayConstants.ID_INP_USERNAME);
		
		isUnique = true;
		isValidated = true;

		//in 3 seconds, check to see if the username field is "dirty" (validation needs to take place).
		final Timer t = new Timer() {
	      @Override
	      public void run() {
	    	  if (!isValidated) {
	    		  presenter.validateUsername(usernameTextField.getValue());  
	    	  }
	      }
	    };

		usernameTextField.addKeyListener(new KeyListener() {
			@Override
			public void componentKeyPress(ComponentEvent event) {
				//After a keypress, wait 3 seconds and validate 
				//Note that the first thing this does is cancel any previous scheduling, so the user really needs to be done editing for a few seconds for this to fire
				t.schedule(3000);
				//mark as dirty
				isUnique = true;
				isValidated = false;
			};
		});
		

	    //Validator depends on an async process (to validate the user name).
		//Will be correct if username validation happens before this validator is called
		usernameTextFieldValidator = new Validator() {
			@Override
			public String validate(Field<?> field, String value) {
				if (value == null || value.trim().length() == 0)
					return DisplayConstants.REQUIRED_USERNAME_ERROR;
				
				if (isUnique)
					return null;
				else 
					return DisplayConstants.DUPLICATE_USERNAME_ERROR;
			}
		};
		
		usernameTextField.setValidator(usernameTextFieldValidator);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void setIsUniqueUsername(boolean isUnique) {
		this.isValidated = true;
		this.isUnique = isUnique;
	}
	
	@Override
	public boolean validate() {
		return usernameTextField.validate();
	}
}
