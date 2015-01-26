package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TeamHeader;
import org.sagebionetworks.repo.model.Versionable;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmitter implements Presenter {

	private EvaluationSubmitterView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private Entity submissionEntity;
	private String submissionEntityId, submissionName;
	private Long submissionEntityVersion;
	private List<Evaluation> evaluations;
	List<TeamHeader> teams;
	
	@Inject
	public EvaluationSubmitter(EvaluationSubmitterView view,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}
	
	/**
	 * 
	 * @param submissionEntity set to null if an entity finder should be shown
	 * @param evaluationIds set to null if we should query for all available evaluations
	 */
	public void configure(Entity submissionEntity, Set<String> evaluationIds) {
		view.showLoading();
		this.submissionEntity = submissionEntity;
		try {
			if (evaluationIds == null)
				synapseClient.getAvailableEvaluations(getEvalCallback());
			else
				synapseClient.getAvailableEvaluations(evaluationIds, getEvalCallback());
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
						view.showModal1(submissionEntity == null, evaluations);
					}
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		};
	}
	
		
	@Override
	public void nextClicked(Reference selectedReference, String submissionName, List<Evaluation> evaluations) {
		//in any case look up the entity (to make sure we have the most recent version, for the current etag
		submissionEntityVersion = null;
		if (submissionEntity != null) {
			submissionEntityId = submissionEntity.getId();
			if (submissionEntity instanceof Versionable)
				submissionEntityVersion = ((Versionable)submissionEntity).getVersionNumber();
		}
		else {
			submissionEntityId = selectedReference.getTargetId();
			submissionEntityVersion = selectedReference.getTargetVersionNumber();
		}
		this.submissionName = submissionName;
		this.evaluations = evaluations;
		//The standard is to attach access requirements to the associated team, and show them when joining the team.
		//So access requirements are not checked here.
		view.hideModal1();
		getAvailableTeams();
	}
	
	public void getAvailableTeams() {
		teams = new ArrayList<TeamHeader>();
		synapseClient.getAvailableSubmissionTeams(getTeamsCallback());
	}
	
	private AsyncCallback<String> getTeamsCallback() {
		return new AsyncCallback<String>() {
			@Override
			public void onSuccess(String jsonString) {
				try {
					PaginatedResults<TeamHeader> results = nodeModelCreator.createPaginatedResults(jsonString, TeamHeader.class);
					teams = results.getResults();
					view.showModal2(teams);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		};
	}
	
	@Override
	public void doneClicked(String selectedTeamName) {
		//resolve team id from the selected team name
		String selectedTeamId = null;
		if (!view.isIndividual()) {
			//team
			if (selectedTeamName == null) {
				view.showErrorMessage("Please select a team.");
				return;
			}
			//resolve team name
			for (TeamHeader team : teams) {
				if(selectedTeamName.equals(team.getName())) {
					selectedTeamId = team.getId();
					break;
				}
			}

			if (selectedTeamId == null) {
				view.showErrorMessage("Unable to find the team in the team list: " + selectedTeamName);
				return;
			}
		}
		lookupEtagAndCreateSubmission(submissionEntityId, submissionEntityVersion, evaluations, selectedTeamId);
	}
	public void lookupEtagAndCreateSubmission(final String id, final Long ver, final List<Evaluation> evaluations, final String selectedTeamId) {
		//look up entity for the current etag
		synapseClient.getEntity(id, new AsyncCallback<EntityWrapper>() {
			public void onSuccess(EntityWrapper result) {
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					Long v = null;
					if (ver != null)
						v = ver;
					else if (entity instanceof Versionable)
						v = ((Versionable)entity).getVersionNumber();
					 else {
						 //entity is not versionable, the service will not accept null, but will accept a version of 1
						v = 1L;
					 }
						 
					submitToEvaluations(id, v, entity.getEtag(), selectedTeamId, evaluations);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void submitToEvaluations(String entityId, Long versionNumber, String etag, String selectedTeamId, List<Evaluation> evaluations) {
		//set up shared values across all submissions
		Submission newSubmission = new Submission();
		newSubmission.setEntityId(entityId);
		newSubmission.setUserId(authenticationController.getCurrentUserPrincipalId());
		newSubmission.setTeamId(selectedTeamId);
		newSubmission.setVersionNumber(versionNumber);
		if (submissionName != null && submissionName.trim().length() > 0)
			newSubmission.setName(submissionName);
		
		if (evaluations.size() > 0)
			submitToEvaluations(newSubmission, etag, evaluations, 0);
	}
	
	public void submitToEvaluations(final Submission newSubmission, final String etag, final List<Evaluation> evaluations, final int index) {
		//and create a new submission for each evaluation
		Evaluation evaluation = evaluations.get(index);
		newSubmission.setEvaluationId(evaluation.getId());
		try {
			synapseClient.createSubmission(newSubmission, etag, new AsyncCallback<Submission>() {			
				@Override
				public void onSuccess(Submission result) {
					//result is the updated submission
					if (index == evaluations.size()-1) {
						HashSet<String> replyMessages = new HashSet<String>();
						for (Evaluation eval : evaluations) {
							String message = eval.getSubmissionReceiptMessage();
							if (message == null || message.length()==0)
								message = DisplayConstants.SUBMISSION_RECEIVED_TEXT;
							replyMessages.add(message);
						}
						view.hideModal2();
						view.showSubmissionAcceptedDialogs(replyMessages);
					} else {
						submitToEvaluations(newSubmission, etag, evaluations, index+1);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage(caught.getMessage());
				}
			});
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
}
