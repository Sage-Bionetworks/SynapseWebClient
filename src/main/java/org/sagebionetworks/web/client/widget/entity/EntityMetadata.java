package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private EntityUpdatedHandler entityUpdatedHandler;
	private AuthenticationController authenticationController;
	private FileHistoryWidget fileHistoryWidget;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			AuthenticationController authenticationController,
			FileHistoryWidget fileHistoryWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.fileHistoryWidget = fileHistoryWidget;
		this.view.setFileHistoryWidget(fileHistoryWidget);
		this.view.setPresenter(this);
	}


	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		view.setEntityBundle(bundle, bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanCertifiedUserEdit(), versionNumber != null);
		boolean showDetailedMetadata = true;
		boolean showEntityName = true;
		if (bundle.getEntity() instanceof FileEntity) {
			showEntityName = false;
		}
		fileHistoryWidget.setEntityBundle(bundle, versionNumber);
		this.view.setFileHistoryWidget(fileHistoryWidget);
		// need some update handling
//		fileHistoryWidget.setEntityUpdatedHandler(handler); 
		view.setDetailedMetadataVisible(showDetailedMetadata);
		view.setEntityNameVisible(showEntityName);
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		return (sessionData==null ? null : sessionData.getProfile());				
	}
	
	public boolean isAnonymous() {
		return getUserProfile()==null;
	}

	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		view.setEntityUpdatedHandler(handler);
	}

	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}
	
	public void setFileHistoryVisible(boolean visible) {
		view.setFileHistoryVisible(visible);
	}
	
}
