package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalStatusRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalStatusResults;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataAccessClientAsync {
	void getResearchProject(Long id, AsyncCallback<ResearchProject> asyncCallback);
	void updateResearchProject(ResearchProject researchProject, AsyncCallback<ResearchProject> asyncCallback);
	void getDataAccessRequest(Long id, AsyncCallback<DataAccessRequestInterface> asyncCallback);
	void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest, boolean isSubmit,
			AsyncCallback<Void> asyncCallback);

	void getAccessRequirement(Long requirementId, AsyncCallback<AccessRequirement> callback);
	void getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, DataAccessSubmissionState stateFilter,
			DataAccessSubmissionOrder order, boolean isAsc, AsyncCallback<DataAccessSubmissionPage> callback);

	void getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset,
			AsyncCallback<List<AccessRequirement>> callback);
	void getAccessApprovalStatus(AccessApprovalStatusRequest approvalStatusRequest,
			AsyncCallback<AccessApprovalStatusResults> asyncCallback);
}