package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
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
	PromptForValuesModalView promptForNoteModal;

	@Inject
	public EntityListConfigEditor(EntityListConfigView view, AuthenticationController authenticationController, EntityListWidget entityListWidget, EntityFinder entityFinder, PromptForValuesModalView promptForNoteModal) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.entityFinder = entityFinder;
		this.entityListWidget = entityListWidget;
		this.promptForNoteModal = promptForNoteModal;
		view.setSelectionToolbarHandler(entityListWidget.getRowWidgets());
		view.setEntityListWidget(entityListWidget.asWidget());
		view.setPresenter(this);
		view.addWidget(promptForNoteModal.asWidget());
		view.initView();
		entityListWidget.setSelectable(view);
		entityListWidget.setSelectionChangedCallback(new Callback() {
			@Override
			public void invoke() {
				refreshCanEditNoteState();
			}
		});
	}

	public void refreshCanEditNoteState() {
		int count = 0;
		for (SelectableListItem row : entityListWidget.getRowWidgets()) {
			if (row.isSelected()) {
				count++;
			}
		}
		view.setCanEditNote(count == 1);
		CheckBoxState state = CheckBoxState.getStateFromCount(count, entityListWidget.getRowWidgets().size());
		view.setSelectionState(state);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		if (widgetDescriptor == null)
			throw new IllegalArgumentException("Descriptor can not be null");
		// set up view based on descriptor parameters
		this.wikiKey = wikiKey;
		descriptor = widgetDescriptor;
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY, Boolean.FALSE.toString());
		refresh();
	}

	public void refresh() {
		entityListWidget.configure(wikiKey, descriptor, null, null);
		boolean isRow = entityListWidget.getRowWidgets().size() > 0;
		view.setButtonToolbarVisible(isRow);
		refreshCanEditNoteState();
	}

	@Override
	public void onAddRecord() {
		entityFinder.configure(true, new SelectedHandler<Reference>() {
			@Override
			public void onSelected(Reference selected) {
				entityFinder.hide();
				EntityGroupRecord record = createRecord(selected.getTargetId(), selected.getTargetVersionNumber(), null);
				entityListWidget.addRecord(record);
				view.setButtonToolbarVisible(true);
			}
		});
		entityFinder.show();
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
		// update widget descriptor from the view
		view.checkParams();
		// update descriptor based on current badges
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		for (SelectableListItem row : entityListWidget.getRowWidgets()) {
			records.add(((EntityListRowBadge) row).getRecord());
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
		int index = entityListWidget.getRowWidgets().findFirstSelected();
		List<SelectableListItem> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = (EntityListRowBadge) editors.get(index);
		return editor.getNote();
	}

	public void setSelectedNote(String newNote) {
		int index = entityListWidget.getRowWidgets().findFirstSelected();
		List<SelectableListItem> editors = entityListWidget.getRowWidgets();
		EntityListRowBadge editor = (EntityListRowBadge) editors.get(index);
		editor.setNote(newNote);
	}

	@Override
	public void onUpdateNote() {
		promptForNoteModal.configureAndShow(NOTE, PROMPT_ENTER_NOTE, getSelectedNote(), newValue -> {
			promptForNoteModal.hide();
			setSelectedNote(newValue);
		});
	}
}
