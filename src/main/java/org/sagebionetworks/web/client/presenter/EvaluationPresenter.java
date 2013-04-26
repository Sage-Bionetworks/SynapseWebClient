package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.evaluation.model.UserEvaluationState;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Evaluation;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EvaluationView;
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
	
	@Inject
	public EvaluationPresenter(EvaluationView view, SynapseClientAsync synapseClient, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState){
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
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
		try {
			if (!authenticationController.isLoggedIn()) {
				//go to login page
				goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			}
			else {
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
		} catch (RestServiceException e) {
			view.showErrorMessage(DisplayConstants.EVALUATION_REGISTRATION_ERROR + e.getMessage());
		}
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
