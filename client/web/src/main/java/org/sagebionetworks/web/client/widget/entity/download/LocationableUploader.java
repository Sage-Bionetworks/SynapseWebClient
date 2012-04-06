package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableUploader implements LocationableUploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable {
	
	private LocationableUploaderView view;
	private PlaceChanger placeChanger;
	private NodeServiceAsync nodeService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private HandlerManager handlerManager = new HandlerManager(this);
	private Entity entity;
	private EntityTypeProvider entityTypeProvider;
	
	@Inject
	public LocationableUploader(LocationableUploaderView view, NodeServiceAsync nodeService, NodeModelCreator nodeModelCreator, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider) {
		this.view = view;
		this.nodeService = nodeService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		
		view.setPresenter(this);		
	}		
		
	public Widget asWidget(Entity entity, boolean showCancel) {
		this.entity = entity;
		this.view.createUploadForm(showCancel);
		return this.view.asWidget();
	}
		
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entity = null;		
	}

	@Override
	public Widget asWidget() {
		return null;
	}

    public void setPlaceChanger(PlaceChanger placeChanger) {
    	this.placeChanger = placeChanger;
    }
    
	@Override
	public PlaceChanger getPlaceChanger() {
		return placeChanger;
	}

	@Override
	public String getUploadActionUrl() {
		return GWT.getModuleBaseURL() + "upload" + "?" + DisplayUtils.ENTITY_PARAM_KEY + "=" + entity.getId();
	}

	@Override
	public void setExternalLocation(String path) {
		// TODO : store 
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCancelHandler(CancelHandler handler) {
		handlerManager.addHandler(CancelEvent.getType(), handler);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addPersistSuccessHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public void closeButtonSelected() {
		handlerManager.fireEvent(new CancelEvent());
	}

	@Override
	public void handleSubmitResult(String resultHtml) {
		if(resultHtml == null) resultHtml = "";
		// response from server 
		if(resultHtml.contains(DisplayUtils.UPLOAD_SUCCESS)) {
			view.showErrorMessage(DisplayConstants.ERROR_UPLOAD);
			handlerManager.fireEvent(new CancelEvent());
		} else {
			view.showInfo(DisplayConstants.TEXT_UPLOAD_FILE, DisplayConstants.TEXT_UPLOAD_SUCCESS);
			handlerManager.fireEvent(new EntityUpdatedEvent());
		}
		
	}
	
	
	/*
	 * Private Methods
	 */
}
