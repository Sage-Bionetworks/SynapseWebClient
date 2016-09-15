package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.shared.WidgetConstants;

public class EntityListConfigEditorTest {
		
	EntityListConfigEditor editor;
	EntityListConfigView mockView;
	AuthenticationController mockAuthenticationController;

	Map<String, String> descriptor;
	
	@Mock
	EntityListWidget mockEntityListWidget;
	@Mock
	EntityFinder mockEntityFinder;
	@Mock
	PromptModalView mockPromptForNoteModal;
	List<SelectableListItem> entityListRowWidgets;
	@Mock
	EntityListRowBadge mockEntityListRowBadge;
	@Mock
	EntityGroupRecord mockEntityGroupRecord;
	
	@Before
	public void setup() throws Exception{
		MockitoAnnotations.initMocks(this);
		mockView = mock(EntityListConfigView.class);
		mockAuthenticationController = mock(AuthenticationController.class);		
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		entityListRowWidgets = new ArrayList<SelectableListItem>();
		entityListRowWidgets.add(mockEntityListRowBadge);
		when(mockEntityListRowBadge.isSelected()).thenReturn(false);
		when(mockEntityListWidget.getRowWidgets()).thenReturn(entityListRowWidgets);
		when(mockEntityListRowBadge.getRecord()).thenReturn(mockEntityGroupRecord);
		
		// create empty descriptor
		descriptor = new HashMap<String, String>();		
		
		editor = new EntityListConfigEditor(mockView, mockAuthenticationController, mockEntityListWidget, mockEntityFinder, mockPromptForNoteModal);
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		editor.configure(null, descriptor, null);
		//by editing a widget, it always attempts to hide the description.  but you have to touch the entity list to get the update.
		assertFalse(Boolean.parseBoolean(descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY)));
		verify(mockEntityListWidget).configure(null, descriptor, null, null);
		verify(mockView).setButtonToolbarVisible(true);
	}
	
	@Test
	public void testAddRecord() throws Exception {
		editor.onAddRecord();
		boolean showVersions = true;
		ArgumentCaptor<SelectedHandler> captor = ArgumentCaptor.forClass(SelectedHandler.class);
		verify(mockEntityFinder).configure(eq(showVersions), captor.capture());
		verify(mockEntityFinder).show();
		Reference selectedRef = new Reference();
		String targetId = "syn987";
		Long targetVersion = 9L;
		selectedRef.setTargetId(targetId);
		selectedRef.setTargetVersionNumber(targetVersion);
		captor.getValue().onSelected(selectedRef);
		
		verify(mockEntityFinder).hide();
		ArgumentCaptor<EntityGroupRecord> recordCaptor = ArgumentCaptor.forClass(EntityGroupRecord.class);
		verify(mockEntityListWidget).addRecord(recordCaptor.capture());
		EntityGroupRecord capturedRecord = recordCaptor.getValue();
		assertEquals(targetId, capturedRecord.getEntityReference().getTargetId());
		assertEquals(targetVersion, capturedRecord.getEntityReference().getTargetVersionNumber());
	}
	

	private EntityListRowBadge setupRow(boolean isSelected) {
		EntityListRowBadge mockSourceEditor= mock(EntityListRowBadge.class);
		when(mockSourceEditor.isSelected()).thenReturn(isSelected);
		return mockSourceEditor;
	}
	
	@Test
	public void testUpdateNote() {
		String existingNote = "existing note";
		
		EntityListRowBadge s1 = setupRow(false);
		EntityListRowBadge s2 = setupRow(true);
		when(s2.getNote()).thenReturn(existingNote);
		when(mockEntityListWidget.findFirstSelected()).thenReturn(1);
		
		entityListRowWidgets.clear();
		entityListRowWidgets.add(s1);
		entityListRowWidgets.add(s2);
		
		editor.onUpdateNote();
		
		verify(mockPromptForNoteModal).clear();
		verify(mockPromptForNoteModal).configure(EntityListConfigEditor.NOTE, EntityListConfigEditor.PROMPT_ENTER_NOTE, DisplayConstants.SAVE_BUTTON_LABEL, existingNote);
		verify(mockPromptForNoteModal).show();
		
		String newNote = "new note";
		when(mockPromptForNoteModal.getValue()).thenReturn(newNote);
		editor.onUpdateNoteFromModal();
		verify(s2).setNote(newNote);
	}
}












