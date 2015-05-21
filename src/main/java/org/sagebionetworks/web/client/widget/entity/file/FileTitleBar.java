package org.sagebionetworks.web.client.widget.entity.file;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBar implements FileTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private FileTitleBarView view;
	private AuthenticationController authenticationController;
	private EntityUpdatedHandler entityUpdatedHandler;
	private EntityBundle entityBundle;
	private SynapseClientAsync synapseClient;
	
	@Inject
	public FileTitleBar(FileTitleBarView view, AuthenticationController authenticationController, SynapseClientAsync synapseClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}	
	
	public void configure(EntityBundle bundle) {		
		view.setPresenter(this);
		this.entityBundle = bundle;

		// Get EntityType
		EntityType entityType = EntityType.getEntityTypeForClass(bundle.getEntity().getClass());
		view.createTitlebar(bundle, entityType, authenticationController);
	}
	
	/**
	 * For unit testing. call asWidget with the new Entity for the view to be in sync.
	 * @param bundle
	 */
	public void setEntityBundle(EntityBundle bundle) {
		this.entityBundle = bundle;
	}
	
	public void clearState() {
		view.clear();
		// remove handlers
		entityUpdatedHandler = null;		
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
    
	@Override
	public void fireEntityUpdatedEvent(EntityUpdatedEvent event) {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(event);
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	
	public static boolean isDataPossiblyWithin(FileEntity fileEntity) {
		String dataFileHandleId = fileEntity.getDataFileHandleId();
		return (dataFileHandleId != null && dataFileHandleId.length() > 0);
	}

	
	public void queryForSftpLoginInstructions(String url) {
		synapseClient.getHost(url, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String host) {
				//update the download login dialog message
				view.setLoginInstructions(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SafeHtmlUtils.htmlEscape(host));
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public void setS3Description(final CallbackP<String> callback) {
		final String entityId = entityBundle.getEntity().getId();
		synapseClient.getUploadDestinations(entityId, new AsyncCallback<List<UploadDestination>>() {
			public void onSuccess(List<UploadDestination> uploadDestinations) {
				if (uploadDestinations.get(0) instanceof ExternalS3UploadDestination) {
					ExternalS3UploadDestination externalUploadDestination = (ExternalS3UploadDestination) uploadDestinations.get(0);
					String description = " - s3://" + externalUploadDestination.getBucket() + "/";
					if (externalUploadDestination.getBaseKey() != null) {
						description += externalUploadDestination.getBaseKey() + "/";
					};
					description += entityBundle.getEntity().getName() + ")";
					callback.invoke(description);
				} else {
					callback.invoke(" - Synapse Storage)");
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.showErrorMessage("Failed to get the upload destination for entity Id " + entityId);
			}
		});
	}


	/*
	 * Private Methods
	 */
}
