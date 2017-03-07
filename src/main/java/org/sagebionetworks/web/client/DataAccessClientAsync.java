package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataAccessClientAsync {
	void getResearchProject(Long id, AsyncCallback<ResearchProject> asyncCallback);
	void updateResearchProject(ResearchProject researchProject, AsyncCallback<ResearchProject> asyncCallback);
	void getDataAccessRequest(Long id, AsyncCallback<DataAccessRequestInterface> asyncCallback);
	void updateDataAccessRequest(DataAccessRequestInterface dataAccessRequest, boolean isSubmit,
			AsyncCallback<Void> asyncCallback);
}