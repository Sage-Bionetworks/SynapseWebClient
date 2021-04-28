package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
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
	public static Map<String, String> userId2AliasMap = new HashMap<>();
	
	
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
				submissions.addAll(newSubmissions);
				if (!newSubmissions.isEmpty() && page.getNextPageToken() != null) {
					gatherSubmissionsPage(page.getNextPageToken());
				} else {
					populateUserMap();
				}
			};
		});
	}

	public void populateUserMap() {
		Set<String> userIdsToLookup = new HashSet<>();
		for (SubmissionInfo submission : submissions) {
			if (!userId2AliasMap.containsKey(submission.getSubmittedBy())) {
				userIdsToLookup.add(submission.getSubmittedBy());
			}
			if (submission.getAccessorChanges() != null) {
				for (AccessorChange change : submission.getAccessorChanges()) {
					if (!userId2AliasMap.containsKey(change.getUserId())) {
						userIdsToLookup.add(change.getUserId());
					}
				}
			}
		}
		List<String> userIdsToLookupList = new ArrayList<>(userIdsToLookup);
		jsClient.listUserProfiles(userIdsToLookupList, new AsyncCallback<List>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage("Unable to get user profiles associated to the submissions: " + caught.getMessage());
			}
			@Override
			public void onSuccess(List profiles) {
				for (int i = 0; i < profiles.size(); i++) {
					UserProfile profile = (UserProfile)profiles.get(i);
					userId2AliasMap.put(profile.getOwnerId(), profile.getUserName());
				}
				showIDUs();
			}
		});
	}

	public void showIDUs() {
		StringBuilder sb = new StringBuilder();
		for (SubmissionInfo submission : submissions) {
			String projectLead = submission.getProjectLead();
			String currentInstitution = submission.getInstitution();
			String currentIDU = submission.getIntendedDataUseStatement();
			sb.append("\n**Project Lead:** ");
			sb.append(projectLead);
			sb.append("\n**Institution:** ");
			sb.append(currentInstitution);
			sb.append("\n**Data Access Request Submitted By:** @");
			sb.append(userId2AliasMap.get(submission.getSubmittedBy()));
			String lastModifiedOn = dateTimeUtils.getDateString(submission.getModifiedOn());
			sb.append("\n**Intended Data Use Statement (accepted on " + lastModifiedOn + "):** <div>");
			sb.append(currentIDU);
			sb.append("</div>");
			if (submission.getAccessorChanges() != null) {
				sb.append("\n\n**Accessor Changes:** ");
				for (AccessorChange change : submission.getAccessorChanges()) {
					sb.append("\n@");
					sb.append(userId2AliasMap.get(change.getUserId()));
					sb.append(" ");
					sb.append(change.getType());
				}
			}
			sb.append("\n\n-------------\n\n");
		}
		mdCallback.invoke(sb.toString());
	}
}
