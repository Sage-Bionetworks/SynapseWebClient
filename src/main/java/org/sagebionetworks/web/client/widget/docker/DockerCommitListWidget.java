package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.web.client.DockerClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitListWidget implements IsWidget, DockerCommitListWidgetView.Presenter {

	public static final Long LIMIT = 10L;
	public static final DockerCommitSortBy DEFAULT_ORDER = DockerCommitSortBy.CREATED_ON;
	public static final Boolean DEFAULT_ASCENDING = false;
	private DockerCommitListWidgetView view;
	private DockerClientAsync dockerClient;
	private SynapseAlert synAlert;
	private LoadMoreWidgetContainer commitsContainer;
	private PortalGinInjector ginInjector;
	private Long offset;
	private DockerCommitSortBy order;
	private Boolean ascending;
	private CallbackP<DockerCommit> commitClickedCallback;
	private String entityId;
	private Callback emptyCommitCallback;
	private boolean withRadio = false;

	@Inject
	public DockerCommitListWidget(
			DockerCommitListWidgetView view,
			DockerClientAsync dockerClient,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer commitsContainer,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.dockerClient = dockerClient;
		this.synAlert = synAlert;
		this.commitsContainer = commitsContainer;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
		commitsContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		view.setCommitsContainer(commitsContainer);
		view.setSynAlert(synAlert.asWidget());
	}


	public void loadMore() {
		synAlert.clear();
		dockerClient.getDockerCommits(entityId, LIMIT, offset, order, ascending, new AsyncCallback<PaginatedResults<DockerCommit>>(){

					@Override
					public void onFailure(Throwable caught) {
						commitsContainer.setIsMore(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(PaginatedResults<DockerCommit> result) {
						long numberOfCommits = result.getTotalNumberOfResults();
						if (numberOfCommits == 0) {
							emptyCommitCallback.invoke();
						} else {
							for(final DockerCommit commit: result.getResults()) {
								DockerCommitRowWidget dockerCommitRow = ginInjector.createNewDockerCommitRowWidget();
								dockerCommitRow.configure(commit);
								dockerCommitRow.setOnClickCallback(new CallbackP<DockerCommit>(){
	
									@Override
									public void invoke(DockerCommit param) {
										commitClickedCallback.invoke(commit);
									}
								});
								if (withRadio) {
									RadioWidget radioWidget = ginInjector.createNewRadioWidget();
									radioWidget.add(dockerCommitRow.asWidget());
									commitsContainer.add(radioWidget.asWidget());
								} else {
									
									commitsContainer.add(dockerCommitRow.asWidget());
								}
							}
						}
						offset += LIMIT;
						commitsContainer.setIsMore(offset < numberOfCommits);
					}
		});
	}

	public void configure(String entityId, boolean withRadio) {
		this.entityId = entityId;
		this.withRadio = withRadio;
		commitsContainer.clear();
		offset = 0L;
		ascending = false;
		order = DEFAULT_ORDER;
		loadMore();
	}

	public void setDockerCommitClickCallback(CallbackP<DockerCommit> callback) {
		this.commitClickedCallback = callback;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEmptyListCallback(Callback callback) {
		this.emptyCommitCallback = callback;
	}

}
