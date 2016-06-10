package org.sagebionetworks.web.client.widget.docker.modal;

import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class AddExternalRepoModal implements AddExternalRepoModalView.Presenter {
	private static final String ADD_EXTERNAL_REPO_MODAL_TITLE = "Add External Repository";
	private static final String SUCCESS_TITLE = "External repository added";
	private static final String SUCCESS_MESSAGE = "An external repository has been added";

	private AddExternalRepoModalView view;
	private SynapseAlert synAlert;

	@Inject
	public AddExternalRepoModal(
			AddExternalRepoModalView view,
			SynapseAlert synAlert
			){
		this.view = view;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
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
	}

}
