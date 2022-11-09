package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sagebionetworks.web.client.ValidationUtils;

public class ValidationUtilsTest {

  @Test
  public void testValidWidgetName() {
    assertTrue(ValidationUtils.isValidWidgetName("a widget name"));
    assertTrue(
      ValidationUtils.isValidWidgetName(
        "special characters allowed (-+) and 01239"
      )
    );

    assertFalse(
      ValidationUtils.isValidWidgetName("special characters disallowed like *$")
    );
    assertFalse(ValidationUtils.isValidWidgetName(null));
    assertFalse(ValidationUtils.isValidWidgetName(""));
  }

  @Test
  public void testIsValidUrl() {
    assertTrue(
      ValidationUtils.isValidUrl(
        "https://www.youtube.com/watch?v=m86ae_e_ptU",
        false
      )
    );
    assertTrue(ValidationUtils.isValidUrl("http://www.google.com", false));
    assertTrue(ValidationUtils.isValidUrl("#!Synapse:syn123", false));
    assertTrue(
      ValidationUtils.isValidUrl(
        "https://adknowledgeportal.synapse.org/Explore/Data?QueryWrapper0=%7B%22sql%22%3A%22SELECT%20*%20FROM%20syn11346063%22%2C%22limit%22%3A25%2C%22offset%22%3A0%2C%22selectedFacets%22%3A%5B%7B%22concreteType%22%3A%22org.sagebionetworks.repo.model.table.FacetColumnValuesRequest%22%2C%22columnName%22%3A%22dataType%22%2C%22facetValues%22%3A%5B%22chromatinActivity%22%5D%7D%5D%7D",
        false
      )
    );

    assertFalse(ValidationUtils.isValidUrl("http:/www.google.com", false));
    assertFalse(ValidationUtils.isValidUrl("missingprotocol.com", false));

    // undefined url handling
    assertTrue(ValidationUtils.isValidUrl("", true));
    assertFalse(ValidationUtils.isValidUrl("", false));

    assertTrue(ValidationUtils.isValidUrl(null, true));
    assertFalse(ValidationUtils.isValidUrl(null, false));
  }

  @Test
  public void testIsValidEmail() {
    assertTrue(ValidationUtils.isValidEmail("test@testing.com"));
    assertTrue(ValidationUtils.isValidEmail("userNAME+123@mail.photography"));

    assertFalse(ValidationUtils.isValidEmail("not-a-valid-email"));
    assertFalse(ValidationUtils.isValidEmail("Also@NotA@ValidEmail"));
  }
}
