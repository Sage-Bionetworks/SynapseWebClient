package org.sagebionetworks.web.client.widget.table;

import java.util.Arrays;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget lists the tables of a given project.
 * 
 * @author John
 *
 */
public class TableListWidget implements TableListWidgetView.Presenter, PageChangeListener, TableCreatedHandler, IsWidget {
	
	public static final Long PAGE_SIZE = 20L;
	public static final Long OFFSET_ZERO = 0L;

	private PreflightController preflightController;
	private TableListWidgetView view;
	private SynapseClientAsync synapseClient;
	private PaginationWidget paginationWidget;
	private CreateTableModalWidget createTableModalWidget;
	private UploadTableModalWidget uploadTableModalWidget;
	private CreateTableViewWizard createTableViewWizard;
	private boolean canEdit;
	private EntityQuery query;
	private EntityBundle parentBundle;
	private CallbackP<String> onTableClickCallback;
	private CookieProvider cookies;
	WizardCallback refreshTablesCallback;
	@Inject
	public TableListWidget(PreflightController preflightController,
			TableListWidgetView view,
			SynapseClientAsync synapseClient,
			CreateTableModalWidget createTableModalWidget,
			PaginationWidget paginationWidget,
			UploadTableModalWidget uploadTableModalWidget,
			CookieProvider cookies,
			CreateTableViewWizard createTableViewWizard) {
		this.preflightController = preflightController;
		this.view = view;
		this.synapseClient = synapseClient;
		this.createTableModalWidget = createTableModalWidget;
		this.uploadTableModalWidget = uploadTableModalWidget;
		this.paginationWidget = paginationWidget;
		this.createTableViewWizard = createTableViewWizard;
		this.cookies = cookies;
		this.view.setPresenter(this);
		this.view.addCreateTableModal(createTableModalWidget);
		this.view.addPaginationWidget(paginationWidget);
		this.view.addUploadTableModal(uploadTableModalWidget);
		this.view.addWizard(createTableViewWizard.asWidget());
		refreshTablesCallback = new WizardCallback() {
			@Override
			public void onFinished() {
				tableCreated();
			}
			
			@Override
			public void onCanceled() {
			}
		};
	}
	
	/**
	 * Configure this widget before use.
	 * @param projectOwnerId
	 * @param canEdit
	 * @param showAddTable
	 */
	public void configure(EntityBundle parentBundle) {
		this.parentBundle = parentBundle;
		this.canEdit = parentBundle.getPermissions().getCanEdit();
		this.createTableModalWidget.configure(parentBundle.getEntity().getId(), this);
		this.uploadTableModalWidget.configure(parentBundle.getEntity().getId(), null);
		this.query = createQuery(parentBundle.getEntity().getId());
		queryForOnePage(OFFSET_ZERO);
	}

	/**
	 * Create a new query.
	 * @param parentId
	 * @return
	 */
	public EntityQuery createQuery(String parentId) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, parentId);
		Condition typeCondition = EntityQueryUtils.buildCondition(
				EntityFieldName.nodeType, Operator.IN, EntityType.table.name(), EntityType.entityview.name());
		
		newQuery.setConditions(Arrays.asList(condition, typeCondition));
		newQuery.setLimit(PAGE_SIZE);
		newQuery.setOffset(OFFSET_ZERO);
		return newQuery;
	}
	/**
	 * Run a query and populate the page with the results.
	 * @param offset The offset used by the query.
	 */
	private void queryForOnePage(final Long offset){
		this.query.setOffset(offset);
		synapseClient.executeEntityQuery(this.query, new AsyncCallback<EntityQueryResults>() {
			
			@Override
			public void onSuccess(EntityQueryResults results) {
				paginationWidget.configure(query.getLimit(), query.getOffset(), results.getTotalEntityCount(), TableListWidget.this);
				boolean showPagination = results.getTotalEntityCount() > query.getLimit();
				view.showPaginationVisible(showPagination);
				setResults(results);
			}
			
			@Override
			public void onFailure(Throwable error) {
				view.showErrorMessage(error.getMessage());
			}
		});
	}
	
	private void setResults(EntityQueryResults results) {
		view.configure(results.getEntities());
		//Must have edit and showAddTables for the buttons to be visible.
		view.setAddTableVisible(this.canEdit);
		view.setUploadTableVisible(this.canEdit);
		view.setAddFileViewVisible(this.canEdit && DisplayUtils.isInTestWebsite(cookies));
	}
    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@Override
	public void onUploadTable() {
		// This operation creates an entity and uploads data to the entity so both checks must pass.
		preflightController.checkCreateEntityAndUpload(parentBundle, TableEntity.class.getName(), new Callback() {
			@Override
			public void invoke() {
				postCheckUploadTable();
			}
		});
	}
	/**
	 * Called after a successful preflight check.
	 */
	private void postCheckUploadTable(){
		this.uploadTableModalWidget.showModal(refreshTablesCallback);
	}


	@Override
	public void onPageChange(Long newOffset) {
		queryForOnePage(newOffset);
	}
	@Override
	public void onAddFileView() {
		preflightController.checkCreateEntity(parentBundle, EntityView.class.getName(), new Callback() {
			@Override
			public void invoke() {
				postCheckCreateFileView();
			}
		});
	}
	/**
	 * Called after all pre-flight checks are performed on a file view.
	 */
	private void postCheckCreateFileView() {
		this.createTableViewWizard.configure(parentBundle.getEntity().getId(), TableType.view);
		this.createTableViewWizard.showModal(refreshTablesCallback);
	}
	
	@Override
	public void onAddTable() {
		preflightController.checkCreateEntity(parentBundle, TableEntity.class.getName(), new Callback() {
			@Override
			public void invoke() {
				postCheckCreateTable();
			}
		});

	}
	
	/**
	 * Called after all pre-flight checks are performed on a table.
	 */
	private void postCheckCreateTable(){
		// use new wizard if in alpha mode
		if (DisplayUtils.isInTestWebsite(cookies)) {
			this.createTableViewWizard.configure(parentBundle.getEntity().getId(), TableType.table);
			this.createTableViewWizard.showModal(refreshTablesCallback);
		} else {
			this.createTableModalWidget.showCreateModal();	
		}
	}
	

	@Override
	public void tableCreated() {
		// Back to page one.
		queryForOnePage(OFFSET_ZERO);
	}
	
	/**
	 * Invokes callback when a table entity is clicked in the table list. 
	 * @param callback
	 */
	public void setTableClickedCallback(CallbackP<String> callback) {
		this.onTableClickCallback = callback;
	}
	
	@Override
	public void onTableClicked(String entityId) {
		if (onTableClickCallback != null) {
			onTableClickCallback.invoke(entityId);
		}
	}
	
}
