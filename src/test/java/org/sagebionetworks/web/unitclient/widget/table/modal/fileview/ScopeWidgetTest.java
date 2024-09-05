package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.EntityViewScopeEditorModalProps;
import org.sagebionetworks.web.client.widget.entity.EntityViewScopeEditorModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ScopeWidgetTest {

  @Mock
  EntityViewScopeWidgetView mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  EntityContainerListWidget mockViewScopeWidget;

  @Mock
  EntityViewScopeEditorModalWidget mockEntityViewScopeEditorModalWidget;

  @Mock
  EntityBundle mockBundle;

  @Mock
  List<String> mockNewScopeIds;

  EntityViewScopeWidget widget;

  @Mock
  EntityView mockEntityView;

  @Mock
  EntityView mockUpdatedEntityView;

  @Mock
  Table mockTable;

  @Mock
  EventBus mockEventBus;

  String scopeId1 = "syn456";
  String scopeId2 = "syn789";

  String entityId = "syn123";

  // Versions for Dataset only
  Long scopeVersion1 = 5L;
  Long scopeVersion2 = 2L;

  List<String> mockScopeIds = Arrays.asList(scopeId1, scopeId2);
  List<Reference> mockReferencesWithoutVersions;
  List<Reference> mockReferencesWithVersions;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    widget =
      new EntityViewScopeWidget(
        mockView,
        mockJsClient,
        mockViewScopeWidget,
        mockEntityViewScopeEditorModalWidget,
        mockEventBus
      );
    when(mockBundle.getEntity()).thenReturn(mockEntityView);
    when(mockEntityView.getId()).thenReturn(entityId);
    when(mockEntityView.getScopeIds()).thenReturn(mockScopeIds);
    when(mockEntityView.getType()).thenReturn(ViewType.file);
    when(mockEntityView.getViewTypeMask()).thenReturn(null);
    AsyncMockStubber
      .callSuccessWith(mockUpdatedEntityView)
      .when(mockJsClient)
      .updateEntity(
        any(Table.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    when(mockView.isFileSelected()).thenReturn(false);
    when(mockView.isFolderSelected()).thenReturn(false);
    when(mockView.isFolderSelected()).thenReturn(false);
    when(mockView.isTableSelected()).thenReturn(false);

    mockReferencesWithoutVersions = new ArrayList<>();
    Reference unversionedRef1 = new Reference();
    unversionedRef1.setTargetId(scopeId1);
    Reference unversionedRef2 = new Reference();
    unversionedRef2.setTargetId(scopeId2);
    mockReferencesWithoutVersions.add(unversionedRef1);
    mockReferencesWithoutVersions.add(unversionedRef2);

    mockReferencesWithVersions = new ArrayList<>();
    Reference versionedRef1 = new Reference();
    versionedRef1.setTargetId(scopeId1);
    versionedRef1.setTargetVersionNumber(scopeVersion1);
    Reference versionedRef2 = new Reference();
    versionedRef2.setTargetId(scopeId2);
    versionedRef2.setTargetVersionNumber(scopeVersion2);
    mockReferencesWithVersions.add(versionedRef1);
    mockReferencesWithVersions.add(versionedRef2);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(widget);
    verify(mockView).setEditableEntityViewModalWidget(any());
    verify(mockView).setEntityListWidget(any());
  }

  @Test
  public void testOnEditScopeAndMaskOnUpdate() {
    // configure with an entityview, edit the scope, and save.
    boolean isEditable = true;
    widget.configure(mockBundle, isEditable);

    // The view scope widget does not allow edit of the scope. That occurs in the modal (with the
    // editScopeWidget)
    verify(mockViewScopeWidget)
      .configure(mockReferencesWithoutVersions, false, TableType.file_view);
    verify(mockView).setEditMaskAndScopeButtonVisible(true);
    verify(mockView).setVisible(true);

    widget.onEditScopeAndMask();

    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onUpdateArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onCancelArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    verify(mockEntityViewScopeEditorModalWidget)
      .configure(
        eq(entityId),
        onUpdateArgumentCaptor.capture(),
        onCancelArgumentCaptor.capture()
      );
    onUpdateArgumentCaptor.getValue().run();
    verify(mockEntityViewScopeEditorModalWidget).setOpen(false);
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnEditScopeAndMaskOnCancel() {
    // configure with an entityview, edit the scope, and save.
    boolean isEditable = true;
    widget.configure(mockBundle, isEditable);

    // The view scope widget does not allow edit of the scope. That occurs in the modal (with the
    // editScopeWidget)
    verify(mockViewScopeWidget)
      .configure(mockReferencesWithoutVersions, false, TableType.file_view);
    verify(mockView).setEditMaskAndScopeButtonVisible(true);
    verify(mockView).setVisible(true);

    widget.onEditScopeAndMask();

    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onUpdateArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onCancelArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    verify(mockEntityViewScopeEditorModalWidget)
      .configure(
        eq(entityId),
        onUpdateArgumentCaptor.capture(),
        onCancelArgumentCaptor.capture()
      );
    onCancelArgumentCaptor.getValue().run();
    verify(mockEntityViewScopeEditorModalWidget).setOpen(false);
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testConfigureUnsupportedViewTypeMask() {
    int unsupportedViewTypeMask = WebConstants.PROJECT | WebConstants.FILE;
    // should be editable, but it isn't because the web client does not support the view type mask
    when(mockEntityView.getViewTypeMask())
      .thenReturn(new Long(unsupportedViewTypeMask));
    boolean isEditable = true;
    widget.configure(mockBundle, isEditable);

    // The view scope widget is configured, but not editable
    verify(mockViewScopeWidget)
      .configure(
        mockReferencesWithoutVersions,
        false,
        new TableType(EntityView.class, unsupportedViewTypeMask)
      );

    // The edit mask + scope modal is visible because the mask cannot be changed in the web client
    verify(mockView).setEditMaskAndScopeButtonVisible(true);
    verify(mockView).setVisible(true);

    // Simulate clicking the edit button
    widget.onEditScopeAndMask();
  }

  @Test
  public void testConfigureView() {
    when(mockEntityView.getType()).thenReturn(ViewType.file_and_table);
    boolean isEditable = true;

    widget.configure(mockBundle, isEditable);
    widget.onEditScopeAndMask();

    // verify new editor is configured
    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onUpdateArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    ArgumentCaptor<
      EntityViewScopeEditorModalProps.Callback
    > onCancelArgumentCaptor = ArgumentCaptor.forClass(
      EntityViewScopeEditorModalProps.Callback.class
    );
    verify(mockEntityViewScopeEditorModalWidget)
      .configure(
        eq(entityId),
        any(EntityViewScopeEditorModalProps.Callback.class),
        any(EntityViewScopeEditorModalProps.Callback.class)
      );
  }

  @Test
  public void testConfigureNotEntityView() {
    boolean isEditable = true;
    when(mockBundle.getEntity()).thenReturn(mockTable);
    widget.configure(mockBundle, isEditable);

    verify(mockView).setVisible(false);
  }

  @Test
  public void testConfigureNotEditable() {
    boolean isEditable = false;
    widget.configure(mockBundle, isEditable);

    verify(mockViewScopeWidget)
      .configure(mockReferencesWithoutVersions, false, TableType.file_view);
    verify(mockView).setEditMaskAndScopeButtonVisible(false);
    verify(mockView).setVisible(true);
  }

  @Test
  public void testAsWidget() {
    widget.asWidget();
    verify(mockView).asWidget();
  }
}
