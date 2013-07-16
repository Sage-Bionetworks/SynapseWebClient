package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
@Deprecated
public class LocationableTitleBar implements LocationableTitleBarView.Presenter, SynapseWidgetPresenter {
	
	private LocationableTitleBarView view;
	private AuthenticationController authenticationController;
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private SynapseClientAsync synapseClient;
	private EntityEditor entityEditor;
	private EntityUpdatedHandler entityUpdatedHandler;
	
	@Inject
	public LocationableTitleBar(LocationableTitleBarView view, AuthenticationController authenticationController, EntityTypeProvider entityTypeProvider, SynapseClientAsync synapseClient, EntityEditor entityEditor) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.entityEditor = entityEditor;
		view.setPresenter(this);
	}	
	
	public Widget asWidget(EntityBundle bundle, boolean isAdministrator, boolean canEdit) {		
		//if this isn't locationable, then return an empty panel
		view.setPresenter(this);
		this.entityBundle = bundle; 		
		
		// Get EntityType
		EntityType entityType = entityTypeProvider.getEntityTypeForEntity(bundle.getEntity());
		
		view.createTitlebar(bundle, entityType, authenticationController, isAdministrator, canEdit);
		Widget widget =  view.asWidget();
		widget.setVisible(bundle.getEntity() instanceof Locationable);
		return widget;
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
		this.entityBundle = null;		
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return null;
	}
    
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		entityEditor.setEntityUpdatedHandler(handler);
	}

	@Override
	public boolean isUserLoggedIn() {
		return authenticationController.isLoggedIn();
	}

	@Override
	public void addNewChild(EntityType type, String parentId) {
		entityEditor.addNewEntity(type, parentId);
		
	}

	public static boolean isDataPossiblyWithinLocationable(EntityBundle bundle, boolean isLoggedIn) {
		if (!(bundle.getEntity() instanceof Locationable))
			throw new IllegalArgumentException("Bundle must reference a Locationable entity");
		
		boolean hasData = false;
		//if user isn't logged in, then there might be something here
		if (!isLoggedIn)
			hasData = true;
		//if it has unmet access requirements, then there might be something here
		else if (bundle.getUnmetAccessRequirements() != null && bundle.getUnmetAccessRequirements().size() > 0)
			hasData = true;
		//else, if it has a locations list whose size is > 0
		else if (((Locationable)bundle.getEntity()).getLocations() != null && ((Locationable)bundle.getEntity()).getLocations().size() > 0)
			hasData = true;
		return hasData;
	}
	
	/*
	 * Private Methods
	 */
}
