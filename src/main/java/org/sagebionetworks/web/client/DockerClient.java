package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dockerclient")
public interface DockerClient extends RemoteService {

	PaginatedResults<DockerCommit> getDockerCommits(String entityId, Long limit,
			Long offset, DockerCommitSortBy sortBy, Boolean ascending) throws RestServiceException;
}
