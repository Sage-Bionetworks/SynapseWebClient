package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupResponse;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;

@RemoteServiceRelativePath("dataaccessclient")
public interface DataAccessClient extends XsrfProtectedService {
	ResearchProject getResearchProject(Long id) throws RestServiceException;
	ResearchProject updateResearchProject(ResearchProject researchProject)  throws RestServiceException;
	RequestInterface getDataAccessRequest(Long id) throws RestServiceException;
	RequestInterface updateDataAccessRequest(RequestInterface dataAccessRequest) throws RestServiceException;
	AccessRequirement getAccessRequirement(Long requirementId) throws RestServiceException;
	SubmissionPage getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, SubmissionState stateFilter,
			SubmissionOrder order, boolean isAsc) throws RestServiceException;
	List<AccessRequirement> getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset) throws RestServiceException;
	AccessRequirementStatus getAccessRequirementStatus(String accessRequirementId) throws RestServiceException;
	void cancelDataAccessSubmission(String submissionId) throws RestServiceException;
	RestrictionInformationResponse getRestrictionInformation(String subjectId, RestrictableObjectType type) throws RestServiceException;
	void createLockAccessRequirement(String entityId) throws RestServiceException;
	Submission updateDataAccessSubmissionState(String submissionId, SubmissionState newState,
			String reason) throws RestServiceException;
	OpenSubmissionPage getOpenSubmissions(String nextPageToken) throws RestServiceException;
	void submitDataAccessRequest(RequestInterface dataAccessRequest) throws RestServiceException;
	AccessorGroupResponse listAccessorGroup(AccessorGroupRequest request) throws RestServiceException;
	void revokeGroup(String accessRequirementId, String submitterId) throws RestServiceException;
}