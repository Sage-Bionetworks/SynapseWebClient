package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiFilesPreviewWidget implements WikiFilesPreviewWidgetView.Presenter, WidgetRendererPresenter {
	
	private WikiFilesPreviewWidgetView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public WikiFilesPreviewWidget(WikiFilesPreviewWidgetView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		//get all of the file attachments for this wiki page
		synapseClient.getWikiAttachmentHandles(wikiKey, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String results) {
				try {
					FileHandleResults fileHandleResults = nodeModelCreator.createJSONEntity(results, FileHandleResults.class);
					//and grab the file endpoint from the global app state
					
					view.configure(wikiKey, fileHandleResults.getList());
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
