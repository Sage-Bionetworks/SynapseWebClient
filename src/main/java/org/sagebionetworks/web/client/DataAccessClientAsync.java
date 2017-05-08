package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictionInformation;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalRequest;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalResult;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmission;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataAccessClientAsync {
	void getResearchProject(Long id, AsyncCallback<ResearchProject> asyncCallback);
	void updateResearchProject(ResearchProject researchProject, AsyncCallback<ResearchProject> asyncCallback);
	void getDataAccessRequest(Long id, AsyncCallback<DataAccessRequestInterface> asyncCallback);
	void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest,
			AsyncCallback<DataAccessRequestInterface> asyncCallback);
	void submitDataAccessRequest(DataAccessRequestInterface dataAccessRequest, AsyncCallback<Void> asyncCallback); 

	void getAccessRequirement(Long requirementId, AsyncCallback<AccessRequirement> callback);
	void getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, DataAccessSubmissionState stateFilter,
			DataAccessSubmissionOrder order, boolean isAsc, AsyncCallback<DataAccessSubmissionPage> callback);

	void getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset,
			AsyncCallback<List<AccessRequirement>> callback);
	void getAccessRequirementStatus(String accessRequirementId, AsyncCallback<AccessRequirementStatus> callback);
	void cancelDataAccessSubmission(String submissionId, AsyncCallback<Void> asyncCallback);
	void getRestrictionInformation(String entityId, AsyncCallback<RestrictionInformation> callback);
	void createLockAccessRequirement(String entityId, AsyncCallback<Void> callback);
	void updateDataAccessSubmissionState(String submissionId, DataAccessSubmissionState newState, String reason,
			AsyncCallback<DataAccessSubmission> callback);
	void getOpenSubmissions(String nextPageToken, AsyncCallback<OpenSubmissionPage> callback);
	void getAccessApprovalInfo(BatchAccessApprovalRequest batchRequest,
			AsyncCallback<BatchAccessApprovalResult> callback);
}