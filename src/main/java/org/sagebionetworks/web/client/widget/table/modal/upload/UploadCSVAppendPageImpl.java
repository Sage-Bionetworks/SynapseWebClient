package org.sagebionetworks.web.client.widget.table.modal.upload;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVAppendPageImpl implements UploadCSVAppendPage {

	private static final String UPDATE_TABLE = "Update Table";
	// Injected dependencies
	UploadCSVAppendPageView view;
	SynapseClientAsync synapseClient;
	JobTrackingWidget jobTrackingWidget;

	// runtime data
	ModalPresenter presenter;
	UploadToTableRequest request;

	@Inject
	public UploadCSVAppendPageImpl(UploadCSVAppendPageView view, SynapseClientAsync synapseClient, JobTrackingWidget jobTrackingWidget) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jobTrackingWidget = jobTrackingWidget;
		this.view.addJobTrackingWidget(jobTrackingWidget);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(UploadToTableRequest request, List<ColumnModel> suggestedSchema) {
		this.request = request;
	}

	@Override
	public void onPrimary() {
		presenter.setLoading(true);
		view.setTrackingWidgetVisible(true);
		// Start the job.
		TableUpdateTransactionRequest transactionRequest = new TableUpdateTransactionRequest();
		transactionRequest.setEntityId(request.getTableId());
		List<TableUpdateRequest> changes = new ArrayList<TableUpdateRequest>();
		changes.add(request);
		transactionRequest.setChanges(changes);
		this.jobTrackingWidget.startAndTrackJob("Applying CSV to the Table...", false, AsynchType.TableTransaction, transactionRequest, new AsynchronousProgressHandler() {

			@Override
			public void onFailure(Throwable failure) {
				presenter.setErrorMessage(failure.getMessage());
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				String errors = QueryResultEditorWidget.getEntityUpdateResultsFailures(response);
				if (!errors.isEmpty()) {
					view.showErrorDialog(errors);
				}
				presenter.onFinished();
			}

			@Override
			public void onCancel() {
				presenter.onCancel();
			}
		});
	}

	@Override
	public void setModalPresenter(ModalPresenter presenter) {
		this.presenter = presenter;
		this.presenter.setPrimaryButtonText(UPDATE_TABLE);
		this.view.setTrackingWidgetVisible(false);
	}

}
