package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.ACTAccessApproval;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ACTApprovalStatus;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ApproveUserAccessModal implements ApproveUserAccessModalView.Presenter, IsWidget {
	
	public static final String EMAIL_SUBJECT = "Data access approval";
	public static final String SELECT_FROM = "SELECT \"Email Body\" FROM ";
	public static final String WHERE = " WHERE \"Dataset Id\"= \"";	
	// Mask to get all parts of a query.
	private static final Long ALL_PARTS_MASK = new Long(255);
	
	private String accessRequirement;
	private String userId;
	private String datasetId;
	private String message;
	private EntityBundle entityBundle;
	
	private ApproveUserAccessModalView view;
	private SynapseAlert synAlert;
	private SynapseSuggestBox peopleSuggestWidget;
	private Map<String, AccessRequirement> arMap;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private JobTrackingWidget progressWidget;
	private Dialog messagePreview;
	
	@Inject
	public ApproveUserAccessModal(ApproveUserAccessModalView view,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider, 
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			JobTrackingWidget progressWidget,
			Dialog messagePreview) {
		this.view = view;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.progressWidget = progressWidget;
		this.messagePreview = messagePreview;
		peopleSuggestWidget.setSuggestionProvider(provider);
		this.view.setPresenter(this);
		this.view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		this.view.setLoadingEmailWidget(progressWidget.asWidget());
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
	}

	public void configure(List<ACTAccessRequirement> accessRequirements, EntityBundle bundle) {
		view.startLoadingEmail();
		this.entityBundle = bundle;
		this.arMap = new HashMap<String, AccessRequirement>();
		List<String> list = new ArrayList<String>();
		for (ACTAccessRequirement ar : accessRequirements) {
			arMap.put(Long.toString(ar.getId()), ar);
			list.add(Long.toString(ar.getId()));
		}
		view.setSynAlert(synAlert.asWidget());
		view.setStates(list);
		if (list.size() > 0) {
			onStateSelected(list.get(0));			
		}
		datasetId = entityBundle.getEntity().getId(); //get synId of dataset we are currently on
		view.setDatasetTitle(entityBundle.getEntity().getName());
		loadEmailMessage();
	}
	
	private void loadEmailMessage() {
		Query query = getDefaultQuery();
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		qbr.setQuery(query);
		qbr.setEntityId(QueryBundleUtils.getTableId(query));
		this.progressWidget.startAndTrackJob("Running query...", false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				QueryResultBundle result = (QueryResultBundle) response;
				if (hasResult(result)) {
					message = result.getQueryResult().getQueryResults().getRows().get(0).getValues().get(0);
					messagePreview.configure("Email Message Preview", view.getEmailBodyWidget(message), null, "Close", null, true);
					view.finishLoadingEmail();
				} else {
					synAlert.showError("An error was encountered while loading the email body");
				}
			}

			@Override
			public void onCancel() {
				synAlert.showError("Query cancelled");
			}
		});
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
		builder.append(globalApplicationState.getSynapseProperty("org.sagebionetworks.portal.act.synapse_storage_id"));
		builder.append(WHERE);
		builder.append(datasetId + "\"");
		
		Query query = new Query();
		query.setSql(builder.toString());
		query.setIsConsistent(true);
		return query;
	}
	
	public void show() {
		synAlert.clear();
		view.show();
	}
	
	@Override
	public void onSubmit() {
		if (userId == null) {
			synAlert.showError("You must select a user to approve");
			return;
		}
		if (message == null) {
			synAlert.showError("An error was encountered while loading the email message body");
			return;
		}
		accessRequirement = view.getAccessRequirement();
		view.setApproveProcessing(true);
		ACTAccessApproval aa  = new ACTAccessApproval();
		aa.setAccessorId(userId);  //user id
		aa.setApprovalStatus(ACTApprovalStatus.APPROVED);
		aa.setRequirementId(Long.parseLong(accessRequirement)); //requirement id
		synapseClient.createAccessApproval(aa, new AsyncCallback<AccessApproval>() {

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.setApproveProcessing(false);
			}

			@Override
			public void onSuccess(AccessApproval result) {
				sendEmail(result);
			}
		});
	}
	
	private void sendEmail(AccessApproval result) {
		Set<String> recipients = new HashSet<String>();
		recipients.add(userId);
		synapseClient.sendMessage(recipients, EMAIL_SUBJECT, message, null, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				view.setApproveProcessing(false);
				synAlert.showError("User has been approved, but an error was encountered while emailing them:" + caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				view.setApproveProcessing(false);
				view.hide();
				view.showInfo("Successfully approved user", "An email has been sent to notify them");
			}
		});
	}
	
	public void onUserSelected(SynapseSuggestion suggestion) {
		this.userId = suggestion.getId();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}

	@Override
	public void onStateSelected(String state) {
		accessRequirement = state;
		view.setAccessRequirement(state, GovernanceServiceHelper.getAccessRequirementText(arMap.get(state)));
	}
	
	@Override
	public void showPreview() {
		messagePreview.show();
	}
		
}
