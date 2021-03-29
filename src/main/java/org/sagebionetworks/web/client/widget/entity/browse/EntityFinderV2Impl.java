package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.ALL;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityFinderV2Impl implements EntityFinder, EntityFinderV2View.Presenter, IsWidget {
    private EntityFinderV2View view;
    private List<Reference> selectedEntities;
    GlobalApplicationState globalApplicationState;
    private SynapseJavascriptClient jsClient;

    private boolean multiSelect;
    private String initialContainerId;
    private EntityFinderScope initialScope;

    private boolean showVersions;
    private EntityFilter visibleTypesInList;
    private EntityFilter selectableTypes;
    private EntityFilter visibleTypesInTree;
    private SelectedHandler<Reference> selectedHandler;
    private SelectedHandler<List<Reference>> selectedMultiHandler;


    private String modalTitle;
    private String promptCopy;
    private String selectedCopy;
    private String confirmButtonCopy;

    @Inject
    public EntityFinderV2Impl(EntityFinderV2View view, GlobalApplicationState globalApplicationState, SynapseJavascriptClient jsClient) {
        this.view = view;
        this.globalApplicationState = globalApplicationState;
        this.jsClient = jsClient;
        this.selectedEntities = new ArrayList<>();
        view.setPresenter(this);
    }

    private EntityFinderV2Impl(Builder builder) {
        this.selectedEntities = new ArrayList<>();

        // Dependencies injected into the builder
        this.view = builder.view;
        this.globalApplicationState = builder.globalApplicationState;
        this.jsClient = builder.jsClient;

        this.view.setPresenter(this);

        // Configuration
        if (builder.modalTitle != null) {
            this.view.setModalTitle(builder.modalTitle);
        }
        if (builder.promptCopy != null) {
            this.view.setPromptCopy(builder.promptCopy);
        }
        if (builder.helpMarkdown != null) {
            this.view.setHelpMarkdown(builder.helpMarkdown);
        }
        if (builder.confirmButtonCopy != null) {
            this.view.setConfirmButtonCopy(builder.confirmButtonCopy);
        }

        modalTitle = builder.modalTitle;
        promptCopy = builder.promptCopy;
        selectedCopy = builder.selectedCopy;
        modalTitle = builder.modalTitle;
        multiSelect = builder.multiSelect;
        initialContainerId = builder.initialContainerId;
        selectedHandler = builder.selectedHandler;
        selectedMultiHandler = builder.selectedMultiHandler;
        selectableTypes = builder.selectableTypesInList;
        visibleTypesInList = builder.visibleTypesInList;
        showVersions = builder.showVersions;
        initialScope = builder.initialScope;
        visibleTypesInTree = builder.visibleTypesInTree;
        renderComponent();
    }

    public static class Builder implements EntityFinder.Builder {
        private EntityFinderV2View view;
        GlobalApplicationState globalApplicationState;
        private SynapseJavascriptClient jsClient;

        private SelectedHandler<Reference> selectedHandler = (selected, finder) -> {
        };
        private SelectedHandler<List<Reference>> selectedMultiHandler = (selected, finder) -> {
        };

        private boolean showVersions = false;
        private EntityFilter visibleTypesInList = ALL;
        private EntityFilter selectableTypesInList = ALL;
        private EntityFilter visibleTypesInTree = CONTAINER;

        private boolean multiSelect = false;
        private String initialContainerId = null;

        private EntityFinderScope initialScope = EntityFinderScope.CREATED_BY_ME;
        private String modalTitle = "Find in Synapse";
        private String promptCopy = "";
        private String selectedCopy = "Selected";
        private String confirmButtonCopy = "Select";
        private String helpMarkdown = "Finding items in Synapse can be done by either “browsing”, “searching,” or directly entering the Synapse ID.&#10;Alternatively, navigate to the desired location in the current project, favorite projects or projects you own.";

        @Inject
        public Builder(EntityFinderV2View view, GlobalApplicationState globalApplicationState, SynapseJavascriptClient jsClient) {
            this.view = view;
            this.globalApplicationState = globalApplicationState;
            this.jsClient = jsClient;
        }

        @Override
        public EntityFinderV2Impl build() {
            return new EntityFinderV2Impl(this);
        }

        @Override
        public EntityFinder.Builder setMultiSelect(boolean multiSelect) {
            this.multiSelect = multiSelect;
            return this;
        }

        @Override
        public EntityFinder.Builder setInitialContainerId(String initialContainerId) {
            this.initialContainerId = initialContainerId;
            return this;
        }

        @Override
        public EntityFinder.Builder setSelectedHandler(SelectedHandler<Reference> handler) {
            if (multiSelect) {
                throw new IllegalArgumentException("Attempted to set handler for single selection in Entity Finder when multiselect is true");
            }
            this.selectedHandler = handler;
            return this;
        }

        @Override
        public EntityFinder.Builder setSelectedMultiHandler(SelectedHandler<List<Reference>> handler) {
            if (!multiSelect) {
                throw new IllegalArgumentException("Attempted to set handler for single selection in Entity Finder when multiselect is true");
            }
            this.selectedMultiHandler = handler;
            return this;
        }

        @Override
        public EntityFinder.Builder setSelectableTypesInList(EntityFilter selectableFilter) {
            this.selectableTypesInList = selectableFilter;
            return this;
        }

        @Override
        public EntityFinder.Builder setVisibleTypesInList(EntityFilter visibleFilter) {
            this.visibleTypesInList = visibleFilter;
            return this;
        }

        @Override
        public EntityFinder.Builder setVisibleTypesInTree(EntityFilter visibleTypesInTree) {
            this.visibleTypesInTree = visibleTypesInTree;
            return this;
        }

        @Override
        public EntityFinder.Builder setShowVersions(boolean showVersions) {
            this.showVersions = showVersions;
            return this;
        }

        @Override
        public EntityFinder.Builder setModalTitle(String modalTitle) {
            this.modalTitle = modalTitle;
            return this;
        }

        @Override
        public EntityFinder.Builder setPromptCopy(String promptCopy) {
            this.promptCopy = promptCopy;
            return this;
        }

        @Override
        public EntityFinder.Builder setHelpMarkdown(String helpMarkdown) {
            this.helpMarkdown = helpMarkdown;
            return this;
        }

        @Override
        public EntityFinder.Builder setSelectedCopy(String selectedCopy) {
            this.selectedCopy = selectedCopy;
            return this;
        }

        @Override
        public EntityFinder.Builder setInitialScope(EntityFinderScope initialScope) {
            this.initialScope = initialScope;
            return this;
        }

        @Override
        public EntityFinder.Builder setConfirmButtonCopy(String confirmButtonCopy) {
            this.confirmButtonCopy = confirmButtonCopy;
            return this;
        }
    }

    @Override
    public void setSelectedEntity(Reference selected) {
        view.clearError();
        selectedEntities.clear();
        selectedEntities.add(selected);
    }

    @Override
    public void setSelectedEntities(List<Reference> selected) {
        view.clearError();
        selectedEntities.clear();
        selectedEntities.addAll(selected);
    }

    @Override
    public void clearSelectedEntities() {
        view.clearError();
        selectedEntities.clear();
    }

    @Override
    public void okClicked() {
        view.clearError();
        // check for valid selection
        if (selectedEntities == null || selectedEntities.isEmpty()) {
            view.setErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
        } else {
            fireEntitiesSelected();
        }
    }

    @Override
    public void renderComponent() {
        if (initialScope.equals(EntityFinderScope.CURRENT_PROJECT)) {
            // We have to determine the current project, so we get the entity path
            Place currentPlace = globalApplicationState.getCurrentPlace();
            if (currentPlace instanceof Synapse) {
                String entityId = ((Synapse) currentPlace).getEntityId();
                EntityBundleRequest bundleRequest = new EntityBundleRequest();
                bundleRequest.setIncludeEntityPath(true);
                jsClient.getEntityBundle(entityId, bundleRequest, new AsyncCallback<EntityBundle>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(EntityBundle result) {
                        EntityPath path = result.getPath();
                        List<EntityHeader> pathHeaders = path.getPath();
                        if (pathHeaders.size() > 2) {
                            initialContainerId = pathHeaders.get(pathHeaders.size() - 2).getId();
                        } else {
                            initialContainerId = pathHeaders.get(pathHeaders.size() - 1).getId();
                        }
                        view.renderComponent(initialScope, initialContainerId, showVersions, multiSelect, selectableTypes, visibleTypesInList, visibleTypesInTree, selectedCopy);
                    }
                });
            } else {
                throw new IllegalStateException("While initializing Entity Finder, attempted to get entity bundle when not in a Synapse place");
            }
        } else {
            view.renderComponent(initialScope, null, showVersions, multiSelect, selectableTypes, visibleTypesInList, visibleTypesInTree, selectedCopy);
        }
    }

    private void fireEntitiesSelected() {
        if (!multiSelect) {
            selectedHandler.onSelected(selectedEntities.get(0), this);
        } else {
            selectedMultiHandler.onSelected(selectedEntities, this);
        }
    }

    @Override
    public void show() {
        view.clear();
        view.show();
    }

    @Override
    public void hide() {
        view.hide();
    }

    @Override
    public void clearState() {
        view.clear();
    }

    @Override
    public void showError(String error) {
        view.showErrorMessage(error);
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
