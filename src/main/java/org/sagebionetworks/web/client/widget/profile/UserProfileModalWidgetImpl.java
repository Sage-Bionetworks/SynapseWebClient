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
	UserProfileModalView modalView;
	UserProfileEditorWidget editorWidget;
	SynapseClientAsync synapse;
	Callback callback;
	
	@Inject
	public UserProfileModalWidgetImpl(UserProfileModalView view, UserProfileEditorWidget editorWidget, SynapseClientAsync synapse){
		this.modalView = view;
		this.editorWidget = editorWidget;
		this.synapse = synapse;
		this.modalView.setPreseneter(this);
		this.modalView.addEditorWidget(editorWidget);
	}

	@Override
	public Widget asWidget() {
		return modalView.asWidget();
	}

	@Override
	public void onSave() {
		// First validate the view
		if(!editorWidget.isValid()){
			modalView.showError(SEE_ERRORS_ABOVE);
			return;
		}
		modalView.hideError();
		modalView.setProcessing(true);
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
				modalView.hideModal();
				callback.invoke();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				modalView.showError(caught.getMessage());
				modalView.setProcessing(false);
			}
		});
	}

	@Override
	public void showEditProfile(String userId, Callback callback) {
		this.callback = callback;
		modalView.showModal();
		modalView.setLoading(true);
		modalView.setProcessing(false);
		modalView.hideError();
		synapse.getUserProfile(userId, new AsyncCallback<UserProfile>() {
			
			@Override
			public void onSuccess(UserProfile profile) {
				originalProfile = profile;
				editorWidget.configure(profile);
				modalView.setLoading(false);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				modalView.setLoading(false);
				modalView.showError(caught.getMessage());
			}
		});
	}

}
