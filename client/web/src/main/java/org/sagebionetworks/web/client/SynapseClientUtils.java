package org.sagebionetworks.web.client;

import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.HasShortcuts;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SynapseClientUtils {

	public static void createShortcut(final Entity entityToBeReferenced, final String addToEntityId, final SynapseWidgetView view,
			final SynapseClientAsync synapseClient, final NodeServiceAsync nodeService,
			final NodeModelCreator nodeModelCreator, final JSONObjectAdapter jsonObjectAdapter) {
		// check inputs
		if(entityToBeReferenced == null || addToEntityId == null) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			return;
		}
		// TODO : check all inputs!
		
		synapseClient.getEntity(addToEntityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper wrapper) {
				Entity addToEntity = null;
				try {
					addToEntity = nodeModelCreator.createEntity(wrapper);					
				} catch (RestServiceException ex) {
					onFailure(ex);
				}
				// make sure we can add a reference
				if(addToEntity == null) {					
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					return;
				} 
				if(!(addToEntity instanceof HasShortcuts)) {
					view.showErrorMessage(DisplayConstants.ERROR_ENTITY_CANT_SHORTCUTS);
					return;
				}
				// Add reference to this and save
				Reference ref = new Reference();
				ref.setTargetId(entityToBeReferenced.getId()); 
				Set<Reference> shortcuts = ((HasShortcuts)addToEntity).getShortcuts();
				if(shortcuts == null) shortcuts = new HashSet<Reference>();
				shortcuts.add(ref);
				((HasShortcuts)addToEntity).setShortcuts(shortcuts);				
				
				// TODO : change to synapse client
				JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
				try {
					addToEntity.writeToJSONObject(adapter);
					nodeService.updateNode(DisplayUtils.getNodeTypeForEntity(addToEntity), addToEntity.getId(), adapter.toJSONString(), addToEntity.getEtag(), new AsyncCallback<String>() {
						@Override
						public void onSuccess(String result) {
							view.showInfo(DisplayConstants.LABEL_SUCCESS, DisplayConstants.TEXT_SHORTCUT_ADDED + ": " + addToEntityId);
						}
						@Override
						public void onFailure(Throwable caught) {
							view.showErrorMessage(DisplayConstants.ERROR_UPDATE_FAILED);							
						}
					});
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
				}				
			}
			
			@Override
			public void onFailure(Throwable ex) {
				if(ex instanceof UnauthorizedException) {									
					view.showErrorMessage("You do not have permission to add a Shortcut to entity: " + addToEntityId);
				} else if(ex instanceof ForbiddenException) {			
					view.showErrorMessage("You do not have permission to add a Shortcut to entity: " + addToEntityId);
				} else if(ex instanceof BadRequestException) {
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
				} else if(ex instanceof NotFoundException) {
					view.showErrorMessage(DisplayConstants.ERROR_NOT_FOUND + ":" + addToEntityId);
				} 			
			}			
		});

	}
}
