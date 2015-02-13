package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubmitToEvaluationWidget implements SubmitToEvaluationWidgetView.Presenter, WidgetRendererPresenter {
	
	private SubmitToEvaluationWidgetView view;
	private Map<String,String> descriptor;
	private WikiPageKey wikiKey;
	private AuthenticationController authenticationController;
	private ChallengeClientAsync challengeClient;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private String[] evaluationIds;
	PortalGinInjector ginInjector;
	
	@Inject
	public SubmitToEvaluationWidget(SubmitToEvaluationWidgetView view, ChallengeClientAsync challengeClient,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			NodeModelCreator nodeModelCreator,
			PortalGinInjector ginInjector) {
		this.view = view;
		view.setPresenter(this);
		this.challengeClient = challengeClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.nodeModelCreator = nodeModelCreator;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
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
		final String evaluationUnavailableMessage = descriptor.get(WidgetConstants.UNAVAILABLE_MESSAGE);
		final String buttonText = descriptor.get(WidgetConstants.BUTTON_TEXT_KEY);
		
		//figure out if we should show anything
		try {
			final Set<String> targetEvaluations = new HashSet<String>();
			for (int i = 0; i < evaluationIds.length; i++) {
				targetEvaluations.add(evaluationIds[i]);
			}
			challengeClient.getAvailableEvaluations(targetEvaluations, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String jsonString) {
					try {
						PaginatedResults<Evaluation> results = nodeModelCreator.createPaginatedResults(jsonString, Evaluation.class);
						view.configure(wikiKey, results.getTotalNumberOfResults() > 0, evaluationUnavailableMessage, buttonText);	
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					//if the user can't read the evaluation, then don't show the join button.  if there was some other error, then report it...
					if (!(caught instanceof ForbiddenException)) {
						view.showErrorMessage(DisplayConstants.EVALUATION_SUBMISSION_ERROR + caught.getMessage());
					}
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(DisplayConstants.EVALUATION_SUBMISSION_ERROR + e.getMessage());
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
	public void gotoLoginPage() {
		goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
	}
	
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void submitToChallengeClicked() {
		if (!authenticationController.isLoggedIn()) {
			//go to login page
			view.showAnonymousRegistrationMessage();
			//directs to the login page
		} else
			showSubmissionDialog();
	}
	
	public void showSubmissionDialog() {
		EvaluationSubmitter submitter = ginInjector.getEvaluationSubmitter();
		view.setEvaluationSubmitterWidget(submitter.asWidget());
		Set<String> evaluationIdsList = new HashSet<String>();
		for (int i = 0; i < evaluationIds.length; i++) {
			evaluationIdsList.add(evaluationIds[i]);
		}
		submitter.configure(null, evaluationIdsList);
	}
	
}
