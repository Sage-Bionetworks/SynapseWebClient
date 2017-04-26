package org.sagebionetworks.web.client;

import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictionInformation;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmission;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dataaccessclient")
public interface DataAccessClient extends RemoteService {
	ResearchProject getResearchProject(Long id) throws RestServiceException;
	ResearchProject updateResearchProject(ResearchProject researchProject)  throws RestServiceException;
	DataAccessRequestInterface getDataAccessRequest(Long id) throws RestServiceException;
	DataAccessRequestInterface updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest) throws RestServiceException;
	AccessRequirement getAccessRequirement(Long requirementId) throws RestServiceException;
	DataAccessSubmissionPage getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, DataAccessSubmissionState stateFilter,
			DataAccessSubmissionOrder order, boolean isAsc) throws RestServiceException;
	List<AccessRequirement> getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset) throws RestServiceException;
	AccessRequirementStatus getAccessRequirementStatus(String accessRequirementId) throws RestServiceException;
	void cancelDataAccessSubmission(String submissionId) throws RestServiceException;
	RestrictionInformation getRestrictionInformation(String entityId) throws RestServiceException;
	ACTAccessRequirement createLockAccessRequirement(String entityId) throws RestServiceException;
	DataAccessSubmission updateDataAccessSubmissionState(String submissionId, DataAccessSubmissionState newState,
			String reason) throws RestServiceException;
	OpenSubmissionPage getOpenSubmissions(String nextPageToken) throws RestServiceException;
	void submitDataAccessRequest(DataAccessRequestInterface dataAccessRequest) throws RestServiceException;
}
