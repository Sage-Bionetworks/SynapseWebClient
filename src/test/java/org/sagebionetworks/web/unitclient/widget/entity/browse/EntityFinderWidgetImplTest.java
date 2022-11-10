package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.exceptions.WebClientConfigurationException;
import org.sagebionetworks.web.client.jsinterop.EntityFinderProps;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

@RunWith(MockitoJUnitRunner.class)
public class EntityFinderWidgetImplTest {

  @Mock
  EntityFinderWidget.SelectedHandler<Reference> singleHandler;

  @Mock
  EntityFinderWidget.SelectedHandler<List<Reference>> multiHandler;

  @Mock
  EntityFinderWidgetView mockView;

  @Mock
  GlobalApplicationState mockGlobalState;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  Synapse mockPlace;

  @Captor
  ArgumentCaptor<AsyncCallback<EntityBundle>> getBundleCaptor;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  PortalGinInjector mockGinInjector;

  @InjectMocks
  EntityFinderWidgetImpl.Builder builder;

  EntityFinderWidgetImpl entityFinder;

  @Before
  public void setUp() {
    when(mockGinInjector.getEntityFinderWidgetView()).thenReturn(mockView);
    when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    when(mockGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalState);
    when(mockGinInjector.getPopupUtils()).thenReturn(mockPopupUtils);
  }

  @Test
  public void testBuildAndRender() {
    // Verify that all of the arguments passed to the builder end up being used
    boolean multiSelect = true;
    EntityFinderWidget.VersionSelection versionSelection =
      EntityFinderWidget.VersionSelection.REQUIRED;
    EntityFilter selectableTypes = EntityFilter.FILE;
    EntityFilter visibleTypesInList = EntityFilter.ALL;
    EntityFilter visibleTypesInTree = EntityFilter.PROJECT;
    EntityFinderScope scope = EntityFinderScope.ALL_PROJECTS;
    EntityFinderWidget.InitialContainer initialContainer =
      EntityFinderWidget.InitialContainer.NONE;
    boolean treeOnly = true;
    boolean mustSelectVersionNumber = false;

    String title = "Custom modal title";
    String prompt = "Custom prompt text";
    EntityFinderProps.SelectedCopyHandler selected = count ->
      "Custom selected text";
    String confirmCopy = "Custom Action";
    String helpText = "Custom Instructions and Guidance";

    // Finder behavior
    builder.setMultiSelect(multiSelect);
    builder.setVersionSelection(versionSelection);
    builder.setSelectableTypes(selectableTypes);
    builder.setVisibleTypesInList(visibleTypesInList);
    builder.setVisibleTypesInTree(visibleTypesInTree);
    builder.setInitialScope(scope);
    builder.setTreeOnly(treeOnly);

    // Copy text
    builder.setModalTitle(title);
    builder.setPromptCopy(prompt);
    builder.setSelectedCopy(selected);
    builder.setConfirmButtonCopy(confirmCopy);
    builder.setHelpMarkdown(helpText);

    // Call under test: Build the entity finder
    entityFinder = builder.build();

    verify(mockView).setPresenter(entityFinder);
    verify(mockView).setModalTitle(title);
    verify(mockView).setPromptCopy(prompt);
    verify(mockView).setConfirmButtonCopy(confirmCopy);

    verify(mockSynAlert, never()).handleException(any());

    // Call under test: show the modal, triggering rendering
    entityFinder.show();

    verify(mockView)
      .renderComponent(
        scope,
        EntityFinderWidget.InitialContainer.NONE,
        null,
        null,
        versionSelection,
        multiSelect,
        selectableTypes,
        visibleTypesInList,
        visibleTypesInTree,
        selected,
        treeOnly
      );
  }

  @Test
  public void testRenderWithCurrentProjectScope() {
    String placeId = "syn123";
    String projectId = "syn456";
    String containerId = "syn789";

    when(mockGlobalState.getCurrentPlace()).thenReturn(mockPlace);
    when(mockPlace.getEntityId()).thenReturn(placeId);

    EntityBundle bundle = new EntityBundle();
    EntityPath path = new EntityPath();
    List<EntityHeader> pathList = new ArrayList<>();
    EntityHeader rootNode = new EntityHeader(); // First entity in path is always the root "syn4489"

    EntityHeader project = new EntityHeader();
    project.setId(projectId);

    EntityHeader parent = new EntityHeader();
    parent.setId(containerId);

    EntityHeader currentEntity = new EntityHeader();
    currentEntity.setId(placeId);

    pathList.add(rootNode);
    pathList.add(project);
    pathList.add(parent);
    pathList.add(currentEntity);
    path.setPath(pathList);
    bundle.setPath(path);

    builder.setSelectableTypes(EntityFilter.ALL);
    builder.setInitialScope(EntityFinderScope.CURRENT_PROJECT);
    builder.setInitialContainer(EntityFinderWidget.InitialContainer.PARENT);
    entityFinder = builder.build();

    // Call under test: showing the modal will trigger finding the current project
    entityFinder.show();

    verify(mockJsClient)
      .getEntityBundle(
        eq(placeId),
        any(EntityBundleRequest.class),
        getBundleCaptor.capture()
      );

    getBundleCaptor.getValue().onSuccess(bundle);

    verify(mockView)
      .renderComponent(
        eq(EntityFinderScope.CURRENT_PROJECT),
        eq(EntityFinderWidget.InitialContainer.PARENT),
        eq(projectId),
        eq(containerId),
        any(EntityFinderWidget.VersionSelection.class),
        anyBoolean(),
        any(EntityFilter.class),
        any(EntityFilter.class),
        any(EntityFilter.class),
        any(),
        anyBoolean()
      );
  }

