package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class JoinWidget implements JoinWidgetView.Presenter, WidgetRendererPresenter {
	
	private JoinWidgetView view;
	private Map<String,String> descriptor;
	private WikiPageKey wikiKey;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private String evaluationId;
	
	@Inject
	public JoinWidget(JoinWidgetView view, SynapseClientAsync synapseClient, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor) {
		this.wikiKey = wikiKey;
		this.descriptor = widgetDescriptor;
		evaluationId = descriptor.get(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY);
		//figure out if we should show anything
		try {
			synapseClient.getUserEvaluationState(evaluationId, new AsyncCallback<UserEvaluationState>() {
				@Override
				public void onSuccess(UserEvaluationState state) {
					view.configure(wikiKey, state);		
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showError(DisplayConstants.EVALUATION_USER_STATE_ERROR + caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showError(DisplayConstants.EVALUATION_USER_STATE_ERROR + e.getMessage());
		}
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void register() {
		registerStep1();
	}

	/**
	 * Check that the user is logged in
	 */
	public void registerStep1() {
		try {
			if (!authenticationController.isLoggedIn()) {
				//go to login page
				goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			}
			else {
				registerStep2();
			}
		} catch (RestServiceException e) {
			view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + e.getMessage());
		}
	}
	
	/**
	 * Check for unmet access restrictions. As long as more exist, it will keep calling itself until all restrictions are approved.
	 * Will not proceed to step3 (joining the challenge) until all have been approved.
	 * @throws RestServiceException
	 */
	public void registerStep2() throws RestServiceException {
		synapseClient.getUnmetEvaluationAccessRequirements(evaluationId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				//are there unmet access restrictions?
				try{
					PaginatedResults<TermsOfUseAccessRequirement> ar = nodeModelCreator.createPaginatedResults(result, TermsOfUseAccessRequirement.class);
					if (ar.getTotalNumberOfResults() > 0) {
						//there are unmet access requirements.  user must accept all before joining the challenge
						List<TermsOfUseAccessRequirement> unmetRequirements = ar.getResults();
						final AccessRequirement firstUnmetAccessRequirement = unmetRequirements.get(0);
						String text = GovernanceServiceHelper.getAccessRequirementText(firstUnmetAccessRequirement);
						Callback termsOfUseCallback = new Callback() {
							@Override
							public void invoke() {
								//agreed to terms of use.
								setLicenseAccepted(firstUnmetAccessRequirement.getId());
							}
						};
						//pop up the requirement
						view.showAccessRequirement(text, termsOfUseCallback);
					} else
						registerStep3();
				} catch (Throwable e) {
					onFailure(e);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + caught.getMessage());
			}
		});
	}
	
	public void setLicenseAccepted(Long arId) {	
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + t.getMessage());
			}
		};
		
		Callback onSuccess = new Callback() {
			@Override
			public void invoke() {
				//ToU signed, now try to register for the challenge (will check for other unmet access restrictions before join)
				try {
					registerStep2();
				} catch (RestServiceException e) {
					onFailure.invoke(e);
				}
			}
		};
		
		GovernanceServiceHelper.signTermsOfUse(
				authenticationController.getLoggedInUser().getProfile().getOwnerId(), 
				arId, 
				onSuccess, 
				onFailure, 
				synapseClient, 
				jsonObjectAdapter);
	}
	
	/**
	 * Join the evaluation
	 * @throws RestServiceException
	 */
	public void registerStep3() throws RestServiceException {
		synapseClient.createParticipant(evaluationId, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo("Successfully Joined!", "");
				configure(wikiKey, descriptor);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + caught.getMessage());
			}
		});
	}

	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
}
