package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.jsinterop.EntityFinderProps;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;

public interface EntityFinderWidgetView extends SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void hide();

  void renderComponent(
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
  );

  void clearError();

  void setModalTitle(String modalTitle);

  void setPromptCopy(String promptCopy);

  void setHelpMarkdown(String helpMarkdown);

  void setConfirmButtonCopy(String confirmButtonCopy);

  Widget asWidget();

  /**
   * Presenter interface
   */
  interface Presenter {
    void setSelectedEntity(Reference selected);

    void okClicked();

    void cancelClicked();

    void setSelectedEntities(List<Reference> selected);

    void clearSelectedEntities();

    void renderComponent();
  }
}
