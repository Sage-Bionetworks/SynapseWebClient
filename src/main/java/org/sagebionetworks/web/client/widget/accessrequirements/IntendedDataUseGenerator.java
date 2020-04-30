package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.dataaccess.SubmissionInfo;
import org.sagebionetworks.repo.model.dataaccess.SubmissionInfoPage;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class IntendedDataUseGenerator {
	SynapseJavascriptClient jsClient;
	PopupUtilsView popupUtils;
	DateTimeUtils dateTimeUtils;
	String accessRequirementId;
	CallbackP<String> mdCallback;
	List<SubmissionInfo> submissions;
	
	@Inject
	public IntendedDataUseGenerator(SynapseJavascriptClient jsClient, PopupUtilsView popupUtils, DateTimeUtils dateTimeUtils) {
		this.jsClient = jsClient;
		this.popupUtils = popupUtils;
		this.dateTimeUtils = dateTimeUtils;
	}
	
	public void gatherAllSubmissions(String accessRequirementId, CallbackP<String> mdCallback) {
		this.accessRequirementId = accessRequirementId;
		this.mdCallback = mdCallback;
		submissions = new ArrayList<SubmissionInfo>();
		gatherSubmissionsPage(null);
	}
	
	private void gatherSubmissionsPage(String nextPageToken) {
		jsClient.listApprovedSubmissionInfo(accessRequirementId, nextPageToken, new AsyncCallback<SubmissionInfoPage>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage("Unable to get all submissions: " + caught.getMessage());
			}
			public void onSuccess(SubmissionInfoPage page) {
				List<SubmissionInfo> newSubmissions = page.getResults();
				submissions.addAll(0, newSubmissions);
				if (!newSubmissions.isEmpty() && page.getNextPageToken() != null) {
					gatherSubmissionsPage(page.getNextPageToken());
				} else {
					showIDUs();
				}
			};
		});
	}

	public void showIDUs() {
		StringBuilder sb = new StringBuilder();
		for (SubmissionInfo submission : submissions) {
			String projectLead = submission.getProjectLead();
			String currentInstitution = submission.getInstitution();
			String currentIDU = submission.getIntendedDataUseStatement();
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
