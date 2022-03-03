package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.VersionHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableTitleBar implements SynapseWidgetPresenter, TableTitleBarView.Presenter {

	private static final String DRAFT = "Draft";
	private static final String CURRENT = "Current";
	private static final String SNAPSHOT = "Snapshot";
	private static final String STABLE = "Stable";

	private TableTitleBarView view;
	private EntityBundle entityBundle;
	private ActionMenuWidget actionMenuWidget;
	private VersionHistoryWidget versionHistoryWidget;

	@Inject
	public TableTitleBar(TableTitleBarView view) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(EntityBundle bundle, ActionMenuWidget actionMenu, VersionHistoryWidget versionHistoryWidget) {
		this.entityBundle = bundle;
		this.actionMenuWidget = actionMenu;
		this.versionHistoryWidget = versionHistoryWidget;

		view.createTitlebar(bundle.getEntity());
		view.setEntityName(bundle.getEntity().getName());
		boolean isMaterializedView = bundle.getEntity() instanceof MaterializedView;
		view.setVersionUIVisible(!isMaterializedView);
		getLatestVersion();
	}

	public void getLatestVersion() {
		// determine if we should report the version as "Current"
		if (((Table) entityBundle.getEntity()).getIsLatestVersion()) {
			if (entityBundle.getEntity() instanceof Dataset) {
				view.setVersionLabel(DRAFT);
			} else {
				view.setVersionLabel(CURRENT);
			}
		} else {
			Long versionNumber = ((Table) entityBundle.getEntity()).getVersionNumber();
			if (entityBundle.getEntity() instanceof Dataset) {
				view.setVersionLabel(versionNumber.toString() + " (" + STABLE + ")");
			} else {
				view.setVersionLabel(versionNumber.toString() + " (" + SNAPSHOT + ")");
			}
		}
	}


	/**
	 * For unit testing. call asWidget with the new Entity for the view to be in sync.
	 * 
	 * @param bundle
	 */
	public void setEntityBundle(EntityBundle bundle) {
		this.entityBundle = bundle;
	}

	public void clearState() {
		view.clear();
		this.entityBundle = null;
	}

	/**
	 * Does nothing. Use asWidget(Entity)
	 */
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void toggleShowVersionHistory() {
		this.actionMenuWidget.onAction(Action.SHOW_VERSION_HISTORY);
	}

	@Override
	public boolean isVersionHistoryVisible() {
		return this.versionHistoryWidget.isVisible();
	}
}
