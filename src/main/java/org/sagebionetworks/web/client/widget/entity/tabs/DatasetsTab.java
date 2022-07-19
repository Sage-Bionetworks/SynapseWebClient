package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

/**
 * Tab that shows Datasets only.
 */
public class DatasetsTab extends AbstractTablesTab {

	public static final String DATASETS_HELP = "Create a Draft Dataset and add File versions. Annotate, Mint DOI’s, and Publish your Dataset to share it with others.";
	public static final String DATASETS_HELP_URL = WebConstants.DOCS_URL + "Datasets.2611281979.html";

	@Inject
	public DatasetsTab(Tab tab, PortalGinInjector ginInjector) {
		super(tab, ginInjector);
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure(DisplayConstants.DATASETS, "dataset", DATASETS_HELP, DATASETS_HELP_URL, EntityArea.DATASETS);
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
		return Arrays.asList(EntityType.dataset, EntityType.datasetcollection);
	}

	@Override
	protected boolean isEntityShownInTab(Entity entity) {
		return entity instanceof EntityRefCollectionView;
	}


}
