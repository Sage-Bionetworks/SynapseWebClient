package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelResponse;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorV2Widget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

/**
 * Wizard page to edit column schema
 *
 * @author Jay
 *
 */
public class CreateTableViewWizardStep2 implements ModalPage, IsWidget {

  public static final String DELETE_PLACEHOLDER_FAILURE_MESSAGE =
    "Unable to delete table/view ";
  public static final String DELETE_PLACEHOLDER_SUCCESS_MESSAGE =
    "User cancelled creation of table/view.  Deleted placeholder: ";
  public static final String SCHEMA_UPDATE_CANCELLED =
    "Schema update cancelled";
  public static final String FINISH = "Finish";
  ColumnModelsEditorWidget editor;
  ColumnModelsEditorV2Widget editorV2;
  String tableId;
  ModalPresenter presenter;
  // the TableEntity or View
  Table entity;
  TableType tableType;
  SynapseClientAsync synapseClient;
  JobTrackingWidget jobTrackingWidget;
  SynapseJavascriptClient jsClient;
  CreateTableViewWizardStep2View view;
  SynapseJSNIUtils jsniUtils;
  GlobalApplicationState globalAppState;
  CookieProvider cookies;

  /*
   * Set to true to indicate that change selections are in progress. This allows selection change
   * events to be ignored during this period.
   */
  boolean changingSelection = false;
  ViewDefaultColumns fileViewDefaultColumns;

  /**
   * New presenter with its view.
   *
   * @param view
   */
  @Inject
  public CreateTableViewWizardStep2(
    CreateTableViewWizardStep2View view,
    ColumnModelsEditorWidget editor,
    SynapseClientAsync synapseClient,
    JobTrackingWidget jobTrackingWidget,
    ViewDefaultColumns fileViewDefaultColumns,
    SynapseJavascriptClient jsClient,
    SynapseJSNIUtils jsniUtils,
    GlobalApplicationState globalAppState,
    ColumnModelsEditorV2Widget editorV2,
    CookieProvider cookieProvider
  ) {
    this.view = view;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.editor = editor;
    this.editorV2 = editorV2;
    this.jobTrackingWidget = jobTrackingWidget;
    this.fileViewDefaultColumns = fileViewDefaultColumns;
    this.jsClient = jsClient;
    this.jsniUtils = jsniUtils;
    this.globalAppState = globalAppState;
    this.cookies = cookieProvider;
    view.setJobTracker(jobTrackingWidget.asWidget());
    if (DisplayUtils.isInTestWebsite(cookieProvider)) {
      view.setEditor(editorV2.asWidget());
    } else {
      view.setEditor(editor.asWidget());
      editor.setOnAddDefaultViewColumnsCallback(
        this::onAddDefaultViewColumnsCallback
      );
      editor.setOnAddAnnotationColumnsCallback(() ->
        getPossibleColumnModelsForViewScope(null)
      );
    }
  }

  public void configure(Table entity, TableType tableType) {
    view.setJobTrackerVisible(false);
    this.changingSelection = false;
    this.entity = entity;
    this.tableType = tableType;

    List<ColumnModel> defaultColumns = getDefaultColumns();

    if (DisplayUtils.isInTestWebsite(this.cookies)) {
      editorV2.configure(
        EntityTypeUtils.getEntityType(entity),
        getViewScope(),
        defaultColumns
      );
    } else {
      editor.configure(tableType, new ArrayList<>());
      boolean isView = !TableType.table.equals(tableType);
      this.editor.setAddDefaultColumnsButtonVisible(isView);
      this.editor.setAddAnnotationColumnsButtonVisible(isView);
      if (isView) {
        // start with the default file columns
        editor.addColumns(defaultColumns);
      }
    }
  }

  public void onAddDefaultViewColumnsCallback() {
    editor.addColumns(getDefaultColumns());
  }

  public List<ColumnModel> getDefaultColumns() {
    boolean isView = !TableType.table.equals(tableType);
    if (isView) {
      return fileViewDefaultColumns.getDefaultViewColumns(tableType);
    } else {
      return Collections.emptyList();
    }
  }

  public ViewScope getViewScope() {
    ViewScope scope = new ViewScope();
    List<String> scopeIds = null;
    if (entity instanceof TableEntity) {
      return null;
    } else if (entity instanceof EntityView) {
      scopeIds = ((EntityView) entity).getScopeIds();
      scope.setViewTypeMask(tableType.getViewTypeMask().longValue());
      scope.setViewEntityType(ViewEntityType.entityview);
    } else if (entity instanceof SubmissionView) {
      scopeIds = ((SubmissionView) entity).getScopeIds();
      scope.setViewEntityType(ViewEntityType.submissionview);
    }
    scope.setScope(scopeIds);
    return scope;
  }

