package org.sagebionetworks.web.client.widget.evaluation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.MemberSubmissionEligibility;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.SubmissionContributor;
import org.sagebionetworks.evaluation.model.SubmissionEligibility;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterView.Presenter;
import org.sagebionetworks.web.shared.FormParams;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationSubmitter implements Presenter {

	public static final String NO_COMMITS_SELECTED_MSG = "Please select a commit to submit.";
	public static final String ZERO_COMMITS_ERROR = "This repo does not have any commit. Please add commits to repo before submit to challenge.";
	private EvaluationSubmitterView view;
	private SynapseJavascriptClient jsClient;
	private ChallengeClientAsync challengeClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private GWTWrapper gwt;
	private DockerCommitListWidget dockerCommitList;
	private Entity submissionEntity;
	private String submissionEntityId, submissionName;
	private Long submissionEntityVersion;
	List<Team> teams;
	private Evaluation evaluation;
	private Challenge challenge;
	private Team selectedTeam;
	private String selectedTeamMemberStateHash;
	private List<Long> selectedTeamEligibleMembers;
	private SynapseAlert challengeListSynAlert;
	private SynapseAlert teamSelectSynAlert;
	private SynapseAlert contributorSynAlert;
	boolean isIndividualSubmission;
	private String dockerDigest;
	private Set<String> evaluationIds;
	private SynapseAlert dockerCommitSynAlert;
	private FormParams formParams;

	@Inject
	public EvaluationSubmitter(EvaluationSubmitterView view, SynapseJavascriptClient jsClient, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, ChallengeClientAsync challengeClient, GWTWrapper gwt, PortalGinInjector ginInjector, DockerCommitListWidget dockerCommitList) {
		this.view = view;
		this.view.setPresenter(this);
		this.jsClient = jsClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		this.gwt = gwt;
		this.dockerCommitList = dockerCommitList;
		this.challengeListSynAlert = ginInjector.getSynapseAlertWidget();
		this.teamSelectSynAlert = ginInjector.getSynapseAlertWidget();
		this.contributorSynAlert = ginInjector.getSynapseAlertWidget();
		this.dockerCommitSynAlert = ginInjector.getSynapseAlertWidget();
		this.view.setChallengesSynAlertWidget(challengeListSynAlert.asWidget());
		this.view.setTeamSelectSynAlertWidget(teamSelectSynAlert.asWidget());
		this.view.setContributorsSynAlertWidget(contributorSynAlert.asWidget());
		this.view.setDockerCommitSynAlert(dockerCommitSynAlert.asWidget());
		this.view.setDockerCommitList(dockerCommitList.asWidget());
	}

	/**
	 * 
	 * @param submissionEntity set to null if an entity finder should be shown
	 * @param evaluationIds set to null if we should query for all available evaluations
	 * @param formParams set to null if entity finder should be shown
	 */
	public void configure(Entity submissionEntity, Set<String> evaluationIds, FormParams formParams) {
		challenge = null;
		evaluation = null;
		selectedTeam = null;
		// initialize as an individual submission
		isIndividualSubmission = true;
		teams = new ArrayList<Team>();
		selectedTeamEligibleMembers = new ArrayList<Long>();
		challengeListSynAlert.clear();
		teamSelectSynAlert.clear();
		contributorSynAlert.clear();
		dockerCommitSynAlert.clear();
		view.resetNextButton();
		view.resetSubmitButton();
		view.setContributorsLoading(false);
		this.submissionEntity = submissionEntity;
		this.evaluationIds = evaluationIds;
		this.formParams = formParams;
		if (submissionEntity instanceof DockerRepository) {
			configureWithDockerCommit(submissionEntity);
		} else {
			configureWithEvaluations();
		}
	}

	public void configureWithDockerCommit(Entity submissionEntity) {
		dockerDigest = null;
		dockerCommitList.setEmptyListCallback(new Callback() {

			@Override
			public void invoke() {
				view.hideDockerCommitModal();
				view.showErrorMessage(ZERO_COMMITS_ERROR);
			}
		});
		dockerCommitList.configure(submissionEntity.getId(), true);
		view.showDockerCommitModal();
	}

	private void configureWithEvaluations() {
		jsClient.getAvailableEvaluations(evaluationIds, true, Integer.MAX_VALUE, 0, getEvalCallback());
	}

	@Override
	public void onIndividualSubmissionOptionClicked() {
		isIndividualSubmission = true;
		view.setIndividualSubmissionActive();
		view.hideTeamsUI();
	}

	@Override
	public void onTeamSubmissionOptionClicked() {
		isIndividualSubmission = false;
		view.setTeamSubmissionActive();
		if (teams.isEmpty()) {
			view.showEmptyTeams();
		} else {
			view.showTeamsUI(teams);
		}
	}

	private AsyncCallback<List<Evaluation>> getEvalCallback() {
		challengeListSynAlert.clear();
		AsyncCallback<List<Evaluation>> callback = new AsyncCallback<List<Evaluation>>() {
			@Override
			public void onSuccess(List<Evaluation> evaluations) {
				if (evaluations == null || evaluations.size() == 0) {
					// no available evaluations, pop up an info dialog
					view.showErrorMessage(DisplayConstants.NOT_PARTICIPATING_IN_ANY_EVALUATIONS);
				} else {
					view.showModal1(submissionEntity != null, formParams, evaluations);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				// modal 1
				challengeListSynAlert.handleException(caught);
			}
		};
		return callback;
	}


	@Override
	public void onNextClicked(Reference selectedReference, String submissionName, Evaluation evaluation) {
		// in any case look up the entity (to make sure we have the most recent version, for the current
		// etag
		submissionEntityVersion = null;
		if (submissionEntity != null) {
			submissionEntityId = submissionEntity.getId();
			if (submissionEntity instanceof Versionable)
				submissionEntityVersion = ((Versionable) submissionEntity).getVersionNumber();
		} else {
			submissionEntityId = selectedReference.getTargetId();
			submissionEntityVersion = selectedReference.getTargetVersionNumber();
		}
		this.submissionName = submissionName;
		this.evaluation = evaluation;
		// The standard is to attach access requirements to the associated team, and show them when joining
		// the team.
		// So access requirements are not checked again here.
		queryForChallenge();
	}

	/**
	 * Look for a challenge associated with the selected evaluation
	 */
	public void queryForChallenge() {
		view.setNextButtonLoading();
		challengeClient.getChallengeForProject(evaluation.getContentSource(), new AsyncCallback<Challenge>() {
			@Override
			public void onSuccess(Challenge result) {
				challenge = result;
				refreshRegisteredTeams();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.resetNextButton();
				// if challenge is not found, or if user has access to the evaluation queue but not the project
				// (messy setup)
				if (caught instanceof NotFoundException || caught instanceof ForbiddenException) {
					// no need to show second page, this is a submission to a non-challenge eval queue.
					onDoneClicked();
				} else {
					view.showErrorMessage("Error querying for associated challenge: " + caught.getMessage());
				}
			}
		});
	}

	@Override
	public void refreshRegisteredTeams() {
		challengeClient.getSubmissionTeams(authenticationController.getCurrentUserPrincipalId(), challenge.getId(), getTeamsCallback());
	}

	@Override
	public void onNewTeamClicked() {
		if (authenticationController.isLoggedIn())
			globalApplicationState.getPlaceChanger().goTo(new Profile(authenticationController.getCurrentUserPrincipalId() + Profile.DELIMITER + Synapse.ProfileArea.TEAMS));
		else {
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		}
	}

	@Override
	public void onRegisterTeamClicked() {
		view.showRegisterTeamDialog(challenge.getId());
	}

	private AsyncCallback<List<Team>> getTeamsCallback() {
		teamSelectSynAlert.clear();
		AsyncCallback<List<Team>> callback = new AsyncCallback<List<Team>>() {
			@Override
			public void onSuccess(List<Team> results) {
				view.clearTeams();
				teams = results;
				if (!teams.isEmpty()) {
					onTeamSelected(0);
					onTeamSubmissionOptionClicked();
				} else {
					onIndividualSubmissionOptionClicked();
				}

				view.hideModal1();
				view.showModal2();
			}

			@Override
			public void onFailure(Throwable caught) {
				// modal 2
				teamSelectSynAlert.handleException(caught);
			}
		};
		return callback;
	}

	@Override
	public void onDoneClicked() {
		view.hideModal1();
		view.setSubmitButtonLoading();
		if (!isIndividualSubmission) {
			// team submission
			if (selectedTeam == null) {
				view.showErrorMessage("Please select a team.");
				return;
			} else if (selectedTeamEligibleMembers.isEmpty()) {
				view.showErrorMessage("No eligible contributors on the selected team.");
				return;
			}
		}
		lookupEtagAndCreateSubmission(submissionEntityId, submissionEntityVersion);
	}

	@Override
	public void onTeamSelected(int selectedIndex) {
		selectedTeam = null;
		selectedTeamMemberStateHash = null;
		selectedTeamEligibleMembers.clear();
		view.clearContributors();
		view.setTeamInEligibleError("");
		// resolve team from team name
		if (selectedIndex >= 0 && selectedIndex < teams.size()) {
			selectedTeam = teams.get(selectedIndex);
			getContributorList(evaluation, selectedTeam);
		}
	}

	public void getContributorList(final Evaluation evaluation, final Team selectedTeam) {
		contributorSynAlert.clear();
		// get contributor list for this team
		view.setContributorsLoading(true);
		AsyncCallback<TeamSubmissionEligibility> callback = new AsyncCallback<TeamSubmissionEligibility>() {
			@Override
			public void onSuccess(TeamSubmissionEligibility teamEligibility) {
				view.setContributorsLoading(false);
				// is the team eligible???
				SubmissionEligibility teamSubmissionEligibility = teamEligibility.getTeamEligibility();
				if (!teamSubmissionEligibility.getIsEligible()) {
					// show the error
					String reason = ""; // unknown reason
					if (!teamSubmissionEligibility.getIsRegistered()) {
						reason = selectedTeam.getName() + " is not registered for this challenge. Please register this team, or select a different team.";
					} else if (teamSubmissionEligibility.getIsQuotaFilled()) {
						reason = selectedTeam.getName() + " has reached the submission quota.";
					}
					view.setTeamInEligibleError(reason);
				} else {
					selectedTeamMemberStateHash = teamEligibility.getEligibilityStateHash().toString();

					for (MemberSubmissionEligibility memberEligibility : teamEligibility.getMembersEligibility()) {
						if (memberEligibility.getIsEligible()) {
							selectedTeamEligibleMembers.add(memberEligibility.getPrincipalId());
							view.addEligibleContributor(memberEligibility.getPrincipalId().toString());
						} else {
							String reason = ""; // unknown reason
							if (!memberEligibility.getIsRegistered()) {
								reason = "Not registered for the challenge.";
							} else if (memberEligibility.getIsQuotaFilled()) {
								reason = "Reached the submission quota.";
							} else if (memberEligibility.getHasConflictingSubmission()) {
								reason = "Has a conflicting submission.";
							}
							view.addInEligibleContributor(memberEligibility.getPrincipalId().toString(), reason);
						}
					}
				}
			};

			@Override
			public void onFailure(Throwable caught) {
				// modal 2
				view.setContributorsLoading(false);
				contributorSynAlert.handleException(caught);
			}
		};
		challengeClient.getTeamSubmissionEligibility(evaluation.getId(), selectedTeam.getId(), callback);
	}

	public void lookupEtagAndCreateSubmission(final String id, final Long ver) {
		// look up entity for the current etag
		jsClient.getEntity(id, new AsyncCallback<Entity>() {
			public void onSuccess(Entity result) {
				Entity entity;
				entity = result;
				Long v = null;
				if (ver != null)
					v = ver;
				else if (entity instanceof Versionable)
					v = ((Versionable) entity).getVersionNumber();
				else {
					// entity is not versionable, the service will not accept null, but will accept a version of 1
					v = 1L;
				}

				submitToEvaluation(id, v, entity.getEtag());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	public void submitToEvaluation(String entityId, Long versionNumber, final String etag) {
		// set up shared values across all submissions
		Submission newSubmission = getNewSubmission(entityId, versionNumber);
		submitToEvaluation(newSubmission, etag);
	}

	public Submission getNewSubmission(String entityId, Long versionNumber) {
		// set up shared values across all submissions
		Submission newSubmission = new Submission();
		newSubmission.setEntityId(entityId);
		newSubmission.setUserId(authenticationController.getCurrentUserPrincipalId());
		newSubmission.setVersionNumber(versionNumber);
		if (submissionEntity instanceof DockerRepository) {
			newSubmission.setDockerDigest(dockerDigest);
		}
		if (submissionName != null && submissionName.trim().length() > 0)
			newSubmission.setName(submissionName);
		if (!isIndividualSubmission && !selectedTeamEligibleMembers.isEmpty()) {
			Set<SubmissionContributor> contributors = new HashSet<SubmissionContributor>();
			for (Long memberId : selectedTeamEligibleMembers) {
				SubmissionContributor contributor = new SubmissionContributor();
				contributor.setPrincipalId(memberId.toString());
				contributors.add(contributor);
			}
			newSubmission.setContributors(contributors);
		}
		return newSubmission;
	}

	public void submitToEvaluation(final Submission newSubmission, final String etag) {
		// and create a new submission for each evaluation
		newSubmission.setEvaluationId(evaluation.getId());
		try {
			String memberStateHash = null;
			if (isIndividualSubmission) {
				challengeClient.createIndividualSubmission(newSubmission, etag, gwt.getHostPageBaseURL(), getSubmissionCallback());
			} else {
				// team submission
				newSubmission.setTeamId(selectedTeam.getId());
				memberStateHash = selectedTeamMemberStateHash;
				challengeClient.createTeamSubmission(newSubmission, etag, memberStateHash, gwt.getHostPageBaseURL(), getSubmissionCallback());
			}
		} catch (RestServiceException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	public AsyncCallback<Submission> getSubmissionCallback() {
		return new AsyncCallback<Submission>() {
			@Override
			public void onSuccess(Submission result) {
				// result is the updated submission
				String message = evaluation.getSubmissionReceiptMessage();
				if (message == null || message.length() == 0)
					message = DisplayConstants.SUBMISSION_RECEIVED_TEXT;
				view.hideModal2();
				view.showSubmissionAcceptedDialogs(message);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		};
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	/***************************
	 * Exposed for unit tests
	 * 
	 * @return
	 ****************************/

	public Challenge getChallenge() {
		return challenge;
	}

	public Team getSelectedTeam() {
		return selectedTeam;
	}

	public String getSelectedTeamMemberStateHash() {
		return selectedTeamMemberStateHash;
	}

	public List<Long> getSelectedTeamEligibleMembers() {
		return selectedTeamEligibleMembers;
	}

	public boolean getIsIndividualSubmission() {
		return isIndividualSubmission;
	}

	public void setDigest(DockerCommit commit) {
		dockerDigest = commit.getDigest();
	}

	@Override
	public void onDockerCommitNextButton() {
		DockerCommit commit = dockerCommitList.getCurrentCommit();
		if (commit == null) {
			dockerCommitSynAlert.showError(NO_COMMITS_SELECTED_MSG);
		} else {
			dockerDigest = commit.getDigest();
			view.hideDockerCommitModal();
			configureWithEvaluations();
		}
	}
}
