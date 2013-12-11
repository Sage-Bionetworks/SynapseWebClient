package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiHistoryWidget implements WikiHistoryWidgetView.Presenter,
	SynapseWidgetPresenter{

	private WikiHistoryWidgetView view;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	WikiPageWidgetView.Presenter wikiPagePresenter;
	private WikiPageKey wikiKey;
	
	@Inject
	public WikiHistoryWidget(WikiHistoryWidgetView view, SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this, null);
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this, wikiPagePresenter);		
		return view.asWidget();
	}
	
	@Override
	public void configure(final WikiPageKey key, final boolean canEdit, final WikiPageWidgetView.Presenter wikiPagePresenter) {
		this.wikiKey = key;
		synapseClient.getV2WikiHistory(wikiKey, new Long(100), new Long(0), new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(String result) {
				org.sagebionetworks.web.shared.PaginatedResults<V2WikiHistorySnapshot> paginatedHistory;
				try {
					paginatedHistory = nodeModelCreator.createPaginatedResults(result, V2WikiHistorySnapshot.class);
					view.configure(canEdit, paginatedHistory.getResults(), wikiPagePresenter);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
				
			}
			
		});
	}
}
