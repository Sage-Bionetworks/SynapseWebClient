package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


/**
 * First page of table/view creation wizard. Ask for the name and scope, then create the entity.
 * 
 * @author Jay
 *
 */
public class CreateTableViewWizardStep1 implements ModalPage, CreateTableViewWizardStep1View.Presenter {
	public static final String EMPTY_SCOPE_MESSAGE = "Please define the scope for this view.";
	private static final String NEXT = "Next";
	public static final String NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER = "Name must include at least one character.";

	CreateTableViewWizardStep1View view;
	SynapseJavascriptClient jsClient;
	String parentId;
	ModalPresenter modalPresenter;
	EntityContainerListWidget entityContainerList;
	TableType tableType;
	CreateTableViewWizardStep2 step2;

	@Inject
	public CreateTableViewWizardStep1(CreateTableViewWizardStep1View view, SynapseJavascriptClient jsClient, EntityContainerListWidget entityContainerList, CreateTableViewWizardStep2 step2) {
		super();
		this.view = view;
		this.step2 = step2;
		this.entityContainerList = entityContainerList;
		view.setScopeWidget(entityContainerList.asWidget());
		this.jsClient = jsClient;
		view.setPresenter(this);
	}

	@Override
	public void updateViewTypeMask() {
		tableType = TableType.getTableType(view.isFileSelected(), view.isFolderSelected(), view.isTableSelected());
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
		view.setScopeWidgetVisible(!TableType.table.equals(type));

		if (TableType.table.equals(type) || TableType.projects.equals(type)) {
			view.setViewTypeOptionsVisible(false);
		} else {
			view.setViewTypeOptionsVisible(true);
			// update the checkbox state based on the view type mask
			view.setIsFileSelected(type.isIncludeFiles());
			view.setIsFolderSelected(type.isIncludeFolders());
			view.setIsTableSelected(type.isIncludeTables());
		}

		entityContainerList.configure(new ArrayList<String>(), canEdit, type);
		view.setName("");
	}

	/**
	 * Create the Table/View
	 * 
	 * @param name
	 */
	private void createEntity(final String name) {
		modalPresenter.setLoading(true);
		Table table;
		if (TableType.table.equals(tableType)) {
			table = new TableEntity();
		} else {
			table = new EntityView();
			List<String> scopeIds = entityContainerList.getEntityIds();
			if (scopeIds.isEmpty()) {
				modalPresenter.setErrorMessage(EMPTY_SCOPE_MESSAGE);
				return;
			}
			((EntityView) table).setScopeIds(scopeIds);
			((EntityView) table).setViewTypeMask(tableType.getViewTypeMask().longValue());
		}
		table.setName(name);
		table.setParentId(parentId);
		createEntity(table);
	}

	private void createEntity(final Entity entity) {
		jsClient.createEntity(entity).addCallback(new FutureCallback<Entity>() {
			@Override
			public void onSuccess(Entity table) {
				step2.configure((Table) table, tableType);
				modalPresenter.setNextActivePage(step2);
			}

			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
		}, directExecutor());
	}

	/**
	 * Should be Called when the create button is clicked on the dialog.
	 */
	@Override
	public void onPrimary() {
		String tableName = view.getName();
		if (tableName == null || "".equals(tableName)) {
			modalPresenter.setErrorMessage(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		} else {
			createEntity(tableName);
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
