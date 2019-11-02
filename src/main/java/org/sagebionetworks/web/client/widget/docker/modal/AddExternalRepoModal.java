package org.sagebionetworks.web.client.widget.docker.modal;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class AddExternalRepoModal implements AddExternalRepoModalView.Presenter {
	public static final String ADD_EXTERNAL_REPO_MODAL_TITLE = "Add External Repository";
	public static final String SUCCESS_TITLE = "External repository added";
	public static final String SUCCESS_MESSAGE = "An external repository has been added";

	private AddExternalRepoModalView view;
	private SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;

	private String parentId;
	private Callback repoAddedCallback;

	@Inject
	public AddExternalRepoModal(AddExternalRepoModalView view, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setModalTitle(ADD_EXTERNAL_REPO_MODAL_TITLE);
	}

	public void configuration(String projectId, Callback repoAddedCallback) {
		this.parentId = projectId;
		this.repoAddedCallback = repoAddedCallback;
	}

	public IsWidget asWidget() {
		return view.asWidget();
	}

	public void show() {
		synAlert.clear();
		view.clear();
		view.showDialog();
	}

	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String repoName = view.getRepoName();
		DockerRepository dockerRepo = new DockerRepository();
		dockerRepo.setParentId(parentId);
		dockerRepo.setRepositoryName(repoName);
		jsClient.createEntity(dockerRepo, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity dockerRepo) {
				view.hideDialog();
				view.showSuccess(SUCCESS_TITLE, SUCCESS_MESSAGE);
				repoAddedCallback.invoke();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}
		});
	}
}
