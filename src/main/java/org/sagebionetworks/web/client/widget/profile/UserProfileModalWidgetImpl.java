package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileModalWidgetImpl implements UserProfileModalWidget {
	
	public static final String SEE_ERRORS_ABOVE = "See errors above.";
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
		this.modalView.setPresenter(this);
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
		updateProfileFromEditor();
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

	/**
	 * Update the profile from the view.
	 * @return
	 */
	public UserProfile updateProfileFromEditor() {
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
		return originalProfile;
	}

	@Override
	public void showEditProfile(String userId, Callback callback) {
		showEditor(userId, null, callback);
	}
	
	@Override
	public void showEditProfile(String userId, UserProfile importedProfile, Callback callback) {
		showEditor(userId, importedProfile, callback);
	}

	private void showEditor(String userId, final UserProfile imported,  Callback callback) {
		this.callback = callback;
		modalView.showModal();
		modalView.setLoading(true);
		modalView.setProcessing(false);
		modalView.hideError();
		synapse.getUserProfile(userId, new AsyncCallback<UserProfile>() {
			
			@Override
			public void onSuccess(UserProfile profile) {
				// Merge the imported into the fetched profile
				originalProfile = mergeFirstIntoSecond(imported, profile);
				editorWidget.configure(originalProfile);
				modalView.setLoading(false);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				modalView.setLoading(false);
				modalView.showError(caught.getMessage());
			}
		});
	}

	/**
	 * Merge some elements from the first profile into the second.
	 * @param first
	 * @param second
	 * @return The second profile is returned.
	 */
	public static UserProfile mergeFirstIntoSecond(UserProfile first, UserProfile second){
		if(first == null){
			return second;
		}
		if(first.getFirstName() != null){
			second.setFirstName(first.getFirstName());
		}
		if(first.getLastName() != null){
			second.setLastName(first.getLastName());
		}
		if(first.getSummary() != null){
			second.setSummary(first.getSummary());
		}
		if(first.getPosition() != null){
			second.setPosition(first.getPosition());
		}
		if(first.getLocation() != null){
			second.setLocation(first.getLocation());
		}
		if(first.getIndustry() != null){
			second.setIndustry(first.getIndustry());
		}
		if(first.getCompany() != null){
			second.setCompany(first.getCompany());
		}
		if(first.getProfilePicureFileHandleId() != null){
			second.setProfilePicureFileHandleId(first.getProfilePicureFileHandleId());
		}
		return second;
	}

}
