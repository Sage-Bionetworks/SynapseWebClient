package org.sagebionetworks.web.client.widget.table.v2.schema;

import java.util.Objects;
import org.sagebionetworks.repo.model.table.FacetColumnSortConfig;
import org.sagebionetworks.repo.model.table.FacetColumnSortDirection;
import org.sagebionetworks.repo.model.table.FacetColumnSortProperty;

/**
 * Mapping of a facet sort config to a view display enum
 *
 * @author Jay
 *
 */
public enum FacetSortConfigViewEnum {
  None(null, null, ""),
  FrequencyDescending(
    FacetColumnSortProperty.FREQUENCY,
    FacetColumnSortDirection.DESC,
    "Frequency Descending"
  ),
  FrequencyAscending(
    FacetColumnSortProperty.FREQUENCY,
    FacetColumnSortDirection.ASC,
    "Frequency Ascending"
  ),
  ValueDescending(
    FacetColumnSortProperty.VALUE,
    FacetColumnSortDirection.DESC,
    "Value Descending"
  ),
  ValueAscending(
    FacetColumnSortProperty.VALUE,
    FacetColumnSortDirection.ASC,
    "Value Ascending"
  );

  private FacetColumnSortProperty property;
  private FacetColumnSortDirection direction;
  private String friendlyDisplay;

  FacetSortConfigViewEnum(
    FacetColumnSortProperty property,
    FacetColumnSortDirection direction,
    String friendlyDisplay
  ) {
    this.property = property;
    this.direction = direction;
    this.friendlyDisplay = friendlyDisplay;
  }

  public FacetColumnSortDirection getDirection() {
    return direction;
  }

  public FacetColumnSortProperty getProperty() {
    return property;
  }

  /**
   * Lookup the view for a facet column sort configuration.
   *
   * @param config the facet column sort configuration
   * @return the corresponding FacetSortConfigViewEnum for the given configuration,
   *         or FacetSortConfigViewEnum.None if the configuration is null
   */
  public static FacetSortConfigViewEnum getViewForConfig(
    FacetColumnSortConfig config
  ) {
    if (config == null) {
      return FacetSortConfigViewEnum.None;
    }
    for (FacetSortConfigViewEnum view : FacetSortConfigViewEnum.values()) {
      if (
        Objects.equals(config.getDirection(), view.getDirection()) &&
        Objects.equals(config.getProperty(), view.getProperty())
      ) {
        return view;
      }
    }
    throw new IllegalArgumentException(
      "Unknown config: " + config.getProperty() + ", " + config.getDirection()
    );
  }

  @Override
  public String toString() {
    return friendlyDisplay;
  }
}
