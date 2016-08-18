package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitRowWidget implements IsWidget, DockerCommitRowWidgetView.Presenter {

	private DockerCommitRowWidgetView view;
	private DigestWidget digest;
	private DockerCommit commit;
	private CallbackP<DockerCommit> callback;

	@Inject
	public DockerCommitRowWidget(
			DockerCommitRowWidgetView view,
			DigestWidget digest){
		this.view = view;
		this.digest = digest;
		view.setPresenter(this);
		view.setDigest(digest.asWidget());
	}

	public void configure(DockerCommit commit) {
		this.commit = commit;
		digest.configure(commit.getDigest());
		view.setTag(commit.getTag());
		view.setCreatedOn(commit.getCreatedOn());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onClick() {
		if (callback != null) {
			callback.invoke(commit);
		}
	}

	public void setOnClickCallback(CallbackP<DockerCommit> callback) {
		this.callback = callback;
	}
}
