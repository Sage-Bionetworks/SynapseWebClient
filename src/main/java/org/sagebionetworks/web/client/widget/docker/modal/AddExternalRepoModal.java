package org.sagebionetworks.web.client.widget.docker.modal;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	private SynapseClientAsync synapseClient;

	private String parentId;
	private Callback repoAddedCallback;

	@Inject
	public AddExternalRepoModal(
			AddExternalRepoModalView view,
			SynapseAlert synAlert,
			SynapseClientAsync synapseClient
			){
		this.view = view;
		this.synAlert = synAlert;
		this.synapseClient = synapseClient;
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
		view.clear();
		view.showDialog();
	}

	public void hide() {
		view.hideDialog();
	}

	@Override
	public void onSave() {
		synAlert.clear();
		String registryHost = view.getRegistryHost();
		String port = view.getPort();
		String repoPath = view.getRepoPath();
		// TODO: validate input
		DockerRepository dockerRepo = new DockerRepository();
		dockerRepo.setEntityType(DockerRepository.class.getName());
		dockerRepo.setParentId(parentId);
		dockerRepo.setIsManaged(false);
		// TODO: replace this method with Bruce's
		String name = buildRepoName(registryHost, port, repoPath);
		dockerRepo.setName(name);
		synapseClient.createEntity(dockerRepo, new AsyncCallback<Entity>() {
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

	private String buildRepoName(String registryHost, String port, String repoPath) {
		String name = "";
		if (registryHost != null && !registryHost.equals("")) {
			name += registryHost;
			if (port != null && !registryHost.equals("")) {
				name += ":"+port;
			}
		}
		if (name.length() > 0) {
			name += "/";
		}
		name += repoPath;
		return name;
	}

}
