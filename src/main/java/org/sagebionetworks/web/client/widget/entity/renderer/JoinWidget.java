package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.factory.SystemFactory;
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
	private String[] evaluationIds;
	SystemFactory systemFactory;
	
	@Inject
	public JoinWidget(JoinWidgetView view, SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter,
			SystemFactory systemFactory) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.systemFactory = systemFactory;
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor) {
		this.wikiKey = wikiKey;
		this.descriptor = widgetDescriptor;
		
		String evaluationId = descriptor.get(WidgetConstants.JOIN_WIDGET_EVALUATION_ID_KEY);
		if (evaluationId != null) {
			evaluationIds = new String[1];
			evaluationIds[0] = evaluationId;
		}
		String subchallengeIdList = null;
		if(descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY)) subchallengeIdList = descriptor.get(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		if(subchallengeIdList != null) {
			evaluationIds = subchallengeIdList.split(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER);
		}
		
		//figure out if we should show anything
		try {
			synapseClient.getUserEvaluationState(evaluationIds[0], new AsyncCallback<UserEvaluationState>() {
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
		if (!systemFactory.getCookieHelper().isLoggedIn()) {
			//go to login page
			view.showAnonymousRegistrationMessage();
			//directs to the login page
		}
		else {
			registerStep2();
		}
	}
	
	/**
	 * Gather additional info about the logged in user
	 */
	public void registerStep2() {
		//pop up profile form.  user does not have to fill in info
		view.showProfileForm(authenticationController.getLoggedInUser().getProfile(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				continueToStep3();
			}
			@Override
			public void onFailure(Throwable caught) {
				continueToStep3();
			}
			
			public void continueToStep3(){
				try{
					registerStep3(0);	
				} catch (RestServiceException e) {
					view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + e.getMessage());
				}			
			}
		});
	}
	
	/**
	 * Check for unmet access restrictions. As long as more exist, it will keep calling itself until all restrictions are approved.
	 * Will not proceed to step3 (joining the challenge) until all have been approved.
	 * @throws RestServiceException
	 */
	public void registerStep3(final int evalIndex) throws RestServiceException {
		synapseClient.getUnmetEvaluationAccessRequirements(evaluationIds[evalIndex], new AsyncCallback<String>() {
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
								setLicenseAccepted(firstUnmetAccessRequirement.getId(), evalIndex);
							}
						};
						//pop up the requirement
						view.showAccessRequirement(text, termsOfUseCallback);
					} else {
						if (evalIndex == evaluationIds.length - 1)
							registerStep4();
						else
							registerStep3(evalIndex+1);
					}
						
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
	
	public void setLicenseAccepted(Long arId, final int evalIndex) {	
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
					registerStep3(evalIndex);
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
	public void registerStep4() throws RestServiceException {
		//create participants
		synapseClient.createParticipants(evaluationIds, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Successfully Joined!", "");
				configure(wikiKey, descriptor);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showError(DisplayConstants.EVALUATION_REGISTRATION_ERROR + caught.getMessage());
			}
		});
	}

	@Override
	public void gotoLoginPage() {
		goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
	
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
}
