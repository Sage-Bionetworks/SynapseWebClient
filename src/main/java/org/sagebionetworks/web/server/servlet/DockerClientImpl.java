package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.DockerClient;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class DockerClientImpl extends SynapseClientBase implements DockerClient{

	/**
	 * Helper to convert from the non-gwt compatible PaginatedResults to the compatible type.
	 * @param in
	 * @return
	 */
	public <T extends JSONEntity> PaginatedResults<T> convertPaginated(org.sagebionetworks.reflection.model.PaginatedResults<T> in){
		return  new PaginatedResults<T>(in.getResults(), in.getTotalNumberOfResults());
	}

	@Override
	public PaginatedResults<DockerCommit> getDockerCommits(String entityId, Long limit, Long offset, DockerCommitSortBy sortBy, Boolean ascending) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return convertPaginated(synapseClient.listDockerCommits(entityId, limit, offset, sortBy, ascending));
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}

}
