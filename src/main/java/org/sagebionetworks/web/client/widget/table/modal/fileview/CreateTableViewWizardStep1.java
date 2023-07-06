package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditor;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * First page of table/view creation wizard. Ask for the name and scope, then create the entity.
 *
 * @author Jay
 *
 */
public class CreateTableViewWizardStep1
  implements ModalPage, CreateTableViewWizardStep1View.Presenter {

  public static final String EMPTY_SCOPE_MESSAGE =
    "Please define the scope for this view.";
  private static final String NEXT = "Next";
  private static final String FINISH = "Finish";
  public static final String NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER =
    "Name must include at least one character.";

  CreateTableViewWizardStep1View view;
  SynapseJavascriptClient jsClient;
  String parentId;
  ModalPresenter modalPresenter;
  EntityContainerListWidget entityContainerList;
  SubmissionViewScopeEditor submissionViewScope;
  TableType tableType;
  CreateTableViewWizardStep2 step2;
  GlobalApplicationState globalAppState;

  @Inject
  public CreateTableViewWizardStep1(
    CreateTableViewWizardStep1View view,
    SynapseJavascriptClient jsClient,
    EntityContainerListWidget entityContainerList,
    SubmissionViewScopeEditor submissionViewScope,
    CreateTableViewWizardStep2 step2,
    GlobalApplicationState globalAppState
  ) {
    super();
    this.view = view;
    this.step2 = step2;
    this.entityContainerList = entityContainerList;
    this.submissionViewScope = submissionViewScope;
    view.setEntityViewScopeWidget(entityContainerList.asWidget());
    view.setSubmissionViewScopeWidget(submissionViewScope);
    this.jsClient = jsClient;
    this.globalAppState = globalAppState;
    view.setPresenter(this);
  }

  @Override
  public void updateViewTypeMask() {
    tableType =
      TableType.getEntityViewTableType(
        view.isFileSelected(),
        view.isFolderSelected(),
        view.isTableSelected(),
        view.isDatasetSelected()
      );
  }

  /**
   * Configure this widget before use.
   *
   * @param parentId
   */
  public void configure(String parentId, TableType type) {
    this.parentId = parentId;
    this.tableType = type;
    boolean canEdit = true;
    view.setEntityViewScopeWidgetVisible(false);
    view.setSubmissionViewScopeWidgetVisible(false);

    if (TableType.submission_view.equals(type)) {
      view.setSubmissionViewScopeWidgetVisible(true);
    } else if (
      !TableType.table.equals(type) &&
      !TableType.dataset.equals(type) &&
      !TableType.dataset_collection.equals(type)
    ) {
      view.setEntityViewScopeWidgetVisible(true);
    }

    if (
      TableType.table.equals(type) ||
      TableType.dataset.equals(type) ||
      TableType.dataset_collection.equals(type) ||
      TableType.project_view.equals(type) ||
      TableType.submission_view.equals(type)
    ) {
      view.setViewTypeOptionsVisible(false);
    } else {
      view.setViewTypeOptionsVisible(true);
      // update the checkbox state based on the view type mask
      view.setIsFileSelected(type.isIncludeFiles());
      view.setIsFolderSelected(type.isIncludeFolders());
      view.setIsTableSelected(type.isIncludeTables());
      view.setIsDatasetSelected(type.isIncludeDatasets());
    }

    entityContainerList.configure(new ArrayList<Reference>(), canEdit, type);
    submissionViewScope.configure(new ArrayList<Evaluation>());
    view.setName("");
    view.setDescription("");
  }

  /**
   * Create the Table/View
   *
   * @param name
   */
  private void createEntity(final String name, final String description) {
    modalPresenter.setLoading(true);
    Table table;
    List<ListenableFuture<?>> futures = new ArrayList<>();
    if (TableType.table.equals(tableType)) {
      table = new TableEntity();
    } else if (
      TableType.dataset.equals(tableType) ||
      TableType.dataset_collection.equals(tableType)
    ) {
      ViewEntityType viewEntityType;
      if (TableType.dataset.equals(tableType)) {
        table = new Dataset();
        viewEntityType = ViewEntityType.dataset;
      } else {
        table = new DatasetCollection();
        viewEntityType = ViewEntityType.datasetcollection;
      }
      ((EntityRefCollectionView) table).setItems(Collections.EMPTY_LIST); // Workaround for PLFM-7076
      // For Datasets, automatically add the default columns (SWC-5917)
      FluentFuture<List<ColumnModel>> defaultColumnsFuture = jsClient.getDefaultColumnsForView(
        viewEntityType
      );
      defaultColumnsFuture.addCallback(
        new FutureCallback<List<ColumnModel>>() {
          @Override
          public void onSuccess(@Nullable List<ColumnModel> results) {
            List<String> columnIds = new ArrayList<>();
            for (ColumnModel col : results) {
              columnIds.add(col.getId());
            }
            table.setColumnIds(columnIds);
          }

          @Override
          public void onFailure(Throwable t) {
            modalPresenter.setErrorMessage(
              "Error fetching columns for the table: " + t.getMessage()
            );
          }
        },
        directExecutor()
      );
      futures.add(defaultColumnsFuture);
    } else if (TableType.submission_view.equals(tableType)) {
      table = new SubmissionView();
      List<String> scopeIds = submissionViewScope.getEvaluationIds();
      if (scopeIds.isEmpty()) {
        modalPresenter.setErrorMessage(EMPTY_SCOPE_MESSAGE);
        return;
      }
      ((SubmissionView) table).setScopeIds(scopeIds);
    } else {
      table = new EntityView();
      List<String> scopeIds = entityContainerList.getEntityIds();
      if (scopeIds.isEmpty()) {
        modalPresenter.setErrorMessage(EMPTY_SCOPE_MESSAGE);
        return;
      }
      ((EntityView) table).setScopeIds(scopeIds);
      ((EntityView) table).setViewTypeMask(
          tableType.getViewTypeMask().longValue()
        );
    }
    table.setName(name);
    table.setParentId(parentId);
    table.setDescription(description);
    // Wait for all possible requests to complete before creating the entity.
    FluentFuture.from(
      whenAllComplete(futures)
        .call(
          () -> {
            createEntity(table);
            return null;
          },
          directExecutor()
        )
    );
  }

  private void createEntity(final Entity entity) {
    jsClient
      .createEntity(entity)
      .addCallback(
        new FutureCallback<Entity>() {
          @Override
          public void onSuccess(Entity table) {
            // For Datasets, this is the only step.
            if (
              TableType.dataset.equals(tableType) ||
              TableType.dataset_collection.equals(tableType)
            ) {
              // Go to the dataset page
              globalAppState.getPlaceChanger().goTo(new Synapse(table.getId()));
              modalPresenter.onFinished();
            } else {
              // All other tables go to step 2
              step2.configure((Table) table, tableType);
              modalPresenter.setNextActivePage(step2);
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            modalPresenter.setErrorMessage(caught.getMessage());
          }
        },
        directExecutor()
      );
  }

  /**
   * Should be Called when the create button is clicked on the dialog.
   */
  @Override
  public void onPrimary() {
    String tableName = view.getName();
    String tableDescription = view.getDescription();
    if (tableName == null || "".equals(tableName)) {
      modalPresenter.setErrorMessage(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
    } else {
      createEntity(tableName, tableDescription);
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setModalPresenter(ModalPresenter modalPresenter) {
    this.modalPresenter = modalPresenter;
    modalPresenter.setPrimaryButtonText(
      TableType.dataset.equals(tableType) ? FINISH : NEXT
    );
  }
}
