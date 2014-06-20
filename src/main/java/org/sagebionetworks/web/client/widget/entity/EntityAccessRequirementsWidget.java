package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget for displaying the access requirements associated with an entity
 */
public class EntityAccessRequirementsWidget implements EntityAccessRequirementsWidgetView.Presenter {
	private EntityAccessRequirementsWidgetView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private String entityId;
	private CallbackP<Boolean> mainCallback;
	private List<AccessRequirement> accessRequirements;
	private int currentAccessRequirement;
	
	@Inject
	public EntityAccessRequirementsWidget(EntityAccessRequirementsWidgetView view, 
			SynapseClientAsync synapseClient, 
			AuthenticationController authenticationController, 
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter
			) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	/**
	 * @param entityId ask for access requirements associated with this entity id
	 * @param acceptedAllCallback Will callback with true if all ARs have been accepted
	 */
	public void showUploadAccessRequirements(String entityId, CallbackP<Boolean> acceptedAllCallback) {
		this.mainCallback = acceptedAllCallback;
		this.entityId = entityId;
		//ask for access requirements of the specified type and show them
		showAccessRequirementsStep1();
	};

	//first, check if logged in
	public void showAccessRequirementsStep1() {
		currentAccessRequirement = 0;
		accessRequirements = new ArrayList<AccessRequirement>();
		boolean isLoggedIn = authenticationController.isLoggedIn();
		if (isLoggedIn) {
			showAccessRequirementsStep2();
		} else {
			//not logged in
			mainCallback.invoke(false);
		}
	}
	public void showAccessRequirementsStep2() {
		synapseClient.getAllEntityUploadAccessRequirements(entityId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//are there access restrictions?
				try{
					PaginatedResults<AccessRequirement> ar = nodeModelCreator.createPaginatedResults(result, TermsOfUseAccessRequirement.class);
					accessRequirements = ar.getResults();
					//if there's anything to show, then show the wizard
					if (accessRequirements.size() > 0)
						view.showWizard();

					showAccessRequirementsStep3();
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				mainCallback.invoke(false);
			}
		});
	}
	
	/**
	 * The access requirements have been set.  Show them until we're finished
	 */
	public void showAccessRequirementsStep3() {
		if (currentAccessRequirement >= accessRequirements.size()) {
			//done showing access requirements
			view.hideWizard();
			mainCallback.invoke(true);
		} else {
			final AccessRequirement accessRequirement = accessRequirements.get(currentAccessRequirement);
			String text = GovernanceServiceHelper.getAccessRequirementText(accessRequirement);
			Callback termsOfUseCallback = new Callback() {
				@Override
				public void invoke() {
					//agreed to terms of use.
					currentAccessRequirement++;
					setLicenseAccepted(accessRequirement.getId());
				}
			};
			//pop up the requirement
			view.updateWizardProgress(currentAccessRequirement, accessRequirements.size());
			view.showAccessRequirement(text, termsOfUseCallback);
		}
	}
	
	@Override
	public void wizardCanceled() {
		mainCallback.invoke(false);
	}
	
	public void setLicenseAccepted(Long arId) {	
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showErrorMessage(t.getMessage());
				mainCallback.invoke(false);
			}
		};
		
		Callback onSuccess = new Callback() {
			@Override
			public void invoke() {
				//AR accepted, continue
				showAccessRequirementsStep3();
			}
		};
		
		GovernanceServiceHelper.signTermsOfUse(
				authenticationController.getCurrentUserPrincipalId(), 
				arId, 
				onSuccess, 
				onFailure, 
				synapseClient, 
				jsonObjectAdapter);
	}
	
	public void clear() {
		view.clear();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
}
