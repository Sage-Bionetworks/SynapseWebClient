package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileEditorWidgetImpl implements UserProfileEditorWidget {
	
	UserProfileEditorWidgetView view;
	
	@Inject
	public UserProfileEditorWidgetImpl(UserProfileEditorWidgetView view) {
		super();
		this.view = view;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(UserProfile profile) {
		view.setUsername(profile.getUserName());
		view.setFirstName(profile.getFirstName());
		view.setLastName(profile.getLastName());
		view.setBio(profile.getSummary());
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFirstName() {
		return view.getFirstName();
	}

	@Override
	public String getImageId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastName() {
		return view.getLastName();
	}

	@Override
	public String getUsername() {
		return view.getUsername();
	}

	@Override
	public String getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompany() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIndustry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSummary() {
		return view.getBio();
	}

}
