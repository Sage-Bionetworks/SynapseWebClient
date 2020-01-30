package org.sagebionetworks.web.client;

import java.util.List;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoRequest;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoResponse;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementConversionRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupResponse;
import org.sagebionetworks.repo.model.dataaccess.CreateSubmissionRequest;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataAccessClientAsync {
	void getResearchProject(Long id, AsyncCallback<ResearchProject> asyncCallback);

	void updateResearchProject(ResearchProject researchProject, AsyncCallback<ResearchProject> asyncCallback);

	void getDataAccessRequest(Long id, AsyncCallback<RequestInterface> asyncCallback);

	void updateDataAccessRequest(RequestInterface dataAccessRequest, AsyncCallback<RequestInterface> asyncCallback);

	void submitDataAccessRequest(CreateSubmissionRequest dataAccessRequest, Long arId, AsyncCallback<Void> asyncCallback);

	void getAccessRequirement(Long requirementId, AsyncCallback<AccessRequirement> callback);

	void getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, SubmissionState stateFilter, SubmissionOrder order, boolean isAsc, AsyncCallback<SubmissionPage> callback);

	void getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset, AsyncCallback<List<AccessRequirement>> callback);

	void getAccessRequirementStatus(String accessRequirementId, AsyncCallback<AccessRequirementStatus> callback);

	void cancelDataAccessSubmission(String submissionId, AsyncCallback<Void> asyncCallback);

	void createLockAccessRequirement(String entityId, AsyncCallback<Void> callback);

	void updateDataAccessSubmissionState(String submissionId, SubmissionState newState, String reason, AsyncCallback<Submission> callback);

	void getOpenSubmissions(String nextPageToken, AsyncCallback<OpenSubmissionPage> callback);

	void getAccessRequirementStatus(BatchAccessApprovalInfoRequest request, AsyncCallback<BatchAccessApprovalInfoResponse> callback);

	void listAccessorGroup(AccessorGroupRequest request, AsyncCallback<AccessorGroupResponse> asyncCallback);

	void revokeGroup(String accessRequirementId, String submitterId, AsyncCallback<Void> callback);

	void convertAccessRequirement(AccessRequirementConversionRequest request, AsyncCallback<AccessRequirement> callback);
}
