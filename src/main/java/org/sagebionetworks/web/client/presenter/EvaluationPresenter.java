package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.view.EvaluationView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class EvaluationPresenter extends AbstractActivity implements EvaluationView.Presenter {
		
	private Evaluation place;
	private EvaluationView view;
	private SynapseClientAsync synapseClient;
	private String evaluationId;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	
	@Inject
	public EvaluationPresenter(EvaluationView view, SynapseClientAsync synapseClient, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter){
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	public void setPlace(Evaluation place) {
		this.place = place;
		this.view.setPresenter(this);
		
		configure(place.toToken());
	}
	
	@Override
	public void configure(final String evaluationId) {
		this.evaluationId = evaluationId;
		synapseClient.hasAccess(evaluationId, ObjectType.EVALUATION.toString(), ACCESS_TYPE.UPDATE.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(final Boolean canEdit) {
				try {
					synapseClient.getUserEvaluationState(evaluationId, new AsyncCallback<UserEvaluationState>() {
						@Override
						public void onSuccess(UserEvaluationState state) {
							view.showPage(new WikiPageKey(evaluationId, ObjectType.EVALUATION.toString(), null), state, canEdit);		
						}
						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(DisplayConstants.EVALUATION_USER_STATE_ERROR + caught.getMessage());
						}
					});
				} catch (RestServiceException e) {
					view.showErrorMessage(DisplayConstants.EVALUATION_USER_STATE_ERROR + e.getMessage());
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.EVALUATION_USER_ACCESS_ERROR + caught.getMessage());
			}
		});
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
			view.showErrorMessage(DisplayConstants.EVALUATION_REGISTRATION_ERROR + e.getMessage());
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
				view.showErrorMessage(DisplayConstants.EVALUATION_REGISTRATION_ERROR + caught.getMessage());
			}
		});
	}
	
	public void setLicenseAccepted(Long arId) {	
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showErrorMessage(DisplayConstants.EVALUATION_REGISTRATION_ERROR + t.getMessage());
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
				configure(evaluationId);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.EVALUATION_REGISTRATION_ERROR + caught.getMessage());
			}
		});
	}

	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
