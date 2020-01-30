package org.sagebionetworks.web.client.widget.docker;

import java.util.ArrayList;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitListWidget implements IsWidget, DockerCommitListWidgetView.Presenter {

	public static final Long LIMIT = 10L;
	public static final DockerCommitSortBy DEFAULT_ORDER = DockerCommitSortBy.CREATED_ON;
	public static final Boolean DEFAULT_ASCENDING = false;
	private DockerCommitListWidgetView view;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;
	private LoadMoreWidgetContainer commitsContainer;
	private PortalGinInjector ginInjector;
	private GWTWrapper gwtWrapper;
	private Long offset = 0L;
	private DockerCommitSortBy order;
	private Boolean ascending;
	private String entityId;
	private Callback emptyCommitCallback;
	private boolean withRadio = false;
	private DockerCommit currentCommit;
	private String radioGroupName;

	@Inject
	public DockerCommitListWidget(DockerCommitListWidgetView view, SynapseJavascriptClient jsClient, SynapseAlert synAlert, LoadMoreWidgetContainer commitsContainer, PortalGinInjector ginInjector, GWTWrapper gwtWrapper) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.commitsContainer = commitsContainer;
		this.ginInjector = ginInjector;
		this.gwtWrapper = gwtWrapper;
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
		jsClient.getDockerTaggedCommits(entityId, LIMIT, offset, order, ascending, new AsyncCallback<ArrayList<DockerCommit>>() {

			@Override
			public void onFailure(Throwable caught) {
				commitsContainer.setIsMore(false);
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(ArrayList<DockerCommit> result) {
				if (offset == 0 && result.isEmpty()) {
					if (emptyCommitCallback != null) {
						emptyCommitCallback.invoke();
					}
				} else {
					for (final DockerCommit commit : result) {
						DockerCommitRowWidget dockerCommitRow = ginInjector.createNewDockerCommitRowWidget();
						dockerCommitRow.configure(commit);
						dockerCommitRow.setOnClickCallback(new CallbackP<DockerCommit>() {

							@Override
							public void invoke(DockerCommit param) {
								currentCommit = param;
							}
						});
						if (withRadio) {
							RadioWidget radioWidget = ginInjector.createNewRadioWidget();
							radioWidget.add(dockerCommitRow.asWidget());
							radioWidget.setGroupName(radioGroupName);
							radioWidget.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									currentCommit = commit;
								}
							});
							commitsContainer.add(radioWidget.asWidget());
						} else {

							commitsContainer.add(dockerCommitRow.asWidget());
						}
					}
				}
				offset += LIMIT;
				commitsContainer.setIsMore(!(result.size() < LIMIT));
			}
		});
	}

	public void configure(String entityId, boolean withRadio) {
		this.entityId = entityId;
		this.withRadio = withRadio;
		commitsContainer.clear();
		currentCommit = null;
		offset = 0L;
		ascending = false;
		order = DEFAULT_ORDER;
		if (withRadio) {
			radioGroupName = gwtWrapper.getUniqueElementId();
		}
		loadMore();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEmptyListCallback(Callback callback) {
		this.emptyCommitCallback = callback;
	}

	@Override
	public DockerCommit getCurrentCommit() {
		return currentCommit;
	}
}
