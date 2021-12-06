package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.Collections;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;

import com.google.inject.Inject;

/**
 * Tab that shows Datasets only.
 */
public class DatasetsTab extends AbstractTablesTab {

	public static final String DATASETS_HELP = "Create and share a collection of File versions using a Dataset.";
	public static final String DATASETS_HELP_URL = ""; // WebConstants.DOCS_URL + "Tables.2011038095.html"; // TODO

	@Inject
	public DatasetsTab(Tab tab, PortalGinInjector ginInjector) {
		super(tab, ginInjector);
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DisplayConstants.DATASETS, "dataset", DATASETS_HELP, "", EntityArea.DATASETS);
	}

	@Override
	protected EntityArea getTabArea() {
		return EntityArea.DATASETS;
	}

	@Override
	protected String getTabDisplayName() {
		return DisplayConstants.DATASETS;
	}

	@Override
	protected List<EntityType> getTypesShownInList() {
		return Collections.singletonList(EntityType.dataset);
	}

	@Override
	protected boolean isEntityShownInTab(Entity entity) {
		return entity instanceof Dataset;
	}


}
