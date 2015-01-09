package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.EntityNameModalView;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A simple modal dialog for creating a TableEntity.
 * This widget is a bootstrap modal and must be added to the page. 
 * @author John
 *
 */
public class CreateTableModalWidgetImpl implements EntityNameModalView.Presenter, CreateTableModalWidget {

	public static final String BUTTON_TEXT = "Create";
	public static final String LABEL = "Table name";
	public static final String TITLE = "Create Table";

	public static final String TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "Table name must include at least one character.";
	
	EntityNameModalView view;
	SynapseClientAsync synapseClient;
	String parentId;
	TableCreatedHandler handler;
	
	@Inject
	public CreateTableModalWidgetImpl(EntityNameModalView view,
			SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.view.setPresenter(this);
		this.view.configure(TITLE, LABEL, BUTTON_TEXT, null);
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
	public void onPrimary() {
		String tableName = view.getName();
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
