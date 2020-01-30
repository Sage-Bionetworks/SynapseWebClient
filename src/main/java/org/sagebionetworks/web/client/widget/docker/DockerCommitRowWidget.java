package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.client.utils.CallbackP;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitRowWidget implements IsWidget, DockerCommitRowWidgetView.Presenter {

	private DockerCommitRowWidgetView view;
	private DockerCommit commit;
	private CallbackP<DockerCommit> callback;

	@Inject
	public DockerCommitRowWidget(DockerCommitRowWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(DockerCommit commit) {
		this.commit = commit;
		view.setTag(commit.getTag());
		view.setDigest(commit.getDigest());
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
