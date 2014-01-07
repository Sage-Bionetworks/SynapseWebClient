package org.sagebionetworks.web.client.widget.table;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableWidget implements SynapseTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private SynapseTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private AdapterFactory adapterFactory;
	GlobalApplicationState globalApplicationState;
	
	private TableEntity table;
	private String queryString = "select *";
	private List<ColumnModel> columns;
	
	@Inject
	public SynapseTableWidget(SynapseTableWidgetView view, 
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController, 
			AdapterFactory adapterFactory,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.adapterFactory = adapterFactory;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}	
	
	public void configure(final TableEntity table) {
		this.table = table;		
		synapseClient.getColumnModelBatch(table.getColumnIds(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					BatchResults<ColumnModel> columnsBatch = new BatchResults<ColumnModel>(ColumnModel.class);
					columnsBatch.initializeFromJSONObject(adapterFactory.createNew(result));
					columns = columnsBatch.getResults();
					view.configure(table, columns, queryString, true);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
			}
		});			
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		view.setPresenter(this);
	}

    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@Override
	public void query(String query) {
		this.queryString = query;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createColumn(ColumnModel col) {
		try {						
			String columnJson = col.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createColumnModel(columnJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						ColumnModel newCol = new ColumnModel(adapterFactory.createNew(result));
						if(newCol.getId() != null) {
							table.getColumnIds().add(newCol.getId());
							updateTableEntity();
						}
						else {
							onFailure(null);
						}
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
							view.showErrorMessage(DisplayConstants.COLUMN_CREATION_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateColumnOrder(List<String> columnIds) {
		table.setColumnIds(columnIds);
		updateTableEntity();
	}
	
	/*
	 * Private Methods
	 */
	
	private void updateTableEntity() {
		try {
			String entityJson = table.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.updateEntity(entityJson, new AsyncCallback<EntityWrapper>() {
				@Override
				public void onSuccess(EntityWrapper result) {
					TableEntity newTable;
					try {
						newTable = new TableEntity(adapterFactory.createNew(result.getEntityJson()));
						configure(newTable);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
						view.showErrorMessage(DisplayConstants.TABLE_UPDATE_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.TABLE_UPDATE_FAILED);
		}
	}
	
}
