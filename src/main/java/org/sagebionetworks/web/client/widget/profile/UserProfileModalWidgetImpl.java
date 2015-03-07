package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileModalWidgetImpl implements UserProfileModalWidget {
	
	private static final String SEE_ERRORS_ABOVE = "See errors above.";
	UserProfile originalProfile;
	UserProfileModalView view;
	UserProfileEditorWidget editorWidget;
	SynapseClientAsync synapse;
	Callback callback;
	
	@Inject
	public UserProfileModalWidgetImpl(UserProfileModalView view, UserProfileEditorWidget editorWidget, SynapseClientAsync synapse){
		this.view = view;
		this.editorWidget = editorWidget;
		this.synapse = synapse;
		this.view.setPreseneter(this);
		this.view.addEditorWidget(editorWidget);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onSave() {
		// First validate the view
		if(!editorWidget.isValid()){
			view.showError(SEE_ERRORS_ABOVE);
			return;
		}
		view.hideError();
		view.setProcessing(true);
		// Update the profile from the editor
		originalProfile.setProfilePicureFileHandleId(editorWidget.getImageId());
		originalProfile.setUserName(editorWidget.getUsername());
		originalProfile.setFirstName(editorWidget.getFirstName());
		originalProfile.setLastName(editorWidget.getLastName());
		originalProfile.setPosition(editorWidget.getPosition());
		originalProfile.setCompany(editorWidget.getCompany());
		originalProfile.setIndustry(editorWidget.getIndustry());
		originalProfile.setLocation(editorWidget.getLocation());
		originalProfile.setUrl(editorWidget.getUrl());
		originalProfile.setSummary(editorWidget.getSummary());
		originalProfile.setDisplayName(originalProfile.getFirstName() + " " + originalProfile.getLastName());
		// update the profile
		synapse.updateUserProfile(originalProfile, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				view.hideModal();
				callback.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
				view.setProcessing(false);
			}
		});
	}

	@Override
	public void showEditProfile(String userId, Callback callback) {
		this.callback = callback;
		view.showModal();
		view.setLoading(true);
		view.hideError();
		synapse.getUserProfile(userId, new AsyncCallback<UserProfile>() {
			
			@Override
			public void onSuccess(UserProfile profile) {
				originalProfile = profile;
				editorWidget.configure(profile);
				view.setLoading(false);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				view.showError(caught.getMessage());
			}
		});
	}

}
