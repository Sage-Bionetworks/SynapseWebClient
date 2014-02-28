package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
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

public class CompleteTableWidget implements CompleteTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private CompleteTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private AdapterFactory adapterFactory;
	GlobalApplicationState globalApplicationState;
	
	private TableEntity table;
	private String queryString = "select *";
	private List<ColumnModel> columns;
	
	@Inject
	public CompleteTableWidget(CompleteTableWidgetView view, 
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
		synapseClient.getColumnModelsForTableEntity(table.getId(), new AsyncCallback<List<String>>() {
			@Override
			public void onSuccess(List<String> result) {
				try {
					columns = new ArrayList<ColumnModel>();
					for(String colStr : result) {
						columns.add(new ColumnModel(adapterFactory.createNew(colStr)));
					}
					
					// TODO : remove this
					columns = getTestColumns();
					
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
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
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
			synapseClient.createOrUpdateEntity(entityJson, null, false, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					synapseClient.getEntity(table.getId(), new AsyncCallback<EntityWrapper>() {
						@Override
						public void onSuccess(EntityWrapper result) {
							try {						
								table = new TableEntity(adapterFactory.createNew(result.getEntityJson()));
								configure(table);
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

	
	/*
	 * Temp
	 */
	private List<ColumnModel> getTestColumns() {
		List<ColumnModel> columns = new ArrayList<ColumnModel>();

		ColumnModel model;

		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("cellline");
		model.setName("cellline");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("Drug1");
		model.setName("Drug1");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_Conc");
		model.setName("Drug1_Conc");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_InhibitionMean");
		model.setName("Drug1_InhibitionMean");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_InhibitionStdev");
		model.setName("Drug1_InhibitionStdev");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("Drug2");
		model.setName("Drug2");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_Conc");
		model.setName("Drug2_Conc");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_InhibitionMean");
		model.setName("Drug2_InhibitionMean");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_InhibitionStdev");
		model.setName("Drug2_InhibitionStdev");
		columns.add(model);
		
		
//		// first name
//		ColumnModel model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("FirstName");
//		model.setName("First Name");
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("LastName");
//		model.setName("Last Name");
//		columns.add(model);
//
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.FILEHANDLEID);
//		model.setId("Plot");
//		model.setName("Plot");
//		columns.add(model);		
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("Category");
//		model.setName("Category");
//		model.setEnumValues(Arrays.asList(new String[] {"One", "Two", "Three"}));
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("Address");
//		model.setName("Address");
//		columns.add(model);
//		
////		model = new ColumnModel();
////		model.setColumnType(ColumnType.DATE);
////		model.setId("Birthday");
////		model.setName("Birthday");
////		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.BOOLEAN);
//		model.setId("IsAlive");
//		model.setName("IsAlive");
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.DOUBLE);
//		model.setId("BMI");
//		model.setName("BMI");
//		columns.add(model);

		
		return columns;
	}

		
}
