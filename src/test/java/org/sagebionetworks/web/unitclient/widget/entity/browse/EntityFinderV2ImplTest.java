package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderScope;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderV2Impl;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderV2View;

import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class EntityFinderV2ImplTest {

    @Mock
    EntityFinder.SelectedHandler<Reference> singleHandler;

    @Mock
    EntityFinder.SelectedHandler<List<Reference>> multiHandler;

    @Mock
    EntityFinderV2View mockView;

    @Mock
    GlobalApplicationState mockGlobalState;

    @Mock
    SynapseJavascriptClient mockJsClient;

    @Mock
    Synapse mockPlace;

    @Captor
    ArgumentCaptor<AsyncCallback<EntityBundle>> getBundleCaptor;

    @InjectMocks
    EntityFinderV2Impl.Builder builder;

    EntityFinderV2Impl entityFinder;

    @Test
    public void testBuildAndRender() {
        // Verify that all of the arguments passed to the builder end up being used
        boolean multiSelect = true;
        boolean showVersions = true;
        EntityFilter selectableTypes = EntityFilter.FILE;
        EntityFilter visibleTypesInList = EntityFilter.ALL_BUT_LINK;
        EntityFilter visibleTypesInTree = EntityFilter.PROJECT;
        EntityFinderScope scope = EntityFinderScope.ALL_PROJECTS;

        String title = "Custom modal title";
        String prompt = "Custom prompt text";
        String selected = "Custom selected text";
        String confirmCopy = "Custom Action";
        String helpText = "Custom Instructions and Guidance";

        // Finder behavior
        builder.setMultiSelect(multiSelect);
        builder.setShowVersions(showVersions);
        builder.setSelectableTypesInList(selectableTypes);
        builder.setVisibleTypesInList(visibleTypesInList);
        builder.setVisibleTypesInTree(visibleTypesInTree);
        builder.setInitialScope(scope);

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


        // Call under test: show the modal, triggering rendering
        entityFinder.show();

        verify(mockView).renderComponent(scope, null, showVersions, multiSelect, selectableTypes, visibleTypesInList, visibleTypesInTree, selected);
    }

    @Test
    public void testRenderWithCurrentProjectScope() {
        String placeId = "syn123";
        String containerId = "syn456";

        when(mockGlobalState.getCurrentPlace()).thenReturn(mockPlace);
        when(mockPlace.getEntityId()).thenReturn(placeId);

        EntityBundle bundle = new EntityBundle();
        EntityPath path = new EntityPath();
        List<EntityHeader> pathList = new ArrayList<>();
        EntityHeader rootNode = new EntityHeader(); // First entity in path is always the root "syn4489"
        EntityHeader parent = new EntityHeader();
        parent.setId(containerId);
        EntityHeader currentEntity = new EntityHeader();
        currentEntity.setId(placeId);
        pathList.add(rootNode);
        pathList.add(parent);
        pathList.add(currentEntity);
        path.setPath(pathList);
        bundle.setPath(path);

        builder.setInitialScope(EntityFinderScope.CURRENT_PROJECT);
        builder.setInitialContainerId(containerId);
        entityFinder = builder.build();

        // Call under test: showing the modal will trigger finding the current project
        entityFinder.show();

        verify(mockJsClient).getEntityBundle(eq(placeId), any(EntityBundleRequest.class), getBundleCaptor.capture());

        getBundleCaptor.getValue().onSuccess(bundle);


        verify(mockView).renderComponent(eq(EntityFinderScope.CURRENT_PROJECT), eq(containerId), anyBoolean(), anyBoolean(), any(EntityFilter.class), any(EntityFilter.class), any(EntityFilter.class), anyString());
    }

    @Test
    public void testFireSingleEntity() {
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
        entityFinder.setSelectedEntities(Arrays.asList(selectedEntity1, selectedEntity2));

        // Call under test: presenter triggers action
        entityFinder.okClicked();

        verify(multiHandler).onSelected(eq(Arrays.asList(selectedEntity1, selectedEntity2)), eq(entityFinder));
    }
}
