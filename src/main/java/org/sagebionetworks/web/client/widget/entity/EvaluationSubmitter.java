package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
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
	private String submissionEntityId;
	private Long submissionEntityVersion;
	
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
						view.popupSelector(submissionEntity == null, evaluations);
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
	
		
	@Override
	public void submitToEvaluations(Reference selectedReference, List<Evaluation> evaluations) {
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
		
		 //Check access requirements for evaluations before moving on with submission
		 try {
			 checkForUnmetRequirements(0, evaluations);
		 } catch(RestServiceException e) {
			 view.showErrorMessage(DisplayConstants.EVALUATION_SUBMISSION_ERROR + e.getMessage());
		 }
	}
	
	/**
	* Check for unmet access restrictions. As long as more exist, it will not move onto submissions.
	* @throws RestServiceException
	*/
	public void checkForUnmetRequirements(final int evalIndex, final List<Evaluation> evaluations) throws RestServiceException {
		synapseClient.getUnmetEvaluationAccessRequirements(evaluations.get(evalIndex).getId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<TermsOfUseAccessRequirement> ar = nodeModelCreator.createPaginatedResults(result, TermsOfUseAccessRequirement.class);
					if (ar.getTotalNumberOfResults() > 0) {
						//there are unmet access requirements.  user must accept all
						List<TermsOfUseAccessRequirement> unmetRequirements = ar.getResults();
						final AccessRequirement firstUnmetAccessRequirement = unmetRequirements.get(0);
						String text = GovernanceServiceHelper.getAccessRequirementText(firstUnmetAccessRequirement);
						Callback termsOfUseCallback = new Callback() {
							@Override
							public void invoke() {
								//agreed to terms of use.
								setLicenseAccepted(firstUnmetAccessRequirement.getId(), evalIndex, evaluations);
							}
						};
						//pop up the requirement
						view.showAccessRequirement(text, termsOfUseCallback);
					} else {
						if (evalIndex != evaluations.size() - 1) {
							checkForUnmetRequirements(evalIndex+1, evaluations);
						} else {
							//we have gone through all unmet access requirements for all evaluations.
							lookupEtagAndCreateSubmission(submissionEntityId, submissionEntityVersion, evaluations);
						}
					}
				} catch(Throwable e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.EVALUATION_SUBMISSION_ERROR+ caught.getMessage());
			}
		});
	}
	
	public void setLicenseAccepted(Long	arId, final int evalIndex, final List<Evaluation> evaluations) {	
		final CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showErrorMessage(DisplayConstants.EVALUATION_SUBMISSION_ERROR + t.getMessage());
			}
		};
		
		Callback onSuccess = new Callback() {
			@Override
			public void invoke() {
				//ToU signed, now try to submit evaluations (checks for other unmet access restrictions before submission)
				try {
					checkForUnmetRequirements(evalIndex, evaluations);
				} catch (RestServiceException e) {
					onFailure.invoke(e);
				}
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
	
	public void lookupEtagAndCreateSubmission(final String id, final Long ver, final List<Evaluation> evaluations) {
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
						//not versionable, the service will not accept
						onFailure(new IllegalArgumentException(DisplayConstants.SUBMIT_VERSIONABLE_ENTITY_MESSAGE));
						return;
					}
					view.hideWindow();
					submitToEvaluations(id, v, entity.getEtag(), evaluations);
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
	
	public void submitToEvaluations(String entityId, Long versionNumber, String etag, List<Evaluation> evaluations) {
		//set up shared values across all submissions
		Submission newSubmission = new Submission();
		newSubmission.setEntityId(entityId);
		newSubmission.setUserId(authenticationController.getCurrentUserPrincipalId());
		newSubmission.setVersionNumber(versionNumber);
		if (evaluations.size() > 0)
			submitToEvaluations(newSubmission, etag, evaluations, 0);
	}
	
	public void submitToEvaluations(final Submission newSubmission, final String etag, final List<Evaluation> evaluations, final int index) {
		//and create a new submission for each evaluation
		Evaluation evaluation = evaluations.get(index);
		newSubmission.setEvaluationId(evaluation.getId());
		JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
		try {
			newSubmission.writeToJSONObject(adapter);
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
		}
		try {
//			//TODO: add the content source as a fav instead of My Challenges area
//			synapseClient.addFavorite(evaluation.getContentSource(), new AsyncCallback<String>() {			
//				@Override
//				public void onSuccess(String result) {
//				}
//				@Override
//				public void onFailure(Throwable caught) {
//					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
//						view.showErrorMessage(caught.getMessage());
//				}
//			});
			synapseClient.createSubmission(adapter.toJSONString(), etag, new AsyncCallback<String>() {			
				@Override
				public void onSuccess(String result) {
					//result is the updated submission
					if (index == evaluations.size()-1) {
						HashSet<String> replyMessages = new HashSet<String>();
						for (Evaluation eval : evaluations) {
							String message = eval.getSubmissionReceiptMessage();
							if (message == null || message.length()==0)
								message = DisplayConstants.SUBMISSION_RECEIVED_TEXT;
							replyMessages.add(message);
						}
						
						view.showSubmissionAcceptedDialogs(replyMessages);
					} else {
						submitToEvaluations(newSubmission, etag, evaluations, index+1);
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
