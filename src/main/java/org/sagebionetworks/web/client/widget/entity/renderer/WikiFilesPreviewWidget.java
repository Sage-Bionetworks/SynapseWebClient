package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Map;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiFilesPreviewWidget implements WikiFilesPreviewWidgetView.Presenter, WidgetRendererPresenter {

	private WikiFilesPreviewWidgetView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;

	@Inject
	public WikiFilesPreviewWidget(WikiFilesPreviewWidgetView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		view.setPresenter(this);
	}

	@Override
	public void configure(final WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		// set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		// get all of the file attachments for this wiki page
		synapseClient.getWikiAttachmentHandles(wikiKey, new AsyncCallback<FileHandleResults>() {
			@Override
			public void onSuccess(FileHandleResults fileHandleResults) {
				// and grab the file endpoint from the global app state
				view.configure(wikiKey, fileHandleResults.getList());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/*
	 * Private Methods
	 */
}
