package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.widget.EntityListWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil.RowLoadedHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListConfigEditor implements EntityListConfigView.Presenter, WidgetEditorPresenter {
	
	private EntityListConfigView view;
	private EntityListWidgetDescriptor descriptor;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public EntityListConfigEditor(EntityListConfigView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof EntityListWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		descriptor = (EntityListWidgetDescriptor)widgetDescriptor;
		
		//set up view based on descriptor parameters
		descriptor = (EntityListWidgetDescriptor)widgetDescriptor;
		final boolean isLoggedIn = authenticationController.isLoggedIn();
		
		view.configure();

		List<EntityGroupRecord> records = descriptor.getRecords();
		if(records != null) {
			for(int i=0; i<records.size(); i++) {
				final int rowIndex = i;
				EntityListUtil.loadIndividualRowDetails(synapseClient, nodeModelCreator, isLoggedIn, descriptor, rowIndex, new RowLoadedHandler() {					
					@Override
					public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay) {
						view.setEntityGroupRecordDisplay(rowIndex, entityGroupRecordDisplay, isLoggedIn);
					}
				});
			}			
		}

	}
	
	@Override
	public void addRecord(final String entityId, Long versionNumber, String note) {
		final boolean isLoggedIn = authenticationController.isLoggedIn();
		
		// add record
		List<EntityGroupRecord> records = descriptor.getRecords();
		if(records == null) {
			records = new ArrayList<EntityGroupRecord>();
			descriptor.setRecords(records);
		}
		final int addedIndex = records.size();
		EntityGroupRecord record = createRecord(entityId, versionNumber, note);
		records.add(record);
		try {
		EntityListUtil.loadIndividualRowDetails(synapseClient, nodeModelCreator, isLoggedIn, descriptor, addedIndex, new RowLoadedHandler() {					
			@Override
			public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay) {
				view.setEntityGroupRecordDisplay(addedIndex, entityGroupRecordDisplay, isLoggedIn);
			}
		});	
		} catch (IllegalArgumentException e) {
			view.showErrorMessage(DisplayConstants.ERROR_SAVE_MESSAGE);
		}
	}

	
	@Override
	public void removeRecord(int row) {
		List<EntityGroupRecord> records = descriptor.getRecords();
		if(records != null && records.size() > row) {
			records.remove(row);
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_SAVE_MESSAGE);
		}
	}
	@Override
	public void updateNote(int row, String note) {
		List<EntityGroupRecord> records = descriptor.getRecords();
		if(records != null && records.size() > row) {
			records.get(row).setNote(note);
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_SAVE_MESSAGE);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		//update widget descriptor from the view
		view.checkParams();		
	}
	
	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	@Override
	public String getTextToInsert(String name) {
		return null;
	}
	
	@Override
	public void setNameProvider(WidgetNameProvider provider) {
	}
	
	/*
	 * Private Methods
	 */
	private EntityGroupRecord createRecord(String entityId, Long versionNumber, String note) {
		Reference ref = new Reference();
		ref.setTargetId(entityId);			
		ref.setTargetVersionNumber(versionNumber);

		EntityGroupRecord record = new EntityGroupRecord();
		record.setEntityReference(ref);
		record.setNote(note);
		return record;
	}

}
