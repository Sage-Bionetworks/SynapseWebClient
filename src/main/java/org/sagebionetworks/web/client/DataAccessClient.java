package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dataaccessclient")
public interface DataAccessClient extends RemoteService {
	ResearchProject getResearchProject(Long id) throws RestServiceException;
	ResearchProject updateResearchProject(ResearchProject researchProject)  throws RestServiceException;
	DataAccessRequestInterface getDataAccessRequest(Long id) throws RestServiceException;
	void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest, boolean isSubmit) throws RestServiceException;
}
