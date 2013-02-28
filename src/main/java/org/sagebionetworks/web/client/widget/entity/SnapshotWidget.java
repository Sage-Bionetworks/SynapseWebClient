package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityGroup;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

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
	
	private static final int MAX_DESCRIPTION_CHARS = 165;
	private AdapterFactory factory;
	private SnapshotWidgetView view;
	private Summary snapshot;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	private boolean canEdit = false;
	private boolean readOnly = false;
	private boolean isLoggedIn = false;
	
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
			AuthenticationController authenticationController,
			SynapseJSNIUtils synapseJSNIUtils) {
		super();
		this.factory = factory;
		this.view = propertyView;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
	}
	
	public void setSnapshot(Summary snapshot, boolean canEdit, boolean readOnly) {		
		this.snapshot = snapshot;
		this.canEdit = canEdit;
		this.readOnly = readOnly;
		
		boolean showEdit = canEdit;
		isLoggedIn = authenticationController.isLoggedIn();		
		
		// add a default group if there are none, but don't persist unless record is added
		if(snapshot != null && (snapshot.getGroups() == null || snapshot.getGroups().size() == 0)) {
			EntityGroup defaultGroup = new EntityGroup();
			defaultGroup.setName(DisplayConstants.CONTENTS);			
			snapshot.setGroups(new ArrayList<EntityGroup>(Arrays.asList(new EntityGroup[] { defaultGroup })));			
		} else {
			showEdit = false;
		}
		
		view.setSnapshot(snapshot, canEdit, readOnly, showEdit);		
	}	
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return view.asWidget();
	}

	public Summary getSnapshot() {
		return snapshot;
	}
	
	@Override
	public void setShowEditor(boolean show) {
		if(snapshot != null) {
			view.setSnapshot(snapshot, canEdit, readOnly, show);
		}
	}	

	/**
	 * Loads the details for all entities referenced in the EntityGroupRecords and sends to the view.
	 * Generally the view should be ready to accept these rows 
	 */
	@Override
	public void loadRowDetails() {
		// iterate through all groups, all records and call setEntityGroupRecordDisplay
		// TODO : do in batch		
		if(snapshot == null) return;
		if(snapshot.getGroups() == null) return;		
		for(int groupIndex=0; groupIndex<snapshot.getGroups().size(); groupIndex++) {
			EntityGroup group = snapshot.getGroups().get(groupIndex);			
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
	public EntityGroup addGroup(final String name, final String description) {
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
		final EntityGroup group = new EntityGroup();
		group.setName(name);
		group.setDescription(description); // can be null
		group.setRecords(new ArrayList<EntityGroupRecord>());
		if(snapshot.getGroups() == null) snapshot.setGroups(new ArrayList<EntityGroup>());
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
		EntityGroup group = snapshot.getGroups().get(groupIndex);
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
		EntityGroup group = snapshot.getGroups().get(groupIndex);
		if(group.getRecords() == null) group.setRecords(new ArrayList<EntityGroupRecord>());		
		final int addedIndex = snapshot.getGroups().get(groupIndex).getRecords().size();
		EntityGroupRecord record = createRecord(entityId, versionNumber, note);			
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
		EntityGroupRecord record = snapshot.getGroups().get(groupIndex).getRecords().get(rowIndex);
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
	private EntityGroupRecordDisplay getEmptyDisplay() {
		return new EntityGroupRecordDisplay(
				"",
				SafeHtmlUtils.EMPTY_SAFE_HTML,
				null, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML);
	}

	private EntityGroupRecordDisplay getGenericErrorDisplay(String id, Long version) {
		String msg = "Error Loading: " + id;
		if(version != null) msg += ", Version " + version;
		return new EntityGroupRecordDisplay(
				null,
				SafeHtmlUtils.fromSafeConstant(msg),
				null, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML, null, SafeHtmlUtils.EMPTY_SAFE_HTML, SafeHtmlUtils.EMPTY_SAFE_HTML);
	}

	private void updateSnapshot(final AsyncCallback<String> callback) {
		try {
			// update current entity
			synapseClient.updateEntity(snapshot.writeToJSONObject(factory.createNew()).toJSONString(), new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					try {
						snapshot = nodeModelCreator.createJSONEntity(result.getEntityJson(), result.getEntityClassName());
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
					callback.onSuccess(null);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
		}
	}

	private EntityGroupRecord createRecord(String entityId, Long versionNumber, String note) {
		Reference ref = new Reference();
		ref.setTargetId(entityId);			
		ref.setTargetVersionNumber(versionNumber);

		EntityGroupRecord record = new EntityGroupRecord();
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
					snapshot = nodeModelCreator.createJSONEntity(result.getEntityJson(), Summary.class);
					setSnapshot(snapshot, canEdit, readOnly);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				setSnapshot(null, canEdit, readOnly);
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}	

	private EntityGroupRecordDisplay createRecordDisplay(
			EntityGroupRecord record, EntityWrapper result)
			throws JSONObjectAdapterException {	
		Entity referencedEntity = nodeModelCreator.createEntity(result);
				
		String nameLinkUrl;
		if(referencedEntity instanceof Versionable) {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId(), ((Versionable)referencedEntity).getVersionNumber());
		} else {
			nameLinkUrl = DisplayUtils.getSynapseHistoryTokenNoHash(referencedEntity.getId());
		}

		// download
		String downloadUrl = null;
		if (referencedEntity instanceof FileEntity) {
			if(!isLoggedIn)
				downloadUrl = "#!" + nameLinkUrl;
			else
				downloadUrl = DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), referencedEntity.getId(), ((Versionable)referencedEntity).getVersionNumber(), false);
		}
		else if(referencedEntity instanceof Locationable) {
			List<LocationData> locations = ((Locationable) referencedEntity).getLocations();
			if(locations != null && locations.size() > 0) {
				downloadUrl = locations.get(0).getPath();
			} else if(!isLoggedIn) {				
				downloadUrl = "#!" + nameLinkUrl;
			}
		}
		
		// version
		String version = "N/A";
		if(referencedEntity instanceof Versionable) {
			version = DisplayUtils.getVersionDisplay((Versionable)referencedEntity);
		}							
		
		// desc
		String description = referencedEntity.getDescription() == null ? "" : referencedEntity.getDescription();
		description = description.replaceAll("\\n", " "); // keep to 3 lines by removing new lines
		if(description.length() > MAX_DESCRIPTION_CHARS) 
			description = description.substring(0, MAX_DESCRIPTION_CHARS) + " ...";
		SafeHtml descSafe =  new SafeHtmlBuilder().appendEscapedLines(description).toSafeHtml();  
		
		// note
		SafeHtml noteSafe = record.getNote() == null ? 
				SafeHtmlUtils.fromSafeConstant("")
				: new SafeHtmlBuilder().appendEscapedLines(record.getNote()).toSafeHtml();
				
		return new EntityGroupRecordDisplay(
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
		EntityGroup group = snapshot.getGroups().get(groupIndex);		
		final EntityGroupRecord record = snapshot.getGroups().get(groupIndex).getRecords().get(rowIndex);
		final Reference ref = record.getEntityReference();
		if(ref == null) return;
				
		AsyncCallback<EntityWrapper> callback = new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					EntityGroupRecordDisplay display = createRecordDisplay(record, result);								
					view.setEntityGroupRecordDisplay(groupIndex, rowIndex, display, isLoggedIn);									
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				EntityGroupRecordDisplay errorDisplay = getEmptyDisplay();
				errorDisplay.setEntityId(ref.getTargetId());
				errorDisplay.setVersion(SafeHtmlUtils.fromSafeConstant(ref.getTargetVersionNumber().toString()));
				String msg = ref.getTargetId();
				if(ref.getTargetVersionNumber() != null) msg += ", Version " + ref.getTargetVersionNumber();
				if(caught instanceof UnauthorizedException || caught instanceof ForbiddenException) {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.TITLE_UNAUTHORIZED + ": " + msg));
				} else if (caught instanceof NotFoundException) {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.NOT_FOUND + ": " + msg));
				} else {
					errorDisplay.setName(SafeHtmlUtils.fromSafeConstant(DisplayConstants.ERROR_LOADING + ": " + msg));
				}
				view.setEntityGroupRecordDisplay(groupIndex, rowIndex, errorDisplay, isLoggedIn);
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
