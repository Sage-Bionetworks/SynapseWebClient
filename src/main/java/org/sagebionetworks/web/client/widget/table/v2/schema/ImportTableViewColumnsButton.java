package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;

public class ImportTableViewColumnsButton implements IsWidget {

  public static final String BUTTON_TEXT = "Import columns";
  public Button button;
  public PortalGinInjector ginInjector;
  SynapseJavascriptClient jsClient;
  CallbackP<List<ColumnModel>> callback;
  EntityFinderWidget finder;

  @Inject
  public ImportTableViewColumnsButton(
    Button button,
    final EntityFinderWidget.Builder entityFinderBuilder,
    SynapseJavascriptClient jsClient
  ) {
    this.button = button;
    this.jsClient = jsClient;
    button.setText(BUTTON_TEXT);
    button.setSize(ButtonSize.DEFAULT);
    button.setType(ButtonType.DEFAULT);
    button.setIcon(IconType.ARROW_CIRCLE_O_DOWN);
    this.finder =
      entityFinderBuilder
        .setInitialScope(EntityFinderScope.CURRENT_PROJECT)
        .setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
        .setModalTitle("Find Table")
        .setHelpMarkdown(
          "Search or Browse Synapse to find an existing Table in order to import columns into this Table"
        )
        .setPromptCopy("Find Tables to import columns")
        .setMultiSelect(false)
        .setVisibleTypesInTree(EntityFilter.PROJECT)
        .setSelectableTypes(EntityFilter.ALL_TABLES)
        .setVersionSelection(EntityFinderWidget.VersionSelection.UNTRACKED)
        .setSelectedHandler((selected, entityFinder) ->
          onTableViewSelected(
            selected.getTargetId(),
            selected.getTargetVersionNumber()
          )
        )
        .build();
    button.addStyleName("margin-left-10");
    button.addClickHandler(event -> finder.show());
  }

  public void onTableViewSelected(String entityId, Long versionNumber) {
    // get the column schema
    EntityBundleRequest bundleRequest = new EntityBundleRequest();
    bundleRequest.setIncludeEntity(true);
    bundleRequest.setIncludeTableBundle(true);
    jsClient.getEntityBundleForVersion(
      entityId,
      versionNumber,
      bundleRequest,
      new AsyncCallback<EntityBundle>() {
        @Override
        public void onSuccess(EntityBundle bundle) {
          if (!(bundle.getEntity() instanceof Table)) {
            finder.showError("Please select a Table or View.");
            return;
          }
          finder.hide();
          List<ColumnModel> columns = bundle.getTableBundle().getColumnModels();
          for (ColumnModel cm : columns) {
            cm.setId(null);
          }
          if (callback != null) {
            callback.invoke(columns);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          finder.showError(caught.getMessage());
        }
      }
    );
  }

  public void configure(CallbackP<List<ColumnModel>> callback) {
    this.callback = callback;
  }

  public Widget asWidget() {
    return button.asWidget();
  }
}
