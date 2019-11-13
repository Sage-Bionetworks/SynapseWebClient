package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.shared.FormParams;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubmitToEvaluationWidget implements SubmitToEvaluationWidgetView.Presenter, WidgetRendererPresenter {

	private SubmitToEvaluationWidgetView view;
	private Map<String, String> descriptor;
	private AuthenticationController authenticationController;
	private ChallengeClientAsync challengeClient;
	private GlobalApplicationState globalApplicationState;
	private Set<String> evaluationIds;
	private String formContainerId;
	private String formSchemaId;
	private String formUiSchemaId;
	PortalGinInjector ginInjector;
	private String evaluationUnavailableMessage;
	private SynapseJavascriptClient jsClient;
	@Inject
	public SubmitToEvaluationWidget(SubmitToEvaluationWidgetView view, SynapseJavascriptClient jsClient, ChallengeClientAsync challengeClient, AuthenticationController authenticationController, GlobalApplicationState globalApplicationState, PortalGinInjector ginInjector) {
		this.view = view;
		view.setPresenter(this);
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.jsClient = jsClient;
		this.ginInjector = ginInjector;
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		formContainerId = descriptor.get(WidgetConstants.FORM_CONTAINER_ID_KEY);
		formSchemaId = descriptor.get(WidgetConstants.JSON_SCHEMA_ID_KEY);
		formUiSchemaId = descriptor.get(WidgetConstants.UI_SCHEMA_ID_KEY);
		evaluationUnavailableMessage = descriptor.get(WidgetConstants.UNAVAILABLE_MESSAGE);

		String evaluationId = descriptor.get(WidgetConstants.EVALUATION_ID_KEY);
		if (evaluationId != null) {
			evaluationIds = new HashSet<String>();
			evaluationIds.add(evaluationId);
		}
		getEvaluationIds(descriptor, new CallbackP<Set<String>>() {
			@Override
			public void invoke(Set<String> evalIds) {
				evaluationIds = evalIds;
				final String buttonText = descriptor.get(WidgetConstants.BUTTON_TEXT_KEY);
				// figure out if we should show anything
				jsClient.getAvailableEvaluations(evaluationIds, true, Integer.MAX_VALUE, 0, new AsyncCallback<List<Evaluation>>() {
					@Override
					public void onSuccess(List<Evaluation> results) {
						if (results.size() == 0) {
							view.showUnavailable(evaluationUnavailableMessage);
						} else {
							view.configure(buttonText);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						// if the user can't read the evaluation, then don't show the join button. if there was some other
						// error, then report it...
						if (!(caught instanceof ForbiddenException)) {
							view.showUnavailable(DisplayConstants.EVALUATION_SUBMISSION_ERROR + caught.getMessage());
						}
					}
				});
			}
		});
	}

	/**
	 * Get the evaluation queue ids
	 * 
	 * @param callback
	 */
	public void getEvaluationIds(Map<String, String> descriptor, final CallbackP<Set<String>> callback) {
		String subchallengeIdList = null;
		if (descriptor.containsKey(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY))
			subchallengeIdList = descriptor.get(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_KEY);
		if (subchallengeIdList != null) {
			String[] evaluationIdsArray = subchallengeIdList.split(WidgetConstants.JOIN_WIDGET_SUBCHALLENGE_ID_LIST_DELIMETER);
			Set<String> evaluationIds = new HashSet<String>();
			for (String evaluationId : evaluationIdsArray) {
				evaluationIds.add(evaluationId);
			}
			if (!evaluationIds.isEmpty())
				callback.invoke(evaluationIds);
			else {
				view.showUnavailable(evaluationUnavailableMessage);
			}
			return;
		} else {
			AsyncCallback<Set<String>> asyncCallback = new AsyncCallback<Set<String>>() {
				@Override
				public void onSuccess(Set<String> evaluationIds) {
					// if no evaluations are accessible, do not continue (show nothing)
					if (!evaluationIds.isEmpty())
						callback.invoke(evaluationIds);
					else
						view.showUnavailable(evaluationUnavailableMessage);
				};

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.CHALLENGE_EVALUATIONS_ERROR + caught.getMessage());
				}
			};

			if (descriptor.containsKey(WidgetConstants.CHALLENGE_ID_KEY)) {
				// else, look for the challenge id
				String challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
				challengeClient.getChallengeEvaluationIds(challengeId, asyncCallback);
			} else if (descriptor.containsKey(WidgetConstants.PROJECT_ID_KEY)) {
				// else, look for the project id
				String projectId = descriptor.get(WidgetConstants.PROJECT_ID_KEY);
				challengeClient.getProjectEvaluationIds(projectId, asyncCallback);
			} else if (!evaluationIds.isEmpty()) {
				callback.invoke(evaluationIds);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

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
			// go to login page
			view.showAnonymousRegistrationMessage();
			// directs to the login page
		} else
			showSubmissionDialog();
	}

	public void showSubmissionDialog() {
		FormParams formParams = null;
		if (formContainerId != null) {
			formParams = new FormParams(formContainerId, formSchemaId, formUiSchemaId);
		}
		EvaluationSubmitter submitter = ginInjector.getEvaluationSubmitter();
		view.setEvaluationSubmitterWidget(submitter.asWidget());
		submitter.configure(null, evaluationIds, formParams);
	}

}
