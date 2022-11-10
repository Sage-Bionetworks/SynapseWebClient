package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class SynapseFormConfigEditor
  implements SynapseFormConfigView.Presenter, WidgetEditorPresenter {

  private SynapseFormConfigView view;
  private Map<String, String> descriptor;
  EntityFinderWidget entityFinder;

  @Inject
  public SynapseFormConfigEditor(
    SynapseFormConfigView view,
    EntityFinderWidget.Builder entityFinderBuilder
  ) {
    this.view = view;

    view.setPresenter(this);
    view.initView();

    this.entityFinder = configureEntityFinder(entityFinderBuilder);
  }

  private EntityFinderWidget configureEntityFinder(
    EntityFinderWidget.Builder builder
  ) {
    return builder
      .setModalTitle("Find Table")
      .setHelpMarkdown(
        "Search or Browse Synapse to find a Table and create a form in this Wiki"
      )
      .setPromptCopy("Find Table to create a form")
      .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
      .setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
      .setVisibleTypesInTree(EntityFilter.PROJECT)
      .setSelectableTypes(EntityFilter.TABLE)
      .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED)
      .setSelectedHandler((selected, entityFinder) -> {
        view.setEntityId(selected.getTargetId());
        entityFinder.hide();
      })
      .build();
  }

  @Override
  public void configure(
    WikiPageKey wikiKey,
    Map<String, String> widgetDescriptor,
    DialogCallback dialogCallback
  ) {
    descriptor = widgetDescriptor;

    if (descriptor.get(WidgetConstants.TABLE_ID_KEY) != null) {
      view.setEntityId(descriptor.get(WidgetConstants.TABLE_ID_KEY));
    }
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
    String entityId = view.getEntityId();
    if (!DisplayUtils.isDefined(entityId)) throw new IllegalArgumentException(
      DisplayConstants.INVALID_SELECTION
    );
    descriptor.clear();
    descriptor.put(WidgetConstants.TABLE_ID_KEY, entityId);
  }

  @Override
  public void onEntityFinderButtonClicked() {
    entityFinder.show();
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
}
