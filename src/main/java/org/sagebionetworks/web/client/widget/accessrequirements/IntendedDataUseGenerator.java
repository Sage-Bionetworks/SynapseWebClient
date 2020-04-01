package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class IntendedDataUseGenerator {
	Map<String, Submission> researchProjectId2Submission;
	SynapseJavascriptClient jsClient;
	PopupUtilsView popupUtils;
	DateTimeUtils dateTimeUtils;
	String accessRequirementId;
	CallbackP<String> mdCallback;
	
	@Inject
	public IntendedDataUseGenerator(SynapseJavascriptClient jsClient, PopupUtilsView popupUtils, DateTimeUtils dateTimeUtils) {
		this.jsClient = jsClient;
		this.popupUtils = popupUtils;
		this.dateTimeUtils = dateTimeUtils;
	}
	
	public void gatherAllSubmissions(String accessRequirementId, CallbackP<String> mdCallback) {
		this.accessRequirementId = accessRequirementId;
		this.mdCallback = mdCallback;
		researchProjectId2Submission = new HashMap<>();
		gatherSubmissionsPage(null);
	}
	
	private void gatherSubmissionsPage(String nextPageToken) {
		// NOTE: Only ACT can call this service!  Need a new service to gather the ResearchProject snapshots and the submission modifiedOn.
		// See PLFM-6172
		jsClient.getDataAccessSubmissions(accessRequirementId, nextPageToken, SubmissionState.APPROVED, SubmissionOrder.CREATED_ON, true, new AsyncCallback<SubmissionPage>() {
			@Override
			public void onSuccess(SubmissionPage page) {
				List<Submission> newSubmissions = page.getResults();
				for (Submission submission : newSubmissions) {
					researchProjectId2Submission.put(submission.getResearchProjectSnapshot().getId(), submission);
				}
				if (!newSubmissions.isEmpty() && page.getNextPageToken() != null) {
					gatherSubmissionsPage(page.getNextPageToken());
				} else {
					showIDUs();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage("Unable to get all submissions: " + caught.getMessage());
			}
		});
	}

	public void showIDUs() {
		StringBuilder sb = new StringBuilder();
		for (Submission submission : researchProjectId2Submission.values()) {
			ResearchProject rp = submission.getResearchProjectSnapshot();
			String projectLead = rp.getProjectLead();
			String currentInstitution = rp.getInstitution();
			String currentIDU = rp.getIntendedDataUseStatement();
			sb.append("\n**Researcher:** ");
			sb.append(projectLead);
			sb.append("\n**Affiliation:** ");
			sb.append(currentInstitution);
			String lastModifiedOn = dateTimeUtils.getDateString(submission.getModifiedOn());
			sb.append("\n**Intended Data Use Statement (accepted on " + lastModifiedOn + "):**\n");
			sb.append(currentIDU);
			sb.append("\n\n-------------\n\n");
		}
		mdCallback.invoke(sb.toString());
	}
}
