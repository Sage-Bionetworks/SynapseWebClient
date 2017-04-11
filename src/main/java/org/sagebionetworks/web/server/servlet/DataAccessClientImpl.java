package org.sagebionetworks.web.server.servlet;

import java.util.List;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
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
	public DataAccessRequestInterface getDataAccessRequest(Long id) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getDataAccessRequestForUpdate(id.toString());
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest, boolean isSubmit)
			throws RestServiceException {
		
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			dataAccessRequest = synapseClient.createOrUpdateDataAccessRequest(dataAccessRequest);
			
			// submit data access request
			if (isSubmit){
				synapseClient.submitDataAccessRequest(dataAccessRequest.getId(), dataAccessRequest.getEtag());
			}
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
	public DataAccessSubmissionPage getDataAccessSubmissions(Long accessRequirementId, String nextPageToken, DataAccessSubmissionState stateFilter, DataAccessSubmissionOrder order, boolean isAsc) throws RestServiceException {
		// TODO: use rpc once available
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.getDataAccessSubmissionStatus(accessRequirementId, nextPageToken, stateFilter, order, isAsc);
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
		return new DataAccessSubmissionPage();
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
	
	//TODO: return type AccessRequirementStatus (could be ACTAccessRequirementStatus or TermsOfUseAccessRequirementStatus)
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
	public void cancelDataAccessSubmission(String submissionId) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			synapseClient.cancelDataAccessSubmission(submissionId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
}
