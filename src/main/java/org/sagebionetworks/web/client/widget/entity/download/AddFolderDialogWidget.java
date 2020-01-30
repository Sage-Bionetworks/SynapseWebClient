package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddFolderDialogWidget implements AddFolderDialogWidgetView.Presenter, SynapseWidgetPresenter {
	public static final String FOLDER_CREATION_ERROR = "Unable to create a new folder";
	private AddFolderDialogWidgetView view;
	private SharingAndDataUseConditionWidget sharingAndDataUseWidget;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;
	private PopupUtilsView popupUtils;
	private GlobalApplicationState globalAppState;
	private String parentEntityId;

	@Inject
	public AddFolderDialogWidget(AddFolderDialogWidgetView view, SharingAndDataUseConditionWidget sharingAndDataUseWidget, SynapseJavascriptClient jsClient, GlobalApplicationState globalAppState, PopupUtilsView popupUtils, SynapseAlert synAlert) {
		this.view = view;
		this.sharingAndDataUseWidget = sharingAndDataUseWidget;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.globalAppState = globalAppState;
		this.popupUtils = popupUtils;
		view.setSynAlert(synAlert);
		view.setSharingAndDataUseWidget(sharingAndDataUseWidget.asWidget());
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void show(String parentEntityId) {
		this.parentEntityId = parentEntityId;
		sharingAndDataUseWidget.configure(parentEntityId);
		view.show();
		synAlert.clear();
	}

	@Override
	public void createFolder(String newFolderName) {
		Folder newFolder = new Folder();
		newFolder.setName(newFolderName);
		newFolder.setParentId(parentEntityId);
		synAlert.clear();
		view.setSaveEnabled(false);
		jsClient.createEntity(newFolder, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				popupUtils.showInfo("Folder '" + newFolderName + "' Added");
				globalAppState.refreshPage();
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setSaveEnabled(true);
				synAlert.handleException(caught);
			}
		});
	}
}
