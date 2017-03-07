package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequest;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClient;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class DataAccessClientImpl extends SynapseClientBase implements DataAccessClient {

	// TODO: use service calls, when available
	@Override
	public ResearchProject getResearchProject(Long arId) throws RestServiceException {
		ResearchProject researchProject = new ResearchProject();
		researchProject.setAccessRequirementId(arId.toString());
		return researchProject;
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.domethod();
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}
	@Override
	public ResearchProject updateResearchProject(ResearchProject researchProject) throws RestServiceException {
		return researchProject;
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.domethod();
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}

	@Override
	public DataAccessRequestInterface getDataAccessRequest(Long id) throws RestServiceException {
		///accessRequirement/{id}/dataAccessRequestForUpdate
		DataAccessRequest request = new DataAccessRequest();
		request.setAccessRequirementId(id.toString());
		return request;
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.domethod();
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
	}
	@Override
	public void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest, boolean isSubmit)
			throws RestServiceException {
		
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//		if (isSubmit){
//			synapseClient.domethodsubmit();
//		} else {
//			synapseClient.domethodupdate();
//		}
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}

		
	}
}
