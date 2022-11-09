package org.sagebionetworks.web.client.widget.entity.act;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.asynch.AsynchType;

public class ApproveUserAccessModal
  implements ApproveUserAccessModalView.Presenter, IsWidget {

  public static final String EMAIL_SUBJECT = "Data access approval";
  public static final String SELECT_FROM = "SELECT \"Email Body\" FROM ";
  public static final String WHERE = " WHERE \"Dataset Id\"='";
  public static final String QUERY_CANCELLED = "Query cancelled";
  public static final String NO_EMAIL_MESSAGE =
    "You must enter an email to send to the user";
  public static final String NO_USER_SELECTED =
    "You must select a user to approve";
  public static final String APPROVE_BUT_FAIL_TO_EMAIL =
    "User has been approved, but an error was encountered while emailing them: ";
  public static final String APPROVED_USER = "Successfully Approved User. ";
  public static final String REVOKED_USER =
    "Successfully Revoked User Access. ";
  public static final String EMAIL_SENT =
    "An email has been sent to notify them";
  public static final String MESSAGE_BLANK =
    "You must enter an email message to approve this user";
  public static final String NO_COMPATIBLE_ARS_MESSAGE =
    "No access requirements that support this functionality were found.";

  // Mask to get all parts of a query.
  private static final Long ALL_PARTS_MASK = 255L;

  private String accessRequirementId;
  private String userId;
  private String datasetId;
  private String message;

  private final ApproveUserAccessModalView view;
  private final SynapseAlert synAlert;
  private final SynapseClientAsync synapseClient;
  private final SynapseProperties synapseProperties;
  private final JobTrackingWidget progressWidget;
  private final DataAccessClientAsync dataAccessClient;
  private final AccessRequirementWidget arWidget;
  private final RestrictableObjectDescriptor subject;
  private final PopupUtilsView popupUtils;

  @Inject
  public ApproveUserAccessModal(
    ApproveUserAccessModalView view,
    AccessRequirementWidget arWidget,
    SynapseAlert synAlert,
    SynapseSuggestBox peopleSuggestBox,
    UserGroupSuggestionProvider provider,
    SynapseClientAsync synapseClient,
    SynapseProperties synapseProperties,
    JobTrackingWidget progressWidget,
    DataAccessClientAsync dataAccessClient,
    final PopupUtilsView popupUtilsView
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.synapseProperties = synapseProperties;
    this.progressWidget = progressWidget;
    this.dataAccessClient = dataAccessClient;
    fixServiceEntryPoint(dataAccessClient);
    this.popupUtils = popupUtilsView;
    peopleSuggestBox.setSuggestionProvider(provider);
    this.view.setPresenter(this);
    this.view.setUserPickerWidget(peopleSuggestBox.asWidget());
    this.arWidget = arWidget;
    arWidget.hideControls();
    this.view.setAccessRequirementWidget(arWidget);
    view.setLoadingEmailWidget(this.progressWidget.asWidget());
    peopleSuggestBox.addItemSelectedHandler(this::onUserSelected);
    subject = new RestrictableObjectDescriptor();
    subject.setType(RestrictableObjectType.ENTITY);
  }

  public void configure(final EntityBundle bundle) {
    subject.setId(bundle.getEntity().getId());

    dataAccessClient.getAccessRequirements(
      subject,
      50L,
      0L,
      new AsyncCallback<List<AccessRequirement>>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }

        @Override
        public void onSuccess(List<AccessRequirement> result) {
          List<AccessRequirement> ars = new ArrayList<>(result);
          configure(ars, bundle);
        }
      }
    );
  }

  public void configure(
    List<AccessRequirement> accessRequirements,
    EntityBundle bundle
  ) {
    subject.setId(bundle.getEntity().getId());
    view.startLoadingEmail();
    List<String> list = new ArrayList<>();
    for (AccessRequirement ar : accessRequirements) {
      // approve/revoke for ManagedACTAccessRequirement handled through data access submission (backend does not currently support directly revoking this type)
      // see PLFM-6209, and SWC-5189
      if (!(ar instanceof ManagedACTAccessRequirement)) {
        list.add(Long.toString(ar.getId()));
      }
    }
    view.setSynAlert(synAlert.asWidget());
    view.setAccessRequirementIDs(list);
    if (!list.isEmpty()) {
      onAccessRequirementIDSelected(list.get(0));
      datasetId = bundle.getEntity().getId(); // get synId of dataset we are currently on
      view.setDatasetTitle(bundle.getEntity().getName());
      loadEmailMessage();
    } else {
      popupUtils.showErrorMessage(NO_COMPATIBLE_ARS_MESSAGE);
      view.hide();
    }
  }

  private void loadEmailMessage() {
    Query query = getDefaultQuery();
    QueryBundleRequest qbr = new QueryBundleRequest();
    qbr.setPartMask(ALL_PARTS_MASK);
    qbr.setQuery(query);
    qbr.setEntityId(QueryBundleUtils.getTableId(query));
    this.progressWidget.startAndTrackJob(
        "Running query...",
        false,
        AsynchType.TableQuery,
        qbr,
        new AsynchronousProgressHandler() {
          @Override
          public void onFailure(Throwable failure) {
            view.setLoadingEmailVisible(false);
            synAlert.handleException(failure);
          }

          @Override
          public void onComplete(AsynchronousResponseBody response) {
            QueryResultBundle result = (QueryResultBundle) response;
            if (hasResult(result)) {
              message =
                result
                  .getQueryResult()
                  .getQueryResults()
                  .getRows()
                  .get(0)
                  .getValues()
                  .get(0);
              view.setMessageEditArea(message);
            } else {
              message = "";
            }
            view.setMessageBody(message);
            view.finishLoadingEmail();
          }

          @Override
          public void onCancel() {
            view.setLoadingEmailVisible(false);
            view.finishLoadingEmail();
            synAlert.showError(QUERY_CANCELLED);
          }
        }
      );
    view.setLoadingEmailWidget(progressWidget.asWidget());
  }

  private boolean hasResult(QueryResultBundle result) {
    QueryResult qr = result.getQueryResult();
    if (qr != null) {
      RowSet rs = qr.getQueryResults();
      if (rs != null) {
        List<Row> rowList = rs.getRows();
        if (rowList != null && rowList.size() > 0) {
          Row r = rowList.get(0);
          if (r != null) {
            List<String> strList = r.getValues();
            if (strList != null && strList.size() > 0) {
              return strList.get(0) != null;
            }
          }
        }
      }
    }
    return false;
  }

  public Query getDefaultQuery() {
    StringBuilder builder = new StringBuilder();
    builder.append(SELECT_FROM);
    builder.append(
      synapseProperties.getSynapseProperty(
        "org.sagebionetworks.portal.act.synapse_storage_id"
      )
    );
    builder.append(WHERE);
    builder.append(datasetId + "'");

    Query query = new Query();
    query.setSql(builder.toString());
    return query;
  }

  public void show() {
    synAlert.clear();
    view.show();
  }

  @Override
  public void onRevoke() {
    if (userId == null) {
      synAlert.showError(NO_USER_SELECTED);
      return;
    }
    accessRequirementId = view.getAccessRequirement();
    view.setRevokeProcessing(true);
    synapseClient.deleteAccessApprovals(
      accessRequirementId,
      userId,
      new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          view.setRevokeProcessing(false);
        }

        @Override
        public void onSuccess(Void result) {
          view.setRevokeProcessing(false);
          view.hide();
          view.showInfo(REVOKED_USER);
        }
      }
    );
  }

  @Override
  public void onSubmit() {
    if (userId == null) {
      synAlert.showError(NO_USER_SELECTED);
      return;
    }
    message = view.getEmailMessage();
    if (message == null || message.isEmpty()) {
      synAlert.showError(MESSAGE_BLANK);
      return;
    }
    accessRequirementId = view.getAccessRequirement();
    view.setApproveProcessing(true);
    AccessApproval aa = new AccessApproval();
    aa.setAccessorId(userId); // user id
    aa.setRequirementId(Long.parseLong(accessRequirementId)); // requirement id
    synapseClient.createAccessApproval(
      aa,
      new AsyncCallback<AccessApproval>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          view.setApproveProcessing(false);
        }

        @Override
        public void onSuccess(AccessApproval result) {
          sendEmail();
        }
      }
    );
  }

  private void sendEmail() {
    Set<String> recipients = new HashSet<>();
    recipients.add(userId);
    synapseClient.sendMessage(
      recipients,
      EMAIL_SUBJECT,
      message,
      null,
      new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          view.setApproveProcessing(false);
          synAlert.showError(APPROVE_BUT_FAIL_TO_EMAIL + caught.getMessage());
        }

        @Override
        public void onSuccess(String result) {
          view.setApproveProcessing(false);
          view.hide();
          view.showInfo(APPROVED_USER + EMAIL_SENT);
        }
      }
    );
  }

  public void onUserSelected(UserGroupSuggestion suggestion) {
    this.userId = suggestion.getId();
  }

  public Widget asWidget() {
    view.setPresenter(this);
    return view.asWidget();
  }

  @Override
  public void onAccessRequirementIDSelected(String arID) {
    accessRequirementId = arID;
    arWidget.configure(arID, subject);
    view.setAccessRequirement(arID);
  }
}
