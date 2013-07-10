package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitterView.Presenter;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EvaluationSubmitter implements Presenter {

	private EvaluationSubmitterView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private JSONObjectAdapter jsonObjectAdapter;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private Entity submissionEntity;
	
	@Inject
	public EvaluationSubmitter(EvaluationSubmitterView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}
	
	/**
	 * 
	 * @param submissionEntity set to null if an entity finder should be shown
	 * @param evaluationIds set to null if we should query for all available evaluations
	 */
	public void configure(Entity submissionEntity, List<String> evaluationIds) {
		view.showLoading();
		this.submissionEntity = submissionEntity;
		try {
			if (evaluationIds == null)
				synapseClient.getAvailableEvaluations(getEvalCallback());
			else
				synapseClient.getEvaluations(evaluationIds, getEvalCallback());
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	private AsyncCallback<String> getEvalCallback() {
		return new AsyncCallback<String>() {
			@Override
			public void onSuccess(String jsonString) {
				try {
					PaginatedResults<Evaluation> results = nodeModelCreator.createPaginatedResults(jsonString, Evaluation.class);
					List<Evaluation> evaluations = results.getResults();
					if (evaluations == null || evaluations.size() == 0) {
						//no available evaluations, pop up an info dialog
						view.showErrorMessage(DisplayConstants.NOT_PARTICIPATING_IN_ANY_EVALUATIONS);
					} 
					else {
						getAvailableEvaluationsSubmitterAliases(evaluations);
					}
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
				view.showErrorMessage(caught.getMessage());
			}
		};
	}
	
	
	public void getAvailableEvaluationsSubmitterAliases(final List<Evaluation> evaluations) {
		try {
			synapseClient.getAvailableEvaluationsSubmitterAliases(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String jsonString) {
					try {
						RestResourceList results = nodeModelCreator.createJSONEntity(jsonString, RestResourceList.class);
						List<String> submitterAliases = results.getList();
						//add the default team name (if set in the profile and not already in the list)
						UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
						String teamName = sessionData.getProfile().getTeamName();
						if (teamName != null && teamName.length() > 0 && !submitterAliases.contains(teamName)) {
							submitterAliases.add(teamName);
						}
						view.popupSelector(submissionEntity == null, evaluations, submitterAliases);	
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	
	@Override
	public void submitToEvaluations(Reference selectedReference, final List<String> evaluationIds, final String submitterAlias) {
		//in any case look up the entity (to make sure we have the most recent version, for the current etag
		String entityId;
		Long version = null;
		if (submissionEntity != null) {
			entityId = submissionEntity.getId();
			if (submissionEntity instanceof Versionable)
				version = ((Versionable)submissionEntity).getVersionNumber();
		}
		else {
			entityId = selectedReference.getTargetId();
			version = selectedReference.getTargetVersionNumber();
		}
		
		final String id = entityId;
		final Long ver = version;
		
		//look up entity for the current etag
		synapseClient.getEntity(selectedReference.getTargetId(), new AsyncCallback<EntityWrapper>() {
			public void onSuccess(EntityWrapper result) {
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					submitToEvaluations(id, ver, entity.getEtag(), evaluationIds, submitterAlias);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void submitToEvaluations(String entityId, Long versionNumber, String etag, List<String> evaluationIds, String submitterAlias) {
		//set up shared values across all submissions
		Submission newSubmission = new Submission();
		newSubmission.setEntityId(entityId);
		newSubmission.setSubmitterAlias(submitterAlias);
		newSubmission.setUserId(authenticationController.getCurrentUserPrincipalId());
		newSubmission.setVersionNumber(versionNumber);
		if (evaluationIds.size() > 0)
			submitToEvaluations(newSubmission, etag, evaluationIds, 0);
	}
	
	public void submitToEvaluations(final Submission newSubmission, final String etag, final List<String> evaluationIds, final int index) {
		//and create a new submission for each evaluation
		String evalId = evaluationIds.get(index);
		newSubmission.setEvaluationId(evalId);
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			newSubmission.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}
		try {
			synapseClient.createSubmission(adapter.toJSONString(), etag, new AsyncCallback<String>() {			
				@Override
				public void onSuccess(String result) {
					//result is the updated submission
					if (index == evaluationIds.size()-1) {
						view.showInfo(DisplayConstants.SUBMITTED_TITLE, DisplayConstants.SUBMITTED_TO_EVALUATION);				
					} else {
						submitToEvaluations(newSubmission, etag, evaluationIds, index+1);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
						view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
}
