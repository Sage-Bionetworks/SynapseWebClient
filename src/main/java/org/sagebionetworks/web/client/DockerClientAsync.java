package org.sagebionetworks.web.client;

import org.sagebionetworks.client.DockerCommitSortBy;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("dockerclient")
public interface DockerClientAsync {

	void getCommits(String entityId, Long limit, Long offset,
			DockerCommitSortBy sortBy, Boolean ascending,
			AsyncCallback<PaginatedResults<DockerCommit>> callback);
}
