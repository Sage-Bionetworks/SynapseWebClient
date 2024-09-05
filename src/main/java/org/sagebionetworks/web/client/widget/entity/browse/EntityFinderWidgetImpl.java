package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.ALL;
import static org.sagebionetworks.web.client.widget.entity.browse.EntityFilter.CONTAINER;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.exceptions.WebClientConfigurationException;
import org.sagebionetworks.web.client.jsinterop.EntityFinderProps;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class EntityFinderWidgetImpl
  implements EntityFinderWidget, EntityFinderWidgetView.Presenter, IsWidget {

  private EntityFinderWidgetView view;
  private List<Reference> selectedEntities;
  GlobalApplicationState globalApplicationState;
  private SynapseJavascriptClient jsClient;
  private SynapseAlert synAlert;
  private PopupUtilsView popupUtils;

  private boolean multiSelect;
  private EntityFinderWidget.InitialContainer initialContainer;
  private EntityFinderScope initialScope;

  private VersionSelection versionSelection;
  private EntityFilter visibleTypesInList;
  private EntityFilter selectableTypes;
  private EntityFilter visibleTypesInTree;
  private boolean treeOnly;
  private EntityFinderWidget.SelectedHandler<Reference> selectedHandler;
  private EntityFinderWidget.SelectedHandler<
    List<Reference>
  > selectedMultiHandler;

  private String modalTitle;
  private String promptCopy;
  private EntityFinderProps.SelectedCopyHandler selectedCopy;
  private String confirmButtonCopy;

  @Inject
  public EntityFinderWidgetImpl(
    final EntityFinderWidgetView view,
    final GlobalApplicationState globalApplicationState,
    final SynapseJavascriptClient jsClient,
    final SynapseAlert synAlert,
    final PopupUtilsView popupUtils
  ) {
    this.view = view;
    this.globalApplicationState = globalApplicationState;
    this.jsClient = jsClient;
    this.synAlert = synAlert;
    this.popupUtils = popupUtils;
    this.selectedEntities = new ArrayList<>();
    view.setPresenter(this);
  }

  private EntityFinderWidgetImpl(
    final Builder builder,
    final PortalGinInjector ginInjector
  ) {
    this.selectedEntities = new ArrayList<>();

    // Since we aren't using constructor injection, get dependencies from a ginInjector passed along by the builder
    this.view = ginInjector.getEntityFinderWidgetView();
    this.globalApplicationState = ginInjector.getGlobalApplicationState();
    this.jsClient = ginInjector.getSynapseJavascriptClient();
    this.synAlert = ginInjector.getSynapseAlertWidget();
    this.popupUtils = ginInjector.getPopupUtils();

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

    treeOnly = builder.treeOnly;
    modalTitle = builder.modalTitle;
    promptCopy = builder.promptCopy;
    selectedCopy = builder.selectedCopy;
    modalTitle = builder.modalTitle;
    multiSelect = builder.multiSelect;
    initialContainer = builder.initialContainer;
    selectedHandler = builder.selectedHandler;
    selectedMultiHandler = builder.selectedMultiHandler;
    selectableTypes = builder.selectableTypes;
    visibleTypesInList = builder.visibleTypesInList;
    versionSelection = builder.versionSelection;
    initialScope = builder.initialScope;
    visibleTypesInTree = builder.visibleTypesInTree;

    // Validation
    try {
      if (selectableTypes == null) {
        throw new WebClientConfigurationException(
          "Selectable types must be explicitly specified in the Entity Finder Builder."
        );
      }
      if (!multiSelect && selectedHandler == null) {
        throw new WebClientConfigurationException(
          "No selected handler set in Entity Finder with multiSelect=false"
        );
      }
      if (multiSelect && selectedMultiHandler == null) {
        throw new WebClientConfigurationException(
          "No selected multi-handler set in Entity Finder with multiSelect=false when multiselect is true"
        );
      }
    } catch (WebClientConfigurationException e) {
      synAlert.handleException(e);
    }
  }

  public static class Builder implements EntityFinderWidget.Builder {

    private PortalGinInjector ginInjector;

    private EntityFinderWidget.SelectedHandler<Reference> selectedHandler = (
      selected,
      finder
    ) -> {};
    private EntityFinderWidget.SelectedHandler<
      List<Reference>
    > selectedMultiHandler = (selected, finder) -> {};

    private VersionSelection versionSelection = VersionSelection.TRACKED;
    private EntityFilter visibleTypesInList = ALL;
    private EntityFilter selectableTypes = null;
    private EntityFilter visibleTypesInTree = CONTAINER;
    boolean treeOnly = false;
    boolean mustSelectVersionNumber = false;

    private boolean multiSelect = false;
    private EntityFinderWidget.InitialContainer initialContainer =
      EntityFinderWidget.InitialContainer.NONE;

    private EntityFinderScope initialScope = EntityFinderScope.CREATED_BY_ME;
    private String modalTitle = "Find in Synapse";
    private String promptCopy = "";
    private EntityFinderProps.SelectedCopyHandler selectedCopy = count ->
      "Selected";
    private String confirmButtonCopy = "Select";
    private String helpMarkdown =
      "Finding items in Synapse can be done by either “browsing”, “searching,” or directly entering the Synapse ID.&#10;Alternatively, navigate to the desired location in the current project, favorite projects or projects you own.";

    @Inject
    public Builder(final PortalGinInjector ginInjector) {
      this.ginInjector = ginInjector;
    }

    @Override
    public EntityFinderWidgetImpl build() {
      return new EntityFinderWidgetImpl(this, ginInjector);
    }

    @Override
    public EntityFinderWidget.Builder setMultiSelect(boolean multiSelect) {
      this.multiSelect = multiSelect;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setInitialContainer(
      EntityFinderWidget.InitialContainer initialContainer
    ) {
      this.initialContainer = initialContainer;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setSelectedHandler(
      EntityFinderWidget.SelectedHandler<Reference> handler
    ) {
      this.selectedHandler = handler;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setSelectedMultiHandler(
      EntityFinderWidget.SelectedHandler<List<Reference>> handler
    ) {
      this.selectedMultiHandler = handler;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setSelectableTypes(
      EntityFilter selectableFilter
    ) {
      this.selectableTypes = selectableFilter;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setVisibleTypesInList(
      EntityFilter visibleFilter
    ) {
      this.visibleTypesInList = visibleFilter;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setVisibleTypesInTree(
      EntityFilter visibleTypesInTree
    ) {
      this.visibleTypesInTree = visibleTypesInTree;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setVersionSelection(
      VersionSelection versionSelection
    ) {
      this.versionSelection = versionSelection;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setModalTitle(String modalTitle) {
      this.modalTitle = modalTitle;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setPromptCopy(String promptCopy) {
      this.promptCopy = promptCopy;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setHelpMarkdown(String helpMarkdown) {
      this.helpMarkdown = helpMarkdown;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setSelectedCopy(
      EntityFinderProps.SelectedCopyHandler selectedCopy
    ) {
      this.selectedCopy = selectedCopy;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setInitialScope(
      EntityFinderScope initialScope
    ) {
      this.initialScope = initialScope;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setConfirmButtonCopy(
      String confirmButtonCopy
    ) {
      this.confirmButtonCopy = confirmButtonCopy;
      return this;
    }

    @Override
    public EntityFinderWidget.Builder setTreeOnly(boolean treeOnly) {
      this.treeOnly = treeOnly;
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

  private boolean hasUnsavedChanges() {
    return selectedEntities.size() > 0;
  }

  @Override
  public void okClicked() {
    view.clearError();
    // check for valid selection
    if (selectedEntities == null || selectedEntities.isEmpty()) {
      view.showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
    } else {
      fireEntitiesSelected();
    }
  }

  @Override
  public void cancelClicked() {
    if (hasUnsavedChanges()) {
      popupUtils.showConfirmDialog(
        "Unsaved Changes",
        "Any unsaved changes will be lost. Are you sure you want to close the finder?",
        () -> this.hide()
      );
    } else {
      this.hide();
    }
  }

  @Override
  public void renderComponent() {
    // We have to determine the current project, so we get the entity path
    Place currentPlace = globalApplicationState.getCurrentPlace();
    if (currentPlace instanceof Synapse) {
      String entityId = ((Synapse) currentPlace).getEntityId();
      EntityBundleRequest bundleRequest = new EntityBundleRequest();
      bundleRequest.setIncludeEntityPath(true);
      jsClient.getEntityBundle(
        entityId,
        bundleRequest,
        new AsyncCallback<EntityBundle>() {
          @Override
          public void onFailure(Throwable caught) {
            showError(caught.getMessage());
          }

          @Override
          public void onSuccess(EntityBundle result) {
            EntityPath path = result.getPath();
            List<EntityHeader> pathHeaders = path.getPath();
            String projectId;
            String parentId;
            projectId = pathHeaders.get(1).getId();
            if (pathHeaders.size() > 2) { // in other words, if the current entity is not a project, get the parent
              parentId = pathHeaders.get(pathHeaders.size() - 2).getId();
            } else { // otherwise get the project itself
              parentId = projectId;
            }
            view.renderComponent(
              initialScope,
              initialContainer,
              projectId,
              parentId,
              versionSelection,
              multiSelect,
              selectableTypes,
              visibleTypesInList,
              visibleTypesInTree,
              selectedCopy,
              treeOnly
            );
          }
        }
      );
    } else {
      view.renderComponent(
        initialScope,
        initialContainer,
        null,
        null,
        versionSelection,
        multiSelect,
        selectableTypes,
        visibleTypesInList,
        visibleTypesInTree,
        selectedCopy,
        treeOnly
      );
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
    renderComponent();
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
