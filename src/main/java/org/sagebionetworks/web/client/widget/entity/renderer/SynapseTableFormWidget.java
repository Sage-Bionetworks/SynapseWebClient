package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PartialRow;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSetUtils;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableFormWidget implements SynapseTableFormWidgetView.Presenter, WidgetRendererPresenter {
	
	private SynapseTableFormWidgetView view;
	private Map<String, String> descriptor;
	private SynapseAlert synAlert;
	private RowFormEditorWidget rowWidget;
	private String tableId;
	private List<ColumnModel> headers;
	private SynapseClientAsync synapseClient;
	private AsynchronousJobTracker jobTracker;
	public static final String DEFAULT_SUCCESS_MESSAGE = "Your response has been recorded";
	
	@Inject
	public SynapseTableFormWidget(SynapseTableFormWidgetView view,
			SynapseAlert synAlert,
			RowFormEditorWidget rowWidget,
			AsynchronousJobTracker jobTracker,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.synAlert = synAlert;
		this.rowWidget = rowWidget;
		this.jobTracker = jobTracker;
		this.synapseClient = synapseClient;
		view.setRowFormWidget(rowWidget.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void clear() {
		synAlert.clear();
		rowWidget.clear();
		view.setSubmitButtonVisible(false);
		view.setSuccessMessageVisible(false);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		clear();
		if (!synAlert.isUserLoggedIn()) {
			synAlert.showMustLogin();
			return;
		}
		view.setSubmitButtonLoading(false);
		tableId = descriptor.get(WidgetConstants.TABLE_ID_KEY);
		String successMessage = descriptor.get(WidgetConstants.SUCCESS_MESSAGE);
		if (successMessage == null) {
			successMessage = DEFAULT_SUCCESS_MESSAGE;
		}
		view.setSuccessMessage(successMessage);
		
		//get the table schema and init row widget!
		synapseClient.getColumnModelsForTableEntity(tableId, new AsyncCallback<List<ColumnModel>>() {
			@Override
			public void onSuccess(List<ColumnModel> result) {
				headers = result;
				rowWidget.configure(tableId, headers);
				view.setSubmitButtonVisible(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void onSubmit() {
		synAlert.clear();
		
		// Are the changes valid?
		if (!rowWidget.isValid()) {
			synAlert.showError(QueryResultEditorWidget.SEE_THE_ERRORS_ABOVE);
			return;
		}
		
		PartialRow pr = RowSetUtils.buildPartialRow(headers, rowWidget.getRow(), null);
		List<PartialRow> rows = new ArrayList<PartialRow>();
		rows.add(pr);
		PartialRowSet prs = new PartialRowSet();
		prs.setRows(rows);
		prs.setTableId(tableId);
		
		AppendableRowSetRequest request = new AppendableRowSetRequest();
		request.setToAppend(prs);
		request.setEntityId(this.tableId);
		view.setSubmitButtonLoading(true);
		jobTracker.startAndTrack(AsynchType.TableAppendRowSet, request, AsynchronousProgressWidget.WAIT_MS,
				new UpdatingAsynchProgressHandler() {
					
					@Override
					public void onFailure(Throwable failure) {
						synAlert.handleException(failure);
					}
					
					@Override
					public void onComplete(AsynchronousResponseBody response) {
						clear();
						view.setSuccessMessageVisible(true);
					}
					
					@Override
					public void onCancel() {
						view.setSubmitButtonLoading(false);
					}
					
					@Override
					public void onUpdate(AsynchronousJobStatus status) {
					}
					
					@Override
					public boolean isAttached() {
						return true;
					}
				});
	}
}
