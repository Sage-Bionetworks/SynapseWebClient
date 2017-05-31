package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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
public class TableListWidget implements TableListWidgetView.Presenter, TableCreatedHandler, IsWidget {
	private PreflightController preflightController;
	private TableListWidgetView view;
	private SynapseClientAsync synapseClient;
	private CreateTableModalWidget createTableModalWidget;
	private UploadTableModalWidget uploadTableModalWidget;
	private CreateTableViewWizard createTableViewWizard;
	private boolean canEdit;
	private EntityChildrenRequest query;
	private EntityBundle parentBundle;
	private CallbackP<String> onTableClickCallback;
	private CookieProvider cookies;
	WizardCallback refreshTablesCallback;
	private LoadMoreWidgetContainer loadMoreWidget;
	private SynapseAlert synAlert;
	
	@Inject
	public TableListWidget(PreflightController preflightController,
			TableListWidgetView view,
			SynapseClientAsync synapseClient,
			CreateTableModalWidget createTableModalWidget,
			UploadTableModalWidget uploadTableModalWidget,
			CookieProvider cookies,
			CreateTableViewWizard createTableViewWizard,
			LoadMoreWidgetContainer loadMoreWidget, 
			SynapseAlert synAlert) {
		this.preflightController = preflightController;
		this.view = view;
		this.synapseClient = synapseClient;
		this.createTableModalWidget = createTableModalWidget;
		this.uploadTableModalWidget = uploadTableModalWidget;
		this.createTableViewWizard = createTableViewWizard;
		this.loadMoreWidget = loadMoreWidget;
		this.cookies = cookies;
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		this.view.addCreateTableModal(createTableModalWidget);
		this.view.setLoadMoreWidget(loadMoreWidget);
		this.view.addUploadTableModal(uploadTableModalWidget);
		this.view.addWizard(createTableViewWizard.asWidget());
		view.setSynAlert(synAlert);
		refreshTablesCallback = new WizardCallback() {
			@Override
			public void onFinished() {
				tableCreated();
			}
			
			@Override
			public void onCanceled() {
			}
		};
		loadMoreWidget.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
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
		view.setAddProjectViewVisible(DisplayUtils.isInTestWebsite(cookies));
		view.resetSortUI();
		loadData();
	}
	
	public void onSort(SortBy sortColumn, Direction sortDirection) {
		query.setSortBy(sortColumn);
		query.setSortDirection(sortDirection);
		query.setNextPageToken(null);
		view.clearTableWidgets();
		loadMore();
	}
	
	public void loadData() {
		query = createQuery(parentBundle.getEntity().getId());
		view.clearTableWidgets();
		query.setNextPageToken(null);
		loadMore();
	}
	/**
	 * Create a new query.
	 * @param parentId
	 * @return
	 */
	public EntityChildrenRequest createQuery(String parentId) {
		EntityChildrenRequest newQuery = new EntityChildrenRequest();
		newQuery.setSortBy(SortBy.CREATED_ON);
		newQuery.setSortDirection(Direction.DESC);
		newQuery.setParentId(parentId);
		List<EntityType> types = new ArrayList<EntityType>();
		types.add(EntityType.table);
		types.add(EntityType.entityview);
		newQuery.setIncludeTypes(types);
		return newQuery;
	}
	
	/**
	 * Run a query and populate the page with the results.
	 * @param offset The offset used by the query.
	 */
	private void loadMore(){
		synAlert.clear();
		synapseClient.getEntityChildren(query, new AsyncCallback<EntityChildrenResponse>() {
			public void onSuccess(EntityChildrenResponse result) {
				query.setNextPageToken(result.getNextPageToken());
				loadMoreWidget.setIsMore(result.getNextPageToken() != null);
				setResults(result.getPage());
			};
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	private void setResults(List<EntityHeader> results) {
		for (EntityHeader header : results) {
			view.addTableListItem(header);
		}
		//Must have edit and showAddTables for the buttons to be visible.
		view.setAddTableVisible(this.canEdit);
		view.setUploadTableVisible(this.canEdit);
		view.setAddFileViewVisible(this.canEdit);
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
		this.createTableViewWizard.configure(parentBundle.getEntity().getId(), TableType.fileview);
		this.createTableViewWizard.showModal(refreshTablesCallback);
	}
	
	@Override
	public void onAddProjectView() {
		preflightController.checkCreateEntity(parentBundle, EntityView.class.getName(), new Callback() {
			@Override
			public void invoke() {
				postCheckCreateProjectView();
			}
		});
	}
	
	/**
	 * Called after all pre-flight checks are performed on a project view.
	 */
	private void postCheckCreateProjectView() {
		this.createTableViewWizard.configure(parentBundle.getEntity().getId(), TableType.projectview);
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
		loadData();
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
