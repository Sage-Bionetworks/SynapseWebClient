package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.AttachmentsView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UsernameTextField implements UsernameTextFieldView.Presenter {

	private UsernameTextFieldView view;
	private SynapseClientAsync synapseClient;

	//keep track of the validated username in the presenter (clear out before revalidating)
	private String validatedUsername;
	
	/**
	 * Widget for defining a new username.  On key press, will check to see if username is taken.
	 */
	@Inject
	public UsernameTextField(UsernameTextFieldView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
	}

	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void configure(String initUsername) {
		view.configure(initUsername);
	}
	
	public boolean validate() {
		return view.validate();
	}
	
	public String getValidatedUsername() {
		return validatedUsername;
	}
	
	@Override
	public void validateUsername(final String username) {
		validatedUsername = null;
		synapseClient.isUniqueUsername(username, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isValid) {
				if (isValid) {
					validatedUsername = username;
				}
				view.setIsUniqueUsername(isValid);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
