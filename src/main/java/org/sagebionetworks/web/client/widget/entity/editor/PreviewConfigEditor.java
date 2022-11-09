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

public class PreviewConfigEditor
  implements PreviewConfigView.Presenter, WidgetEditorPresenter {

  private PreviewConfigView view;
  private Map<String, String> descriptor;
  EntityFinderWidget entityFinder;

  @Inject
  public PreviewConfigEditor(
    PreviewConfigView view,
    EntityFinderWidget.Builder entityFinderBuilder
  ) {
    this.view = view;
    view.setPresenter(this);
    view.initView();

    this.entityFinder =
      entityFinderBuilder
        .setModalTitle("Insert File Preview")
        .setHelpMarkdown(
          "Search or Browse Synapse to find Files and insert a preview into this Wiki page"
        )
        .setPromptCopy("Find a File to insert a preview into this Wiki")
        .setConfirmButtonCopy("Insert")
        .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
        .setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
        .setSelectableTypes(EntityFilter.FILE)
        .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED)
        .setSelectedHandler((selected, finder) -> {
          view.setEntityId(selected.getTargetId());
          Long version = selected.getTargetVersionNumber();
          if (version != null) {
            view.setVersion(version.toString());
          } else {
            view.setVersion("");
          }

          finder.hide();
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

    if (descriptor.get(WidgetConstants.WIDGET_ENTITY_ID_KEY) != null) {
      view.setEntityId(descriptor.get(WidgetConstants.WIDGET_ENTITY_ID_KEY));
    }
    if (descriptor.get(WidgetConstants.WIDGET_ENTITY_VERSION_KEY) != null) {
      view.setVersion(
        descriptor.get(WidgetConstants.WIDGET_ENTITY_VERSION_KEY)
      );
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
    String version = view.getVersion();
    if (DisplayUtils.isDefined(version)) {
      Long.parseLong(version);
      descriptor.put(WidgetConstants.WIDGET_ENTITY_VERSION_KEY, version);
    } else {
      descriptor.remove(WidgetConstants.WIDGET_ENTITY_VERSION_KEY);
    }

    descriptor.put(WidgetConstants.WIDGET_ENTITY_ID_KEY, entityId);
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
