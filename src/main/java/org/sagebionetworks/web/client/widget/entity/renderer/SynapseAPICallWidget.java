package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAPICallWidget implements SynapseAPICallWidgetView.Presenter, WidgetRendererPresenter {
	
	private SynapseAPICallWidgetView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	
	@Inject
	public SynapseAPICallWidget(SynapseAPICallWidgetView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
	}
	
	@Override
	public void configure(String entityId, Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		String uri = descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		if (uri != null) {
			synapseClient.getJSONEntity(uri, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					JSONObjectAdapter adapter;
					try {
						adapter = jsonObjectAdapter.createNew(result);
						if (adapter.has("results")) {
							JSONArrayAdapter resultsList = adapter.getJSONArray("results");
							if (resultsList.length() > 0) {
								JSONObjectAdapter firstItem = resultsList.getJSONObject(0);
								//initialize column data
								Map<String, List<String>> columnData = new HashMap<String, List<String>>();
								//initialize the column data lists
								for (Iterator<String> iterator = firstItem.keys(); iterator.hasNext();) {
									columnData.put(iterator.next(), new ArrayList<String>());
								}
	
								for (int i = 0; i < resultsList.length(); i++) {
									JSONObjectAdapter row = resultsList.getJSONObject(i);
									for (String key : columnData.keySet()) {
										String value = (String)row.get(key);
										List<String> col = columnData.get(key);
										col.add(value);
									}
								}
								
								String columns = descriptor.get(WidgetConstants.API_TABLE_WIDGET_COLUMNS_KEY);
								String displayColumnNames = descriptor.get(WidgetConstants.API_TABLE_WIDGET_DISPLAY_COLUMN_NAMES_KEY);
								String rendererNames = descriptor.get(WidgetConstants.API_TABLE_WIDGET_RENDERERS_KEY);
								
								view.configure(columnData, columns, displayColumnNames, rendererNames);
							}
						}
					} catch (JSONObjectAdapterException e1) {
					}

				}
				
				
				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
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