  public void getPossibleColumnModelsForViewScope(String nextPageToken) {
    view.setJobTrackerVisible(true);
    presenter.clearErrors();
    ViewScope scope = getViewScope();

    ViewColumnModelRequest request = new ViewColumnModelRequest();
    request.setViewScope(scope);
    request.setNextPageToken(nextPageToken);

    this.jobTrackingWidget.startAndTrackJob(
        ColumnModelsWidget.RETRIEVING_DATA,
        false,
        AsynchType.ViewColumnModelRequest,
        request,
        new AsynchronousProgressHandler() {
          @Override
          public void onFailure(Throwable failure) {
            view.setJobTrackerVisible(false);
            presenter.setError(failure);
          }

          @Override
          public void onComplete(AsynchronousResponseBody response) {
            ViewColumnModelResponse viewColumnModelResponse =
              (ViewColumnModelResponse) response;
            editor.addColumns(viewColumnModelResponse.getResults());
            if (viewColumnModelResponse.getNextPageToken() != null) {
              getPossibleColumnModelsForViewScope(
                viewColumnModelResponse.getNextPageToken()
              );
            } else {
              view.setJobTrackerVisible(false);
            }
          }

          @Override
          public void onCancel() {
            view.setJobTrackerVisible(false);
          }
        }
      );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setModalPresenter(ModalPresenter presenter) {
    this.presenter = presenter;
    presenter.setPrimaryButtonText(FINISH);

    ((ModalWizardWidget) presenter).addCallback(
        new ModalWizardWidget.WizardCallback() {
          @Override
          public void onFinished() {}

          @Override
          public void onCanceled() {
            onCancel();
          }
        }
      );
  }

  public void onCancel() {
    // user decided not to create the table/view. clean it up.
    String entityId = entity.getId();
    jsClient.deleteEntityById(
      entityId,
      true,
      new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          jsniUtils.consoleError(
            DELETE_PLACEHOLDER_FAILURE_MESSAGE +
            entityId +
            ": " +
            caught.getMessage()
          );
        }

        @Override
        public void onSuccess(Void result) {
          jsniUtils.consoleLog(DELETE_PLACEHOLDER_SUCCESS_MESSAGE + entityId);
        }
      }
    );
  }

  @Override
  public void onPrimary() {
    presenter.setLoading(true);
    // Save it the data is valid
    boolean isValid = DisplayUtils.isInTestWebsite(cookies)
      ? editorV2.validate()
      : editor.validate();
    if (!isValid) {
      presenter.setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
      return;
    }
    // Get the models from the view and save them
    List<ColumnModel> newSchema;
    if (DisplayUtils.isInTestWebsite(cookies)) {
      newSchema = editorV2.getEditedColumnModels();
    } else {
      newSchema = editor.getEditedColumnModels();
    }
    presenter.clearErrors();
    synapseClient.getTableUpdateTransactionRequest(
      entity.getId(),
      new ArrayList<>(),
      newSchema,
      new AsyncCallback<TableUpdateTransactionRequest>() {
        @Override
        public void onFailure(Throwable caught) {
          presenter.setError(caught);
        }

        @Override
        public void onSuccess(TableUpdateTransactionRequest request) {
          if (request.getChanges().isEmpty()) {
            globalAppState.getPlaceChanger().goTo(new Synapse(entity.getId()));
            finished();
          } else {
            startTrackingJob(request);
          }
        }
      }
    );
  }

  public void finished() {
    // Hide the dialog
    presenter.setLoading(false);
    presenter.onFinished();
  }

  public void startTrackingJob(TableUpdateTransactionRequest request) {
    view.setJobTrackerVisible(true);
    presenter.setLoading(true);
    presenter.clearErrors();
    this.jobTrackingWidget.startAndTrackJob(
        ColumnModelsWidget.UPDATING_SCHEMA,
        false,
        AsynchType.TableTransaction,
        request,
        new AsynchronousProgressHandler() {
          @Override
          public void onFailure(Throwable failure) {
            view.setJobTrackerVisible(false);
            presenter.setError(failure);
          }

          @Override
          public void onComplete(AsynchronousResponseBody response) {
            view.setJobTrackerVisible(false);
            globalAppState.getPlaceChanger().goTo(new Synapse(entity.getId()));
            finished();
          }

          @Override
          public void onCancel() {
            view.setJobTrackerVisible(false);
            presenter.setErrorMessage(SCHEMA_UPDATE_CANCELLED);
          }
        }
      );
  }
}
