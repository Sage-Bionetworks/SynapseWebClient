package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Tab that shows Datasets only.
 */
public class DatasetsTab extends AbstractTablesTab {

  public static final String DATASETS_HELP =
    "Create a Draft Dataset and add File versions. Annotate, Mint DOI’s, and Publish your Dataset to share it with others.";

  public static final String DATASETS_AND_COLLECTIONS_HELP =
    "Use Datasets to produce and distribute an immutable set of files found across one or more Projects or Folders. You can also create Dataset Collections which contain multiple Datasets.";
  public static final String DATASETS_HELP_URL =
    WebConstants.DOCS_URL + "Datasets.2611281979.html";
  public static final String DATASETS_DESCRIPTION =
    "Use Datasets to produce and distribute an immutable set of files found across one or more Projects or Folders. ";

  @Inject
  public DatasetsTab(Tab tab, PortalGinInjector ginInjector) {
    super(tab, ginInjector);
    this.tab = tab;
    this.ginInjector = ginInjector;
    String help = DisplayUtils.isInTestWebsite(ginInjector.getCookieProvider())
      ? DATASETS_AND_COLLECTIONS_HELP
      : DATASETS_HELP;
    tab.configure(
      DisplayConstants.DATASETS,
      "dataset",
      help,
      DATASETS_HELP_URL,
      EntityArea.DATASETS
    );
    tab.configureOrientationBanner(
      "Datasets",
      "Getting Started With Datasets",
      "Use Datasets to produce and distribute an immutable set of files found across one or more Projects or Folders.",
      null,
      null,
      "Learn More About Datasets",
      "https://help.synapse.org/docs/Datasets.2611281979.html"
    );
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
  protected String getTabDescription() {
    return DATASETS_DESCRIPTION;
  }

  @Override
  protected String getHelpLink() {
    return DATASETS_HELP_URL;
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
