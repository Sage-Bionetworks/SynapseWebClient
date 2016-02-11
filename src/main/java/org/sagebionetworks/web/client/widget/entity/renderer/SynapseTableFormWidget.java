package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.PartialRow;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormWidget;
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
	private RowFormWidget rowWidget;
	private String tableId;
	private List<ColumnModel> headers;
	private SynapseClientAsync synapseClient;
	JobTrackingWidget editJobTrackingWidget;
	public static final String DEFAULT_SUCCESS_MESSAGE = "Your response has been recorded";
	
	@Inject
	public SynapseTableFormWidget(SynapseTableFormWidgetView view,
			SynapseAlert synAlert,
			RowFormWidget rowWidget,
			JobTrackingWidget editJobTrackingWidget,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.synAlert = synAlert;
		this.rowWidget = rowWidget;
		this.editJobTrackingWidget = editJobTrackingWidget;
		this.synapseClient = synapseClient;
		view.setRowFormWidget(rowWidget.asWidget());
		view.setSynAlertWidget(synAlert.asWidget());
		view.setProgressWidget(editJobTrackingWidget.asWidget());
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		synAlert.clear();
		rowWidget.clear();
		view.setSuccessMessageVisible(false);
		if (!synAlert.isUserLoggedIn()) {
			//must login
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
		view.setSubmitButtonLoading(true);

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
		editJobTrackingWidget.startAndTrackJob("Submitting...", false,
				AsynchType.TableAppendRowSet, request,
				new AsynchronousProgressHandler() {
					@Override
					public void onFailure(Throwable t) {
						synAlert.handleException(t);
					}

					@Override
					public void onComplete(AsynchronousResponseBody response) {
						view.setSuccessMessageVisible(true);
						view.setSubmitButtonLoading(false);
					}

					@Override
					public void onCancel() {
						// If they cancel after the job starts, treat it as a
						// change.
						view.setSubmitButtonLoading(false);
					}
				});
	}
}
