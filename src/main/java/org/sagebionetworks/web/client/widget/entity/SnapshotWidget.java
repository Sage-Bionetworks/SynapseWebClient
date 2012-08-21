package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Snapshot;
import org.sagebionetworks.repo.model.SnapshotGroup;
import org.sagebionetworks.repo.model.SnapshotGroupRecord;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget is a view/edit display for a Snapshot Entity
 * 
 * @author Dburdick
 *
 */
public class SnapshotWidget implements SnapshotWidgetView.Presenter, IsWidget {
	
	private AdapterFactory factory;
	private SnapshotWidgetView view;
	private Snapshot snapshot;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private boolean canEdit = false;
	private boolean readOnly = false;
	
	/**
	 * 
	 * @param factory
	 * @param cache
	 * @param propertyView
	 */
	@Inject
	public SnapshotWidget(AdapterFactory factory,
			SnapshotWidgetView propertyView, SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		super();
		this.factory = factory;
		this.view = propertyView;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}
	
	public void setSnapshot(Snapshot snapshot, boolean canEdit, boolean readOnly) {				
		this.snapshot = snapshot;
		this.canEdit = canEdit;
		this.readOnly = readOnly;
		
		// add a default group if there are none, but don't persist unless record is added
		if(snapshot != null && (snapshot.getGroups() == null || snapshot.getGroups().size() == 0)) {
			SnapshotGroup defaultGroup = new SnapshotGroup();
			defaultGroup.setName(DisplayConstants.CONTENTS);			
			snapshot.setGroups(new ArrayList<SnapshotGroup>(Arrays.asList(new SnapshotGroup[] { defaultGroup })));			
		}		
		view.setSnapshot(snapshot, canEdit, readOnly);		
	}	
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return view.asWidget();
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}
	
	/**
	 * Loads the details for all entities referenced in the SnapshotGroupRecords and sends to the view.
	 * Generally the view should be ready to accept these rows 
	 */
	@Override
	public void loadRowDetails() {
		// iterate through all groups, all records and call setSnapshotGroupRecordDisplay
		// TODO : do in batch		
		if(snapshot == null) return;
		if(snapshot.getGroups() == null) return;		
		for(int groupIndex=0; groupIndex<snapshot.getGroups().size(); groupIndex++) {
			SnapshotGroup group = snapshot.getGroups().get(groupIndex);			
			if(group.getRecords() == null) continue;
			for(int rowIndex=0; rowIndex<group.getRecords().size(); rowIndex++) {
				loadIndividualRowDetails(groupIndex, rowIndex);
			}
		}
	}

	/**
	 * Adds a group to the entity
	 */
	@Override
	public SnapshotGroup addGroup(final String name, final String description) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return null;
		}		
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return null;
		}
		if(name == null || name.equals("")) {
			// The view should keep this from happening
			view.showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
			rebuildEverything();
			return null;
		}

		// add group
		final SnapshotGroup group = new SnapshotGroup();
		group.setName(name);
		group.setDescription(description); // can be null
		group.setRecords(new ArrayList<SnapshotGroupRecord>());
		if(snapshot.getGroups() == null) snapshot.setGroups(new ArrayList<SnapshotGroup>());
		snapshot.getGroups().add(group);				
		updateSnapshot(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.GROUP_ADDED, name + " " + DisplayConstants.GROUP_ADDED);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}
		});
		
		return group;
	}

	@Override
	public void updateGroup(int groupIndex, String name, String description) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return;
		}
		if(name == null || name.equals("")) {
			// The view should keep this from happening
			view.showErrorMessage(DisplayConstants.ERROR_NAME_MUST_BE_DEFINED);
			rebuildEverything();
			return;
		}		
		if(groupIndex < 0 || groupIndex >= snapshot.getGroups().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}

		// update group
		SnapshotGroup group = snapshot.getGroups().get(groupIndex);
		group.setName(name);
		group.setDescription(description);
		updateSnapshot(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.UPDATE_SAVED, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}				
		});
	}

	@Override
	public void removeGroup(int groupIndex) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return;
		}
		if(groupIndex < 0 || groupIndex >= snapshot.getGroups().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}

		// remove group
		snapshot.getGroups().remove(groupIndex);
		updateSnapshot(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.GROUP_REMOVED, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}				
		});
	}

	@Override
	public void addGroupRecord(final int groupIndex, final String entityId, String version, String note) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return;
		}
		if(groupIndex < 0 || groupIndex >= snapshot.getGroups().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}
		// version		
		Long versionNumber = null;
		try {
			versionNumber = Long.parseLong(version);
		} catch (NumberFormatException ex) {
			view.showErrorMessage(DisplayConstants.ERROR_INVALID_VERSION_FORMAT);
			return;
		}

		// add group record
		SnapshotGroup group = snapshot.getGroups().get(groupIndex);
		if(group.getRecords() == null) group.setRecords(new ArrayList<SnapshotGroupRecord>());		
		final int addedIndex = snapshot.getGroups().get(groupIndex).getRecords().size();
		SnapshotGroupRecord record = createRecord(entityId, versionNumber, note);			
		snapshot.getGroups().get(groupIndex).getRecords().add(record);		
		updateSnapshot(new AsyncCallback<String>() {				
			@Override
			public void onSuccess(String result) {
				// load entity details and show in view
				loadIndividualRowDetails(groupIndex, addedIndex);			
				view.showInfo(DisplayConstants.ENTRY_ADDED, DisplayConstants.SYNAPSE_ENTITY + " " + entityId);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}
		});
	}

	@Override
	public void updateGroupRecord(int groupIndex, int rowIndex, String note) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return;
		}
		if(groupIndex < 0 || groupIndex >= snapshot.getGroups().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}
		if(rowIndex < 0 
				|| snapshot.getGroups() == null
				|| snapshot.getGroups().get(groupIndex) == null
				|| snapshot.getGroups().get(groupIndex).getRecords() == null
				|| rowIndex >= snapshot.getGroups().get(groupIndex).getRecords().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}

		// update record
		SnapshotGroupRecord record = snapshot.getGroups().get(groupIndex).getRecords().get(rowIndex);
		record.setNote(note);
		updateSnapshot(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.UPDATE_SAVED, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}
		});
	}

	
	@Override
	public void removeGroupRecord(int groupIndex, int rowIndex) {
		// preconditions
		if(readOnly) {
			view.showErrorMessage(DisplayConstants.ERROR_IN_READ_ONLY_MODE);
			return;
		}
		if(!canEdit) {
			view.showErrorMessage(DisplayConstants.ERROR_NO_EDIT_PERMISSION);
			return;
		}
		if(groupIndex < 0 || groupIndex >= snapshot.getGroups().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}
		if(rowIndex < 0 || rowIndex >= snapshot.getGroups().get(groupIndex).getRecords().size()) {
			view.showErrorMessage(DisplayConstants.ERROR_GENERIC);
			rebuildEverything();
			return;
		}

		// remove record
		snapshot.getGroups().get(groupIndex).getRecords().remove(rowIndex);
		updateSnapshot(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo(DisplayConstants.ENTRY_REMOVED, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);
				rebuildEverything();
			}
		});
	}


	/*
	 * Private methods
	 */

	private SnapshotGroupRecordDisplay getUnauthDisplay() {
		return new SnapshotGroupRecordDisplay(
				null,
				SafeHtmlUtils.fromSafeConstant(DisplayConstants.TITLE_UNAUTHORIZED),
				null, null, null, null, null, null, null);
	}

	private void updateSnapshot(final AsyncCallback<String> callback) {
		try {
			// update current entity
			synapseClient.updateEntity(snapshot.writeToJSONObject(factory.createNew()).toJSONString(), new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					try {
						snapshot = nodeModelCreator.createEntity(result.getEntityJson(), result.getEntityClassName());
					} catch (RestServiceException e) {
						onFailure(e);
					}
					callback.onSuccess(null);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}
	}

	private SnapshotGroupRecord createRecord(String entityId, Long versionNumber, String note) {
		Reference ref = new Reference();
		ref.setTargetId(entityId);			
		ref.setTargetVersionNumber(versionNumber);

		SnapshotGroupRecord record = new SnapshotGroupRecord();
		record.setEntityReference(ref);
		record.setNote(note);
		return record;
	}

	/**
	 * It is a lot easier to get back to the proper state in both presenter and view if we rebuild all
	 * Generally this is used for failures as successful atomic changes are simple to represent
	 */
	private void rebuildEverything() {
		synapseClient.getEntity(snapshot.getId(), new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					// update current entity
					snapshot = nodeModelCreator.createEntity(result, Snapshot.class);
					setSnapshot(snapshot, canEdit, readOnly);
				} catch (RestServiceException e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				setSnapshot(null, canEdit, readOnly);
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}	

	private SnapshotGroupRecordDisplay createRecordDisplay(
			SnapshotGroupRecord record, EntityWrapper result)
			throws RestServiceException {	
		Entity referencedEntity = nodeModelCreator.createEntity(result);
				
		// download
		String downloadUrl = null;
		if(referencedEntity instanceof Locationable) {
			List<LocationData> locations = ((Locationable) referencedEntity).getLocations();
			if(locations != null && locations.size() > 0) {
				downloadUrl = locations.get(0).getPath();
			}
		}
		
		String nameLinkUrl;
		if(referencedEntity instanceof Versionable) {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId(), ((Versionable)referencedEntity).getVersionNumber());
		} else {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId());
		}
		
		// version
		String version = "N/A";
		if(referencedEntity instanceof Versionable) {
			version = DisplayUtils.getVersionDisplay((Versionable)referencedEntity);
		}							
		
		// desc
		SafeHtml descSafe = referencedEntity.getDescription() == null ? 
				SafeHtmlUtils.fromSafeConstant("") 
				: new SafeHtmlBuilder().appendEscapedLines(referencedEntity.getDescription()).toSafeHtml();  
		
		// note
		SafeHtml noteSafe = record.getNote() == null ? 
				SafeHtmlUtils.fromSafeConstant("")
				: new SafeHtmlBuilder().appendEscapedLines(record.getNote()).toSafeHtml();
				
		return new SnapshotGroupRecordDisplay(
				referencedEntity.getId(),
				SafeHtmlUtils.fromString(referencedEntity.getName()),
				nameLinkUrl,
				downloadUrl, descSafe,
				SafeHtmlUtils.fromString(version),
				referencedEntity.getModifiedOn(),
				referencedEntity.getCreatedBy() == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromString(referencedEntity.getCreatedBy()),
				noteSafe);		
	}

	private void loadIndividualRowDetails(final int groupIndex, final int rowIndex) {
		SnapshotGroup group = snapshot.getGroups().get(groupIndex);		
		final SnapshotGroupRecord record = snapshot.getGroups().get(groupIndex).getRecords().get(rowIndex);
		final Reference ref = record.getEntityReference();
		if(ref == null) return;
				
		AsyncCallback<EntityWrapper> callback = new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					SnapshotGroupRecordDisplay display = createRecordDisplay(record, result);								
					view.setSnapshotGroupRecordDisplay(groupIndex, rowIndex, display);									
				} catch (RestServiceException e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof UnauthorizedException) {
					SnapshotGroupRecordDisplay unauthDisplay = getUnauthDisplay();
					unauthDisplay.setEntityId(ref.getTargetId());
					view.setSnapshotGroupRecordDisplay(groupIndex, rowIndex, unauthDisplay);
				} else {
					view.showErrorMessage(DisplayConstants.ERROR_FAILED_PERSIST);					
				}
			}
		};
		if(ref.getTargetVersionNumber() != null) {
			synapseClient.getEntityForVersion(ref.getTargetId(), ref.getTargetVersionNumber(), callback);
		} else {
			// failsafe
			synapseClient.getEntity(ref.getTargetId(), callback);
		}
	}	

}
