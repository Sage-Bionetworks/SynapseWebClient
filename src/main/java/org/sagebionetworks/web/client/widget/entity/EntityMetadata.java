package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private EntityUpdatedHandler entityUpdatedHandler;
	private AuthenticationController authenticationController;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.view.setPresenter(this);
	}


	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		view.setEntityBundle(bundle, bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanCertifiedUserEdit(), versionNumber != null);
		boolean showDetailedMetadata = false;
		boolean showEntityName = false;
		if (bundle.getEntity() instanceof FileEntity) {
			//it has data if there is a file handle associated with it
			showDetailedMetadata = ((FileEntity)bundle.getEntity()).getDataFileHandleId() != null;
			showEntityName = !showDetailedMetadata;
		}
		else {
			//TODO: delete this after migration to FileHandle system.  This corresponds to the old logic
			boolean isLocationable = bundle.getEntity() instanceof Locationable;
			boolean isStudy = bundle.getEntity() instanceof Study; //if study, always show metadata and entity name
			showDetailedMetadata = !isLocationable || isStudy || LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, !isAnonymous());
			showEntityName = !isLocationable || isStudy || !LocationableTitleBar.isDataPossiblyWithinLocationable(bundle, !isAnonymous());
		}
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

}
