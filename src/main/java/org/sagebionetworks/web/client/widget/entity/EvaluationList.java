package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationList implements EvaluationListView.Presenter,
		SynapseWidgetPresenter {

	private EvaluationListView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public EvaluationList(EvaluationListView view, SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(List<Evaluation> list) {
		view.configure(list);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public List<String> getSelectedEvaluationIds() {
		return view.getSelectedEvaluationIds();
	}
}
