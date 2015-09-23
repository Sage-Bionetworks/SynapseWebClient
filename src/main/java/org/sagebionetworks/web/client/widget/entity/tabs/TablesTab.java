package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;

import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter{
	Tab tab;
	TablesTabView view;
	TableListWidget tableListWidget;
	BasicTitleBar tableTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	TableEntityWidget v2TableWidget;
	EntityActionController controller;
	ActionMenuWidget actionMenu;
	boolean annotationsShown;
	
	@Inject
	public TablesTab(
			TablesTabView view,
			Tab tab,
			TableEntityWidget v2TableWidget,
			TableListWidget tableListWidget,
			BasicTitleBar tableTitleBar,
			Breadcrumb breadcrumb,
			EntityMetadata metadata,
			EntityActionController controller,
			ActionMenuWidget actionMenu
			) {
		this.view = view;
		this.tab = tab;
		this.v2TableWidget = v2TableWidget;
		this.tableListWidget = tableListWidget;
		this.tableTitleBar = tableTitleBar;
		this.breadcrumb = breadcrumb;
		this.metadata = metadata;
		this.controller = controller;
		this.actionMenu = actionMenu;
		
		view.setBreadcrumb(breadcrumb.asWidget());
		view.setTableList(tableListWidget.asWidget());
		view.setTitlebar(tableTitleBar.asWidget());
		view.setEntityMetadata(metadata.asWidget());
		view.setTableEntityWidget(v2TableWidget.asWidget());
		view.setActionMenu(actionMenu.asWidget());
		tab.configure("Tables", view.asWidget());
		

		actionMenu.addControllerWidget(controller.asWidget());
		
		annotationsShown = false;
		actionMenu.addActionListener(Action.TOGGLE_ANNOTATIONS, new ActionListener() {
			@Override
			public void onAction(Action action) {
				annotationsShown = !annotationsShown;
				TablesTab.this.controller.onAnnotationsToggled(annotationsShown);
				TablesTab.this.metadata.setAnnotationsVisible(annotationsShown);
			}
		});
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void configure(EntityBundle bundle, final EntityUpdatedHandler handler, QueryChangeHandler qch) {
		Entity entity = bundle.getEntity();
		boolean isTable = entity instanceof TableEntity;
		
		breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
		Long versionNumber = null;
		if (isTable) {
			versionNumber = ((TableEntity)entity).getVersionNumber();
		}
		metadata.setEntityBundle(bundle, versionNumber);
		tableTitleBar.configure(bundle);
		tab.setPlace(new Synapse(entity.getId(), versionNumber, EntityArea.TABLES, null));
		v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), qch, actionMenu);
		view.configureModifiedAndCreatedWidget(entity);
		
		metadata.setEntityUpdatedHandler(handler);
	}
	
	public Tab asTab(){
		return tab;
	}
}
