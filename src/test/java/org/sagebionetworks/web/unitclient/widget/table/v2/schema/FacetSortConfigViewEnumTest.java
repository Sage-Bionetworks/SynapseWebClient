package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.sagebionetworks.repo.model.table.FacetColumnSortConfig;
import org.sagebionetworks.repo.model.table.FacetColumnSortDirection;
import org.sagebionetworks.repo.model.table.FacetColumnSortProperty;
import org.sagebionetworks.web.client.widget.table.v2.schema.FacetSortConfigViewEnum;

public class FacetSortConfigViewEnumTest {

  /**
   * Test that all combinations of properties and directions are supported.
   */
  @Test
  public void testAllPropertyDirectionCombinations() {
    for (FacetColumnSortProperty property : FacetColumnSortProperty.values()) {
      for (FacetColumnSortDirection direction : FacetColumnSortDirection.values()) {
        FacetColumnSortConfig config = new FacetColumnSortConfig();
        config.setProperty(property);
        config.setDirection(direction);
        assertNotNull(FacetSortConfigViewEnum.getViewForConfig(config));
      }
    }
  }

  /**
   * Test that null config returns None.
   */
  @Test
  public void testNullConfig() {
    assertEquals(
      FacetSortConfigViewEnum.None,
      FacetSortConfigViewEnum.getViewForConfig(null)
    );
  }

  /**
   * Test frequency descending mapping.
   */
  @Test
  public void testFrequencyDescending() {
    FacetColumnSortConfig config = new FacetColumnSortConfig();
    config.setProperty(FacetColumnSortProperty.FREQUENCY);
    config.setDirection(FacetColumnSortDirection.DESC);
    assertEquals(
      FacetSortConfigViewEnum.FrequencyDescending,
      FacetSortConfigViewEnum.getViewForConfig(config)
    );
  }

  /**
   * Test frequency ascending mapping.
   */
  @Test
  public void testFrequencyAscending() {
    FacetColumnSortConfig config = new FacetColumnSortConfig();
    config.setProperty(FacetColumnSortProperty.FREQUENCY);
    config.setDirection(FacetColumnSortDirection.ASC);
    assertEquals(
      FacetSortConfigViewEnum.FrequencyAscending,
      FacetSortConfigViewEnum.getViewForConfig(config)
    );
  }

  /**
   * Test value descending mapping.
   */
  @Test
  public void testValueDescending() {
    FacetColumnSortConfig config = new FacetColumnSortConfig();
    config.setProperty(FacetColumnSortProperty.VALUE);
    config.setDirection(FacetColumnSortDirection.DESC);
    assertEquals(
      FacetSortConfigViewEnum.ValueDescending,
      FacetSortConfigViewEnum.getViewForConfig(config)
    );
  }

  /**
   * Test value ascending mapping.
   */
  @Test
  public void testValueAscending() {
    FacetColumnSortConfig config = new FacetColumnSortConfig();
    config.setProperty(FacetColumnSortProperty.VALUE);
    config.setDirection(FacetColumnSortDirection.ASC);
    assertEquals(
      FacetSortConfigViewEnum.ValueAscending,
      FacetSortConfigViewEnum.getViewForConfig(config)
    );
  }

  /**
   * Test that all enum values have a string representation.
   */
  @Test
  public void testToString() {
    for (FacetSortConfigViewEnum view : FacetSortConfigViewEnum.values()) {
      assertNotNull(view.toString());
    }
  }
}
