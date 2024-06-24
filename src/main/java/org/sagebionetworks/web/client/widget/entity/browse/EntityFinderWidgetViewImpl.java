package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityFinderProps;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsni.ReferenceJSNIObject;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class EntityFinderWidgetViewImpl implements EntityFinderWidgetView {

  public interface Binder
    extends UiBinder<Widget, EntityFinderWidgetViewImpl> {}

  private Presenter presenter;

  private SynapseJSNIUtils jsniUtils;
  private SynapseAlert synAlert;
  private SynapseReactClientFullContextPropsProvider contextPropsProvider;

  // the modal dialog
  private Modal modal;

  @UiField
  ReactComponent entityFinderContainer;

  @UiField
  Heading modalTitle;

  @UiField
  Paragraph promptCopy;

  @UiField
  Button okButton;

  @UiField
  Button cancelButton;

  @UiField
  HelpWidget helpWidget;

  @UiField
  SimplePanel synAlertPanel;

  @Inject
  public EntityFinderWidgetViewImpl(
    Binder uiBinder,
    SynapseJSNIUtils jsniUtils,
    SynapseAlert synAlert,
    final SynapseReactClientFullContextPropsProvider propsProvider,
    final PopupUtilsView popupUtils
  ) {
    this.modal = (Modal) uiBinder.createAndBindUi(this);

    this.jsniUtils = jsniUtils;
    this.synAlert = synAlert;
    this.contextPropsProvider = propsProvider;

    synAlertPanel.setWidget(synAlert);

    // Initially, nothing is selected, so we disable the confirm button
    okButton.setEnabled(false);
    okButton.addClickHandler(event -> presenter.okClicked());
    okButton.addDomHandler(
      DisplayUtils.getPreventTabHandler(okButton),
      KeyDownEvent.getType()
    );
    cancelButton.addClickHandler(event -> presenter.cancelClicked());
  }

  private String getInitialContainerAsString(
    EntityFinderWidget.InitialContainer initialContainer,
    String projectId,
    String containerId
  ) {
    switch (initialContainer) {
      case PROJECT:
        return projectId;
      case PARENT:
        return containerId;
      case SCOPE:
        return "root";
      case NONE:
      default:
        return null;
    }
  }

  @Override
  public void renderComponent(
    EntityFinderScope initialScope,
    EntityFinderWidget.InitialContainer initialContainer,
    String projectId,
    String initialContainerId,
    EntityFinderWidget.VersionSelection versionSelection,
    boolean multiSelect,
    EntityFilter selectableEntityTypes,
    EntityFilter visibleTypesInList,
    EntityFilter visibleTypesInTree,
    EntityFinderProps.SelectedCopyHandler selectedCopy,
    boolean treeOnly
  ) {
    entityFinderContainer.clear();

    EntityFinderProps.OnSelectCallback onSelected = result -> {
      List<Reference> selected = Arrays
        .stream(result)
        .map(ReferenceJSNIObject::getJavaObject)
        .collect(Collectors.toList());
      okButton.setEnabled(selected.size() > 0);
      if (multiSelect) {
        presenter.setSelectedEntities(selected);
      } else {
        if (selected.size() > 0) {
          presenter.setSelectedEntity(selected.get(0));
        } else {
          presenter.clearSelectedEntities();
        }
      }
    };

    EntityFinderProps props = EntityFinderProps.create(
      onSelected,
      multiSelect,
      versionSelection.name(),
      initialScope,
      projectId,
      getInitialContainerAsString(
        initialContainer,
        projectId,
        initialContainerId
      ),
      visibleTypesInList.getEntityQueryValues(),
      visibleTypesInTree.getEntityQueryValues(),
      selectableEntityTypes.getEntityQueryValues(),
      selectedCopy,
      treeOnly
    );

    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityFinder,
      props,
      contextPropsProvider.getJsInteropContextProps()
    );
    entityFinderContainer.render(component);
    modal.show();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void showErrorMessage(String message) {
    synAlert.showError(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {
    synAlert.clear();
    entityFinderContainer.clear();
    presenter.clearSelectedEntities();
    okButton.setEnabled(false);
  }

  @Override
  public void clearError() {
    synAlert.clear();
  }

  @Override
  public void hide() {
    modal.hide();
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public void setModalTitle(String modalTitle) {
    this.modalTitle.setText(modalTitle);
  }

  @Override
  public void setPromptCopy(String promptCopy) {
    this.promptCopy.setHTML(promptCopy);
  }

  @Override
  public void setHelpMarkdown(String helpMarkdown) {
    this.helpWidget.setHelpMarkdown(helpMarkdown);
  }

  @Override
  public void setConfirmButtonCopy(String confirmButtonCopy) {
    this.okButton.setText(confirmButtonCopy);
  }
}
