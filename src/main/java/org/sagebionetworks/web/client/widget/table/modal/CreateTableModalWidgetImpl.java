package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for creating a TableEntity.
 * This widget is a bootstrap modal and must be added to the page. 
 * @author John
 *
 */
public class CreateTableModalWidgetImpl implements CreateTableModalView.Presenter, CreateTableModalWidget {

	public static final String TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "Table name must include at least one character.";
	
	CreateTableModalView view;
	SynapseClientAsync synapseClient;
	String parentId;
	TableCreatedHandler handler;
	
	@Inject
	public CreateTableModalWidgetImpl(CreateTableModalView view,
			SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.view.setPresenter(this);
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 * @param handler
	 */
	@Override
	public void configure(String parentId, TableCreatedHandler handler){
		this.parentId = parentId;
		this.handler = handler;
	}
	
	/**
	 * Create the table.
	 * @param name
	 */
	private void createTableEntity(final String name) {
		view.setLoading(true);
		TableEntity table = new TableEntity();
		table.setName(name);
		table.setParentId(parentId);
		table.setEntityType(TableEntity.class.getName());
		synapseClient.createTableEntity(table, new AsyncCallback<TableEntity>() {
			@Override
			public void onSuccess(TableEntity table) {
				view.hide();
				handler.tableCreated();
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
				view.setLoading(false);
			}
		});
	}

	/**
	 * Should be Called when the create button is clicked on the dialog.
	 */
	@Override
	public void onCreateTable() {
		String tableName = view.getTableName();
		if(tableName == null || "".equals(tableName)){
			view.showError(TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		}else{
			// Create the table
			createTableEntity(tableName);
		}
	}
	
	/**
	 * Show the create modal dialog.
	 */
	@Override
	public void showCreateModal() {
		this.view.clear();
		this.view.show();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
