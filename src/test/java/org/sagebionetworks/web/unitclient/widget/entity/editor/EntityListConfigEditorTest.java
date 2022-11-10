package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SelectableItemList;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.EntityListConfigView;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.test.helper.SelfReturningAnswer;

@RunWith(MockitoJUnitRunner.class)
public class EntityListConfigEditorTest {

  EntityListConfigEditor editor;

  @Mock
  EntityListConfigView mockView;

  @Mock
  AuthenticationController mockAuthenticationController;

  Map<String, String> descriptor;

  @Mock
  EntityListWidget mockEntityListWidget;

  EntityFinderWidget.Builder mockEntityFinderBuilder;

  @Mock
  EntityFinderWidget mockEntityFinder;

  @Mock
  PromptForValuesModalView mockPromptForNoteModal;

  SelectableItemList entityListRowWidgets;

  @Mock
  EntityListRowBadge mockEntityListRowBadge;

  @Mock
  EntityGroupRecord mockEntityGroupRecord;

  @Captor
  ArgumentCaptor<CallbackP<String>> promptCallbackCaptor;

  @Captor
  ArgumentCaptor<EntityFinderWidget.SelectedHandler<List<Reference>>> entityFinderCaptor;

  @Before
  public void setup() throws Exception {
    mockEntityFinderBuilder =
      mock(EntityFinderWidget.Builder.class, new SelfReturningAnswer());
    when(mockEntityFinderBuilder.build()).thenReturn(mockEntityFinder);
    when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
    entityListRowWidgets = new SelectableItemList();
    entityListRowWidgets.add(mockEntityListRowBadge);
    when(mockEntityListRowBadge.isSelected()).thenReturn(false);
    when(mockEntityListWidget.getRowWidgets()).thenReturn(entityListRowWidgets);
    when(mockEntityListRowBadge.getRecord()).thenReturn(mockEntityGroupRecord);

    // create empty descriptor
    descriptor = new HashMap<String, String>();

    editor =
      new EntityListConfigEditor(
        mockView,
        mockAuthenticationController,
        mockEntityListWidget,
        mockEntityFinderBuilder,
        mockPromptForNoteModal
      );
  }

  @Test
  public void testAsWidget() {
    editor.asWidget();
    verify(mockView).asWidget();
  }

  @Test
  public void testConfigure() {
    editor.configure(null, descriptor, null);
    // by editing a widget, it always attempts to hide the description. but you have to touch the entity
    // list to get the update.
    assertFalse(
      Boolean.parseBoolean(
        descriptor.get(WidgetConstants.ENTITYLIST_WIDGET_SHOW_DESCRIPTION_KEY)
      )
    );
    verify(mockEntityListWidget).configure(null, descriptor, null, null);
    verify(mockView).setButtonToolbarVisible(true);
  }

  @Test
  public void testAddRecord() throws Exception {
    editor.onAddRecord();
    boolean showVersions = true;
    verify(mockEntityFinderBuilder).setMultiSelect(true);
    verify(mockEntityFinderBuilder)
      .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED);
    verify(mockEntityFinderBuilder)
      .setSelectedMultiHandler(entityFinderCaptor.capture());
    verify(mockEntityFinderBuilder).build();
    verify(mockEntityFinder).show();
    Reference selectedRef = new Reference();
    String targetId = "syn987";
    Long targetVersion = 9L;
    selectedRef.setTargetId(targetId);
    selectedRef.setTargetVersionNumber(targetVersion);
    List<Reference> selectedRefs = Collections.singletonList(selectedRef);
    entityFinderCaptor.getValue().onSelected(selectedRefs, mockEntityFinder);

    verify(mockEntityFinder).hide();
    ArgumentCaptor<EntityGroupRecord> recordCaptor = ArgumentCaptor.forClass(
      EntityGroupRecord.class
    );
    verify(mockEntityListWidget).addRecord(recordCaptor.capture());
    EntityGroupRecord capturedRecord = recordCaptor.getValue();
    assertEquals(targetId, capturedRecord.getEntityReference().getTargetId());
    assertEquals(
      targetVersion,
      capturedRecord.getEntityReference().getTargetVersionNumber()
    );
  }

  private EntityListRowBadge setupRow(boolean isSelected) {
    EntityListRowBadge mockSourceEditor = mock(EntityListRowBadge.class);
    when(mockSourceEditor.isSelected()).thenReturn(isSelected);
    return mockSourceEditor;
  }

  @Test
  public void testUpdateNote() {
    String existingNote = "existing note";

    EntityListRowBadge s1 = setupRow(false);
    EntityListRowBadge s2 = setupRow(true);
    when(s2.getNote()).thenReturn(existingNote);

    entityListRowWidgets.clear();
    entityListRowWidgets.add(s1);
    entityListRowWidgets.add(s2);

    editor.onUpdateNote();

    verify(mockPromptForNoteModal)
      .configureAndShow(
        eq(EntityListConfigEditor.NOTE),
        eq(EntityListConfigEditor.PROMPT_ENTER_NOTE),
        eq(existingNote),
        promptCallbackCaptor.capture()
      );

    String newNote = "new note";
    promptCallbackCaptor.getValue().invoke(newNote);
    verify(s2).setNote(newNote);
  }
}
