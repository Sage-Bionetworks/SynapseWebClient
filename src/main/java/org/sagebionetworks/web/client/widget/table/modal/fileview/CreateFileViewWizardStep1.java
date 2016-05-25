package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * First page of file view creation wizard.  Ask for the name and scope, then create the entity.
 * @author Jay
 *
 */
public class CreateFileViewWizardStep1 implements ModalPage {
	public static final String TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "File view name must include at least one character.";
	
	CreateFileViewWizardStep1View view;
	SynapseClientAsync synapseClient;
	String parentId;
	ModalPresenter modalPresenter;
	EntityContainerListWidget entityContainerList;
	
	@Inject
	public CreateFileViewWizardStep1(
			CreateFileViewWizardStep1View view,
			SynapseClientAsync synapseClient, 
			EntityContainerListWidget entityContainerList) {
		super();
		this.view = view;
		this.entityContainerList = entityContainerList;
		view.setScopeWidget(entityContainerList.asWidget());
		this.synapseClient = synapseClient;
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 * @param parentId
	 */
	public void configure(String parentId ){
		this.parentId = parentId;
		boolean canEdit = true;
		entityContainerList.configure(new ArrayList<String>(), canEdit);
	}
	
	/**
	 * Create the file view.
	 * @param name
	 */
	private void createFileViewEntity(final String name) {
		modalPresenter.setLoading(true);
		FileView table = new FileView();
		table.setName(name);
		table.setParentId(parentId);
		table.setEntityType(FileView.class.getName());
		List<String> scopeIds = entityContainerList.getEntityIds();
		table.setScopeIds(scopeIds);
		synapseClient.createEntity(table, new AsyncCallback<Entity>() {
			@Override
			public void onSuccess(Entity table) {
				modalPresenter.onFinished();
				//TODO: add other pages
//				modalPresenter.setNextActivePage(next);
			}
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
				modalPresenter.setLoading(false);
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
			modalPresenter.setErrorMessage(TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
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
	}


}
