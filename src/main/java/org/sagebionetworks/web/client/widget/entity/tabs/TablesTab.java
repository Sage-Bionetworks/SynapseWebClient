package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

/**
 * Tab that shows Tables, EntityViews, and SubmissionViews.
 */
public class TablesTab extends AbstractTablesTab {

	public static final String TABLES_HELP = "Build structured queryable data that can be described by a schema using the Tables.";
	public static final String TABLES_HELP_URL = WebConstants.DOCS_URL + "Tables.2011038095.html";

	@Inject
	public TablesTab(Tab tab, PortalGinInjector ginInjector) {
		super(tab, ginInjector);
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DisplayConstants.TABLES, "table", TABLES_HELP, TABLES_HELP_URL, EntityArea.TABLES);
	}

	@Override
	protected EntityArea getTabArea() {
		return EntityArea.TABLES;
	}

	@Override
	protected String getTabDisplayName() {
		return DisplayConstants.TABLES;
	}

	@Override
	protected String getTabDescription(){
		return "";
	}

	@Override
	protected String getHelpLink(){return TABLES_HELP_URL;}

	@Override
	protected List<EntityType> getTypesShownInList() {
		List<EntityType> types = new ArrayList<>();
		types.add(EntityType.table);
		types.add(EntityType.entityview);
		types.add(EntityType.submissionview);
		types.add(EntityType.materializedview);
		return types;
	}

	@Override
	protected boolean isEntityShownInTab(Entity entity) {
		return entity instanceof Table && !(entity instanceof Dataset);
	}

}
