package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private EntityUpdatedHandler entityUpdatedHandler;
	private AuthenticationController authenticationController;
	private AnnotationsRendererWidget annotationsWidget;
	private FavoriteWidget favoriteWidget;
	private DoiWidget doiWidget;
	private FileHistoryWidget fileHistoryWidget;
	private RestrictionWidget restrictionWidget;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			AuthenticationController authenticationController,
			FavoriteWidget favoriteWidget,
			DoiWidget doiWidget,
			AnnotationsRendererWidget annotationsWidget,
			RestrictionWidget restrictionWidget,
			FileHistoryWidget fileHistoryWidget) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.favoriteWidget = favoriteWidget;
		this.doiWidget = doiWidget;
		this.annotationsWidget = annotationsWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.restrictionWidget = restrictionWidget;
		this.view.setPresenter(this);
		this.view.setFavoriteWidget(favoriteWidget);
		this.view.setDoiWidget(doiWidget);
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setRestrictionWidget(restrictionWidget);
		this.view.setFileHistoryWidget(fileHistoryWidget);
	}

	public void setAttachCallback(Callback attachCallback) {
		view.setAttachCallback(attachCallback);
	}

	public Widget asWidget() {
		return view.asWidget();
	}
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		clear();
		Entity en = bundle.getEntity();
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		boolean showDetailedMetadata = true;
		boolean showEntityName = true;
		restrictionWidget.configure(bundle, true, false, true, new Callback() {
			@Override
			public void invoke() {
				fireEntityUpdatedEvent();
			}
		});
		view.setEntityId(en.getId());
		view.setEntityName(en.getName());
		if (bundle.getEntity() instanceof FileEntity) {
			showEntityName = false;
			fileHistoryWidget.setEntityBundle(bundle, versionNumber);
			fileHistoryWidget.setEntityUpdatedHandler(entityUpdatedHandler);
			view.setFileHistoryWidget(fileHistoryWidget);
			GWT.debugger();
			view.setFileHistoryVisible(versionNumber != null);
		} else {
			view.setFileHistoryVisible(false);
		}
		favoriteWidget.configure(bundle.getEntity().getId());
		doiWidget.configure(bundle.getEntity().getId(), bundle.getPermissions().getCanCertifiedUserEdit(), versionNumber);
		annotationsWidget.configure(bundle, canEdit);
		view.setDetailedMetadataVisible(showDetailedMetadata);
		view.setEntityNameVisible(showEntityName);		
		view.configureRestrictionWidget();
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
		this.annotationsWidget.setEntityUpdatedHandler(entityUpdatedHandler);
	}

	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}
	
	public void setFileHistoryVisible(boolean visible) {
		view.setFileHistoryVisible(visible);
	}
	
	public void clear() {
		doiWidget.clear();
		view.clear();
	}
	
}
