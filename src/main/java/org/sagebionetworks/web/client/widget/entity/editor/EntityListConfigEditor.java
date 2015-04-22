package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil.RowLoadedHandler;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class EntityListConfigEditor implements EntityListConfigView.Presenter, WidgetEditorPresenter {
	
	private EntityListConfigView view;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Map<String, String> descriptor;
	List<EntityGroupRecord> records;
	AuthenticationController authenticationController;

	@Inject
	public EntityListConfigEditor(EntityListConfigView view,
			SynapseClientAsync synapseClient, SynapseJSNIUtils synapseJSNIUtils,
			AuthenticationController authenticationController) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		if (widgetDescriptor == null) throw new IllegalArgumentException("Descriptor can not be null");
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		final boolean isLoggedIn = authenticationController.isLoggedIn();
		
		view.configure();

		records = EntityListUtil.parseRecords(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY));
		if(records != null && !records.equals("")) {
			for(int i=0; i<records.size(); i++) {
				final int rowIndex = i;
				EntityListUtil.loadIndividualRowDetails(synapseClient, synapseJSNIUtils, isLoggedIn, records, rowIndex, new RowLoadedHandler() {					
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

		// add record to list of records
		final int addedIndex = records.size();
		EntityGroupRecord record = createRecord(entityId, versionNumber, note);
		records.add(record);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, EntityListUtil.recordsToString(records));
		try {
			EntityListUtil.loadIndividualRowDetails(synapseClient, synapseJSNIUtils, isLoggedIn, records, addedIndex, new RowLoadedHandler() {					
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
		if(records != null && records.size() > row) {
			records.remove(row);
			descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, EntityListUtil.recordsToString(records));
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_SAVE_MESSAGE);
		}
	}
	@Override
	public void updateNote(int row, String note) {
		if(records != null && records.size() > row) {
			records.get(row).setNote(note);
			descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, EntityListUtil.recordsToString(records));
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
	public String getTextToInsert() {
		return null;
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

	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
}
