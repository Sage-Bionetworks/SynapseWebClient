package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;

public class ProvenanceListWidget
  implements ProvenanceListWidgetView.Presenter, IsWidget {

  ProvenanceListWidgetView view;
  PortalGinInjector ginInjector;
  List<ProvenanceEntry> rows;
  EntityFinderWidget.Builder entityFinderBuilder;
  EntityFinderWidget entityFinder;
  ProvenanceURLDialogWidget urlDialog;
  ProvenanceType provenanceType;

  @Inject
  public ProvenanceListWidget(
    final ProvenanceListWidgetView view,
    final PortalGinInjector ginInjector,
    final EntityFinderWidget.Builder entityFinderBuilder
  ) {
    this.view = view;
    this.ginInjector = ginInjector;
    rows = new LinkedList<ProvenanceEntry>();
    this.view.setPresenter(this);
    this.entityFinderBuilder = entityFinderBuilder;
    this.entityFinder =
      entityFinderBuilder
        .setModalTitle("Find in Synapse")
        .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
        .setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
        .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED)
        .setMultiSelect(true)
        .setSelectableTypes(EntityFilter.ALL)
        .setSelectedMultiHandler((refs, finder) -> {
          for (Reference ref : refs) {
            if (ref.getTargetId() != null) {
              final EntityRefProvEntryView newEntry = ginInjector.getEntityRefEntry();
              rows.add(newEntry);
              String targetId = ref.getTargetId();
              Long version = ref.getTargetVersionNumber();
              newEntry.configure(
                targetId,
                version != null ? version.toString() : "Current"
              );
              newEntry.setAnchorTarget(
                DisplayUtils.getSynapseHistoryToken(targetId, version)
              );
              newEntry.setRemoveCallback(() -> {
                rows.remove(newEntry);
                view.removeRow(newEntry);
              });
              view.addRow(newEntry);
            }
            entityFinder.hide();
          }
        })
        .build();
  }

  @Override
  public void configure(
    List<ProvenanceEntry> provEntries,
    ProvenanceType provenanceType
  ) {
    this.provenanceType = provenanceType;
    rows = provEntries;
    for (final ProvenanceEntry entry : rows) {
      view.addRow(entry);
      entry.setRemoveCallback(
        new Callback() {
          @Override
          public void invoke() {
            rows.remove(entry);
            view.removeRow(entry);
          }
        }
      );
    }

    switch (this.provenanceType) {
      case USED:
        this.entityFinderBuilder.setHelpMarkdown(
            "Search or Browse Synapse to find items that were used to generate this entity"
          )
          .setPromptCopy(
            "Find items representing objects used to create this entity"
          );
        break;
      case EXECUTED:
        this.entityFinderBuilder.setHelpMarkdown(
            "Search or Browse Synapse to find items that represent a process or executable unit that generated this entity"
          )
          .setPromptCopy(
            "Find items representing processes that were executed to generate this entity"
          );
        break;
    }

    this.entityFinder = this.entityFinderBuilder.build();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void addEntityRow() {
    entityFinder.clearState();
    entityFinder.show();
  }

  @Override
  public void addURLRow() {
    urlDialog.configure(
      new Callback() {
        @Override
        public void invoke() {
          final URLProvEntryView newEntry = ginInjector.getURLEntry();
          rows.add(newEntry);
          String name = urlDialog.getURLName();
          String address = urlDialog.getURLAddress();
          if (name.trim().isEmpty()) {
            name = address;
          }
          newEntry.configure(name, address);
          newEntry.setAnchorTarget(address);
          newEntry.setRemoveCallback(
            new Callback() {
              @Override
              public void invoke() {
                rows.remove(newEntry);
                view.removeRow(newEntry);
              }
            }
          );
          view.addRow(newEntry);
          urlDialog.hide();
        }
      }
    );
    urlDialog.show();
  }

  public void clear() {
    rows.clear();
    view.clear();
  }

  public List<ProvenanceEntry> getEntries() {
    return rows;
  }

  public void setURLDialog(final ProvenanceURLDialogWidget urlDialog) {
    this.urlDialog = urlDialog;
  }
}
