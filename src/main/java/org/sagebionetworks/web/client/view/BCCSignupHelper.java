package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class BCCSignupHelper {

	
	public static void showDialog(BCCSignupProfile profile, final BCCCallback callback) {
        final Dialog window = new Dialog();
        window.setMaximizable(false);
        window.setSize(ProfilePanel.WIDTH, 600);
        window.setPlain(true); 
        window.setModal(true); 
        
        window.setHeading("Sage / DREAM Breast Cancer Prognosis Challenge Sign-Up"); 
        window.setButtons(Dialog.OKCANCEL);
        window.okText = "Submit";
        window.setHideOnButtonClick(true);

        final ProfilePanel profilePanel = new ProfilePanel(profile);
        window.setLayout(new FitLayout());
	    window.add(profilePanel);
	      Button submitButton = window.getButtonById(Dialog.OK);
	        submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
	            @Override
	            public void componentSelected(ButtonEvent ce) {
	            	BCCSignupProfile completedProfile = new BCCSignupProfile();
	            	completedProfile.setTitle(profilePanel.getTitle());
	            	completedProfile.setFname(profilePanel.getFirstName());
	            	completedProfile.setLname(profilePanel.getLastName());
	            	completedProfile.setEmail(profilePanel.getEmail());
	            	completedProfile.setTeam(profilePanel.getTeam());
	            	completedProfile.setLab(profilePanel.getLab());
	            	completedProfile.setOrganization(profilePanel.getOrganization());
	            	completedProfile.setPhone(profilePanel.getPhone());
	            	completedProfile.setPostToBridge(profilePanel.getBridge());
	            	if (callback!=null) callback.submit(completedProfile);
	            }
	        });
	        // show the window

		    FormButtonBinding binding = new FormButtonBinding(profilePanel);  
		    binding.addButton(submitButton);  

		    window.show();		
	}
	


}
