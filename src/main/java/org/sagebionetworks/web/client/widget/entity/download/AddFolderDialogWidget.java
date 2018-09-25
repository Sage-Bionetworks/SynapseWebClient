package org.sagebionetworks.web.client.widget.entity.download;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.SharingAndDataUseConditionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddFolderDialogWidget implements AddFolderDialogWidgetView.Presenter, SynapseWidgetPresenter {
	public static final String FOLDER_CREATION_ERROR = "Unable to create a new folder";
	private AddFolderDialogWidgetView view;
	private SharingAndDataUseConditionWidget sharingAndDataUseWidget;
	private SynapseClientAsync synapseClient;
	private String currentFolderEntityId;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;
	private PopupUtilsView popupUtils;
	private GlobalApplicationState globalAppState;
	@Inject
	public AddFolderDialogWidget(AddFolderDialogWidgetView view, 
			SharingAndDataUseConditionWidget sharingAndDataUseWidget,
			SynapseClientAsync synapseClient,
			SynapseJavascriptClient jsClient,
			GlobalApplicationState globalAppState,
			PopupUtilsView popupUtils,
			SynapseAlert synAlert) {
		this.view = view;
		this.sharingAndDataUseWidget = sharingAndDataUseWidget;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
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
		Folder folder = new Folder();
		folder.setParentId(parentEntityId);
		synAlert.clear();
		jsClient.createEntity(folder)
			.addCallback(
					new FutureCallback<Entity>() {
						@Override
						public void onSuccess(Entity entity) {
							currentFolderEntityId = entity.getId();
							Callback refreshSharingAndDataUseWidget = new Callback() {
								@Override
								public void invoke() {
									// entity was updated by the sharing and data use widget.
									sharingAndDataUseWidget.setEntity(currentFolderEntityId);
								}
							};
							sharingAndDataUseWidget.configure(currentFolderEntityId, true,
									refreshSharingAndDataUseWidget);
							view.show();	
						}
	
						@Override
						public void onFailure(Throwable caught) {
							popupUtils.showErrorMessage(FOLDER_CREATION_ERROR, caught.getMessage());
						}
					},
					directExecutor()
		);
	}
	
	@Override
	public void deleteFolder(boolean skipTrashCan) {
		synAlert.clear();
		jsClient.deleteEntityById(currentFolderEntityId, skipTrashCan, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void na) {
				//folder is deleted when folder creation is canceled.
				view.hide();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void updateEntity(final Entity folder) {
		synAlert.clear();
		synapseClient.updateEntity(folder, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				view.hide();
				popupUtils.showInfo("Folder '" + folder.getName() + "' Added");
				globalAppState.refreshPage();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setSaveEnabled(true);
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void updateFolderName(final String newFolderName) {
		synAlert.clear();
		view.setSaveEnabled(false);
		jsClient.getEntity(currentFolderEntityId, OBJECT_TYPE.Folder, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity result) {
				Folder folder = (Folder) result;
				folder.setName(newFolderName);
				updateEntity(folder);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.setSaveEnabled(true);
				synAlert.handleException(caught);
			}
		});
	}
}