  @Test
  public void testFireSingleEntity() {
    builder.setSelectableTypes(EntityFilter.ALL);
    builder.setMultiSelect(false);
    builder.setSelectedHandler(singleHandler);
    entityFinder = builder.build();

    String entityId = "syn123";
    Long entityVersion = 5L;
    Reference selectedEntity = new Reference();
    selectedEntity.setTargetId(entityId);
    selectedEntity.setTargetVersionNumber(entityVersion);

    // Select an entity
    entityFinder.setSelectedEntity(selectedEntity);

    // Call under test: presenter triggers action
    entityFinder.okClicked();

    verify(singleHandler).onSelected(selectedEntity, entityFinder);
  }

  @Test
  public void testFireMultipleEntities() {
    builder.setSelectableTypes(EntityFilter.ALL);
    builder.setMultiSelect(true);
    builder.setSelectedMultiHandler(multiHandler);
    entityFinder = builder.build();

    String entityId1 = "syn123";
    Long entityVersion1 = 5L;
    Reference selectedEntity1 = new Reference();
    selectedEntity1.setTargetId(entityId1);
    selectedEntity1.setTargetVersionNumber(entityVersion1);

    String entityId2 = "syn456";
    Long entityVersion2 = null;
    Reference selectedEntity2 = new Reference();
    selectedEntity2.setTargetId(entityId2);
    selectedEntity2.setTargetVersionNumber(entityVersion2);

    // Select a couple of entities
    entityFinder.setSelectedEntities(
      Arrays.asList(selectedEntity1, selectedEntity2)
    );

    // Call under test: presenter triggers action
    entityFinder.okClicked();

    verify(multiHandler)
      .onSelected(
        eq(Arrays.asList(selectedEntity1, selectedEntity2)),
        eq(entityFinder)
      );
  }

  @Test
  public void testMisconfigurationTriggersAlert_NoSelectableTypes() {
    builder
      .setSelectableTypes(null) // !
      .setMultiSelect(false)
      .setSelectedHandler((selected, finder) -> {});

    // Method under test
    builder.build();

    verify(mockSynAlert)
      .handleException(any(WebClientConfigurationException.class));
  }

  @Test
  public void testMisconfigurationTriggersAlert_NoSingleHandler() {
    builder
      .setSelectableTypes(EntityFilter.ALL)
      .setMultiSelect(false)
      .setSelectedHandler(null); // !

    // Method under test
    builder.build();

    verify(mockSynAlert)
      .handleException(any(WebClientConfigurationException.class));
  }

  @Test
  public void testMisconfigurationTriggersAlert_NoMultiHandler() {
    builder
      .setSelectableTypes(EntityFilter.ALL)
      .setMultiSelect(true)
      .setSelectedMultiHandler(null); // !

    // Method under test
    builder.build();

    verify(mockSynAlert)
      .handleException(any(WebClientConfigurationException.class));
  }

  @Test
  public void testShowWarningOnCancelWithSelection() {
    builder.setSelectableTypes(EntityFilter.ALL);
    builder.setMultiSelect(true);
    builder.setSelectedMultiHandler(multiHandler);
    entityFinder = builder.build();

    String entityId1 = "syn123";
    Long entityVersion1 = 5L;
    Reference selectedEntity1 = new Reference();
    selectedEntity1.setTargetId(entityId1);
    selectedEntity1.setTargetVersionNumber(entityVersion1);

    String entityId2 = "syn456";
    Long entityVersion2 = null;
    Reference selectedEntity2 = new Reference();
    selectedEntity2.setTargetId(entityId2);
    selectedEntity2.setTargetVersionNumber(entityVersion2);

    // Select a couple of entities
    entityFinder.setSelectedEntities(
      Arrays.asList(selectedEntity1, selectedEntity2)
    );

    // Call under test: presenter triggers action
    entityFinder.cancelClicked();

    // The confirmation popup should be shown
    verify(mockPopupUtils)
      .showConfirmDialog(
        eq("Unsaved Changes"),
        anyString(),
        callbackCaptor.capture()
      );

    // Hide should not have been called
    verify(mockView, never()).hide();

    // Simulate confirming the warning dialog
    callbackCaptor.getValue().invoke();

    verify(mockView).hide();
  }
}
