package org.sagebionetworks.web.server.servlet;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationRequest;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.web.client.DataAccessClient;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class DataAccessClientImpl extends SynapseClientBase implements DataAccessClient {

	@Override
	public ResearchProject getResearchProject(Long arId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getResearchProjectForUpdate(arId.toString());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	@Override
	public ResearchProject updateResearchProject(ResearchProject researchProject) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createOrUpdateResearchProject(researchProject);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

	@Override
	public RequestInterface getDataAccessRequest(Long id) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getRequestForUpdate(id.toString());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public RequestInterface updateDataAccessRequest(RequestInterface dataAccessRequest)
			throws RestServiceException {
		
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.createOrUpdateRequest(dataAccessRequest);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void submitDataAccessRequest(RequestInterface dataAccessRequest) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.submitRequest(dataAccessRequest.getId(), dataAccessRequest.getEtag());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public AccessRequirement getAccessRequirement(Long requirementId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getAccessRequirement(requirementId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public SubmissionPage getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, SubmissionState stateFilter, SubmissionOrder order, boolean isAsc) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.listSubmissions(accessRequirementId.toString(), nextPageToken, stateFilter, order, isAsc);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public List<AccessRequirement> getAccessRequirements(RestrictableObjectDescriptor subject, Long limit, Long offset) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getAccessRequirements(subject, limit, offset).getResults();
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public AccessRequirementStatus getAccessRequirementStatus(String accessRequirementId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getAccessRequirementStatus(accessRequirementId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	@Override
	public List<Boolean> getAccessRequirementStatus(List<String> accessRequirementIds)
			throws RestServiceException {
		//TODO: replace with a single call, when available (see PLFM-4492)
		List<Boolean> status = new ArrayList<>(accessRequirementIds.size());
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			for (String accessRequirementId : accessRequirementIds) {
				status.add(synapseClient.getAccessRequirementStatus(accessRequirementId).getIsApproved());
			}
			return status;
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void cancelDataAccessSubmission(String submissionId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.cancelSubmission(submissionId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	

	@Override
	public RestrictionInformationResponse getRestrictionInformation(String subjectId, RestrictableObjectType type) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			RestrictionInformationRequest request = new RestrictionInformationRequest();
			request.setObjectId(subjectId);
			request.setRestrictableObjectType(type);
			return synapseClient.getRestrictionInformation(request);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void createLockAccessRequirement(String entityId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.createLockAccessRequirement(entityId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	@Override
	public Submission updateDataAccessSubmissionState(String submissionId, SubmissionState newState, String reason) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.updateSubmissionState(submissionId, newState, reason);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
	@Override
	public OpenSubmissionPage getOpenSubmissions(String nextPageToken) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getOpenSubmissions(nextPageToken);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		} 
	}
}