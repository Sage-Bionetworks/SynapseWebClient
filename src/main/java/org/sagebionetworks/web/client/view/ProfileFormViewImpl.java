package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.shared.WebConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class ProfileFormViewImpl extends SimplePanel implements ProfileFormView {

	private static final int COLUMN_FORM_WIDTH = 600;
	
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;
	private FormPanel userFormPanel;
	private Button updateUserInfoButton;
	private Button cancelUpdateUserButton;
	
	//Edit profile form fields
	private TextField<String> firstName, lastName, position, company, industry, location, email, teamName, url;
	private TextArea summary;
	
	@Inject
	public ProfileFormViewImpl(IconsImageBundle icons,
			SageImageBundle imageBundle, SageImageBundle sageImageBundle) {		
		this.iconsImageBundle = icons;
		this.sageImageBundle = sageImageBundle;
		createProfileForm();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void updateView(UserProfile profile) {
		clear();
		updateUserForm(profile);
		add(userFormPanel);
	}
	
	@Override
	public void showUserUpdateSuccess() {
		changeUserInfoButtonToDefault();
	}

	@Override
	public void userUpdateFailed() {
		changeUserInfoButtonToDefault();
	}
	
	private void changeUserInfoButtonToDefault()
	{
		updateUserInfoButton.setIcon(null);
		updateUserInfoButton.setText(DisplayConstants.BUTTON_CHANGE_USER_INFO);
	}

	 private void createProfileForm() {
		 userFormPanel = new FormPanel();
		 FormData formData = new FormData("-20");
		 
	     userFormPanel.setFrame(true);
	     userFormPanel.setHeaderVisible(false); 
	     userFormPanel.setLabelAlign(LabelAlign.TOP);
	     userFormPanel.setSize(COLUMN_FORM_WIDTH, -1);
	     
	     LayoutContainer main = new LayoutContainer();
	     main.setLayout(new ColumnLayout());

	     LayoutContainer left = new LayoutContainer();
	     left.setStyleAttribute("paddingRight", "10px");
	     FormLayout layout = new FormLayout();
	     layout.setLabelAlign(LabelAlign.TOP);
	     left.setLayout(layout);
	     
	     LayoutContainer right = new LayoutContainer();
	     right.setStyleAttribute("paddingLeft", "10px");
	     layout = new FormLayout();
	     layout.setLabelAlign(LabelAlign.TOP);
	     right.setLayout(layout);
	     
	     firstName = new TextField<String>();  
	     firstName.setFieldLabel("First Name");  
	     firstName.setAllowBlank(false);
	     left.add(firstName, formData);
	   
	     lastName = new TextField<String>();  
	     lastName.setFieldLabel("Last Name");  
	     lastName.setAllowBlank(false);
	     right.add(lastName, formData);
	     
	     position = new TextField<String>();  
	     position.setFieldLabel("Current Position");  
	     position.setAllowBlank(true);
	     left.add(position, formData);
	     
	     company = new TextField<String>();  
	     company.setFieldLabel("Current Affiliation");  
	     company.setAllowBlank(true);
	     right.add(company, formData);
	     
	     industry = new TextField<String>();  
	     industry.setFieldLabel("Industry/Discipline");  
	     industry.setAllowBlank(true);
	     left.add(industry, formData);
	     
	     location = new TextField<String>();  
	     location.setFieldLabel("City, Country");  
	     location.setAllowBlank(true);
	     right.add(location, formData);
	     
	     teamName = new TextField<String>();  
	     teamName.setFieldLabel("DREAM 8 Team Name");  
	     teamName.setAllowBlank(true);
//	     left.add(teamName, formData);
	     
	     url = new TextField<String>();  
	     url.setFieldLabel("Link To More Info");  
	     url.setAllowBlank(true);
	     url.setRegex(WebConstants.VALID_URL_REGEX);
	     url.getMessages().setRegexText(DisplayConstants.INVALID_URL_MESSAGE);
	     right.add(url, formData);
	     
	     main.add(left, new ColumnData(.5));
	     main.add(right, new ColumnData(.5));
	     
	     summary = new TextArea();  
	     summary.setFieldLabel("Summary");  
	     summary.setAllowBlank(true);
	     summary.setHeight(200);
	   
	     email = new TextField<String>();  
	     email.setFieldLabel("Email");  
	     email.setAllowBlank(false);
	     email.setRegex(WebConstants.VALID_EMAIL_REGEX);
	     email.getMessages().setRegexText(WebConstants.INVALID_EMAIL_MESSAGE);
	     
	     userFormPanel.add(main, new FormData("100%"));
	     userFormPanel.add(summary, new FormData("100%"));
		 //TODO: uncomment to add ability to change email
//	     userFormPanel.add(email, new FormData("100%"));
	     
	     userFormPanel.setButtonAlign(HorizontalAlignment.LEFT);  
	     updateUserInfoButton = new Button(DisplayConstants.BUTTON_CHANGE_USER_INFO);
	     updateUserInfoButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	 @Override
	    	 public void componentSelected(ButtonEvent ce) {
	    		 if(firstName.getValue().trim().equals("") || firstName.getValue().trim() == null) {
	    			 MessageBox.alert("Error", "Please enter your first name.", null);
	    		 } else if(lastName.getValue().trim().equals("") || lastName.getValue() == null) {
	    			 MessageBox.alert("Error", "Please enter your last name.", null);
    			 //TODO: uncomment to add ability to change email
//	    		 } else if(!email.isValid()) {
//		    			 MessageBox.alert("Error", email.getErrorMessage(), null);
	    		 } else if(!url.isValid()) {
	    			 MessageBox.alert("Error", url.getErrorMessage(), null);
	    		 } else {
	    			 startSave();
	    		 }
	    	 }
	     });
	     cancelUpdateUserButton = new Button(DisplayConstants.BUTTON_CANCEL);
	     cancelUpdateUserButton.addSelectionListener(new SelectionListener<ButtonEvent>() {				
	    	 @Override
	    	 public void componentSelected(ButtonEvent ce) {
	    		  presenter.cancelClicked();
	    	 }
	     });	     
	     
	     setUpdateUserInfoDefaultIcon();
	     userFormPanel.addButton(updateUserInfoButton);
	     userFormPanel.addButton(cancelUpdateUserButton);
	     // Enter key used to submit.  with a multiline component in the mix, we lose this convenience (unless we add code to
	     //	know if the summary field is in focus, or have a way of asking for the currently selected component (via the focus manager?)
//	     new KeyNav<ComponentEvent>(userFormPanel) {
//	    	 @Override
//	    	 public void onEnter(ComponentEvent ce) {
//	    		 super.onEnter(ce);
//	    		 if (updateUserInfoButton.isEnabled())
//	    			 updateUserInfoButton.fireEvent(Events.Select);
//	    	 }
//	     };

	     // form binding so submit button is greyed out until all fields are filled 
	     final FormButtonBinding binding = new FormButtonBinding(userFormPanel);
	     binding.addButton(updateUserInfoButton);

	 }
	 
	 private void startSave() {
		 DisplayUtils.changeButtonToSaving(updateUserInfoButton, sageImageBundle);
		 presenter.updateProfile(firstName.getValue(), lastName.getValue(), summary.getValue(), position.getValue(), location.getValue(), industry.getValue(), company.getValue(), null, null, teamName.getValue(), url.getValue());
//TODO: uncomment to add ability to change email
//		 presenter.updateProfile(firstName.getValue(), lastName.getValue(), summary.getValue(), position.getValue(), location.getValue(), industry.getValue(), company.getValue(), email.getValue(), null);
	 }
	 
	 private void updateUserForm(UserProfile profile) {
		 firstName.setValue(profile.getFirstName());
		 lastName.setValue(profile.getLastName());
		 position.setValue(profile.getPosition());
		 company.setValue(profile.getCompany());

		 industry.setValue(profile.getIndustry());
		 summary.setValue(profile.getSummary());
		 location.setValue(profile.getLocation());
		 email.setValue(profile.getEmail());
		 url.setValue(profile.getUrl());
		 teamName.setValue(profile.getTeamName());
	 }
		 
	 private void setUpdateUserInfoDefaultIcon() {
		 updateUserInfoButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.arrowCurve16()));
	 }
	 @Override
	 public void hideCancelButton(){
		 cancelUpdateUserButton.setVisible(false);
	 }

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
