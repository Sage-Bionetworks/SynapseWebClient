package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DockerClientAsync {
	void getDockerCommits(String entityId, Long limit, Long offset,
			DockerCommitSortBy sortBy, Boolean ascending,
			AsyncCallback<PaginatedResults<DockerCommit>> callback);
}