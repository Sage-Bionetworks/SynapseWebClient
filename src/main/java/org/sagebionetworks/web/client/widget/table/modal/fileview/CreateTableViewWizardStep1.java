package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of table/view creation wizard.  Ask for the name and scope, then create the entity.
 * @author Jay
 *
 */
public class CreateTableViewWizardStep1 implements ModalPage {
	private static final String NEXT = "Next";

	public static final String NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "Name must include at least one character.";
	
	CreateTableViewWizardStep1View view;
	SynapseClientAsync synapseClient;
	String parentId;
	ModalPresenter modalPresenter;
	EntityContainerListWidget entityContainerList;
	TableType tableType;
	CreateTableViewWizardStep2 step2;
	
	@Inject
	public CreateTableViewWizardStep1(
			CreateTableViewWizardStep1View view,
			SynapseClientAsync synapseClient, 
			EntityContainerListWidget entityContainerList,
			CreateTableViewWizardStep2 step2) {
		super();
		this.view = view;
		this.step2 = step2;
		this.entityContainerList = entityContainerList;
		view.setScopeWidget(entityContainerList.asWidget());
		this.synapseClient = synapseClient;
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 */
	public void configure(String parentId, TableType type) {
		this.parentId = parentId;
		this.tableType = type;
		boolean canEdit = true;
		view.setScopeWidgetVisible(TableType.view.equals(type));
		entityContainerList.configure(new ArrayList<String>(), canEdit);
		view.setName("");
	}
	
	/**
	 * Create the file view.
	 * @param name
	 */
	private void createFileViewEntity(final String name) {
		modalPresenter.setLoading(true);
		Table table;
		if (TableType.view.equals(tableType)) {
			table = new EntityView();
			List<String> scopeIds = entityContainerList.getEntityIds();
			((EntityView)table).setScopeIds(scopeIds);
			((EntityView)table).setType(ViewType.file);
		} else {
			table = new TableEntity();
		}
		table.setName(name);
		table.setParentId(parentId);
		synapseClient.createEntity(table, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity table) {
				step2.configure((Table)table, tableType);
				modalPresenter.setNextActivePage(step2);
			}
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
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
			modalPresenter.setErrorMessage(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		}else{
			// Create the table
			createFileViewEntity(tableName);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(NEXT);
	}


}
