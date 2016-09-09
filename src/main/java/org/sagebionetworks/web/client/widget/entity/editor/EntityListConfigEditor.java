package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class EntityListConfigEditor implements EntityListConfigView.Presenter, WidgetEditorPresenter {
	
	public static final String PROMPT_ENTER_NOTE = "Enter note";
	public static final String NOTE = "Note";
	private EntityListConfigView view;
	private Map<String, String> descriptor;
	AuthenticationController authenticationController;
	EntityFinder entityFinder;
	EntityListWidget entityListWidget;
	WikiPageKey wikiKey;
	boolean changingSelection;
	PromptModalView promptForNoteModal;
	@Inject
	public EntityListConfigEditor(EntityListConfigView view,
			AuthenticationController authenticationController,
			EntityListWidget entityListWidget,
			EntityFinder entityFinder,
			PromptModalView promptForNoteModal) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityFinder = entityFinder;
		this.entityListWidget = entityListWidget;
		this.promptForNoteModal = promptForNoteModal;
		view.setEntityListWidget(entityListWidget.asWidget());
		view.setPresenter(this);
		view.addWidget(promptForNoteModal.asWidget());
		view.initView();
		entityListWidget.setIsSelectable(true);
		changingSelection = false;
		promptForNoteModal.setPresenter(new PromptModalView.Presenter() {
			@Override
			public void onPrimary() {
				onUpdateNoteFromModal();
			}
		});
		entityListWidget.setSelectionChangedCallback(new Callback() {
			@Override
			public void invoke() {
				checkSelectionState();
			}
		});
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		if (widgetDescriptor == null) throw new IllegalArgumentException("Descriptor can not be null");
		//set up view based on descriptor parameters
		this.wikiKey = wikiKey;
		descriptor = widgetDescriptor;
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY, Boolean.FALSE.toString());
		refresh();
	}
	
	private void refresh() {
		entityListWidget.configure(wikiKey, descriptor, null, null);
		boolean isRow = entityListWidget.getRowWidgets().size() > 0;
		view.setButtonToolbarVisible(isRow);
		checkSelectionState();
	}
	
	@Override
	public void onAddRecord() {
		entityFinder.configure(true, new SelectedHandler<Reference>() {					
			@Override
			public void onSelected(Reference selected) {
				entityFinder.hide();
				EntityGroupRecord record = createRecord(selected.getTargetId(), selected.getTargetVersionNumber(), null);
				entityListWidget.addRecord(record);
			}
		});
		entityFinder.show();	
	}
	

	public void selectAll() {
		changeAllSelection(true);
	}

	public void selectNone() {
		changeAllSelection(false);
	}

	public void onMoveUp() {
		int index = findFirstSelected();
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = editors.get(index);
		editors.remove(index);
		editors.add(index-1, editor);
		entityListWidget.refresh();
		checkSelectionState();
	}

	public void onMoveDown() {
		int index = findFirstSelected();
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = editors.get(index);
		editors.remove(index);
		editors.add(index+1, editor);
		entityListWidget.refresh();
		checkSelectionState();
	}

	public void deleteSelected() {
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		Iterator<EntityListRowBadge> it = editors.iterator();
		while(it.hasNext()){
			EntityListRowBadge row = it.next();
			if(row.isSelected()){
				it.remove();
			}
		}
		entityListWidget.refresh();
		checkSelectionState();
	}

	/**
	 * Find the first selected row.
	 * @return
	 */
	private int findFirstSelected(){
		int index = 0;
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		for(EntityListRowBadge row: editors){
			if(row.isSelected()){
				return index;
			}
			index++;
		}
		throw new IllegalStateException("Nothing selected");
	}
	
	public void selectionChanged(boolean isSelected) {
		checkSelectionState();
	}
	
	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select){
		try{
			List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
			changingSelection = true;
			// Select all 
			for(EntityListRowBadge row: editors){
				row.setSelected(select);
			}
		}finally{
			changingSelection = false;
		}
		checkSelectionState();
	}
	
	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState(){
		if(!changingSelection){
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
			for(EntityListRowBadge row: editors) {
				if(row.isSelected()){
					count++;
					lastIndex = index;
				}
				index++;
			}
			view.setCanDelete(count > 0);
			view.setCanMoveUp(count == 1 && lastIndex > 0);
			view.setCanMoveDown(count == 1 && lastIndex < editors.size()-1);
			view.setCanEditNote(count == 1);
		}
	}
	
	
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
		//update descriptor based on current badges
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		for (EntityListRowBadge row : entityListWidget.getRowWidgets()) {
			records.add(row.getRecord());
		}
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, EntityListUtil.recordsToString(records));
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
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
	
	public String getSelectedNote() {
		int index = findFirstSelected();
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = editors.get(index);
		return editor.getNote();
	}
	
	public void setSelectedNote(String newNote) {
		int index = findFirstSelected();
		List<EntityListRowBadge> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = editors.get(index);
		editor.setNote(newNote);
	}


	@Override
	public void onUpdateNote() {
		promptForNoteModal.clear();
		promptForNoteModal.configure(NOTE, PROMPT_ENTER_NOTE, DisplayConstants.SAVE_BUTTON_LABEL, getSelectedNote());
		promptForNoteModal.show();
	}
	
	public void onUpdateNoteFromModal() {
		promptForNoteModal.hide();
		setSelectedNote(promptForNoteModal.getValue());
	}
}
