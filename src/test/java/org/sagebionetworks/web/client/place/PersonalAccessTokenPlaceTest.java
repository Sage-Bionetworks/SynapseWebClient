package org.sagebionetworks.web.client.place;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersonalAccessTokenPlaceTest {

  PersonalAccessTokenPlace.Tokenizer tokenizer =
    new PersonalAccessTokenPlace.Tokenizer();
  String testToken;

  @Before
  public void setup() {
    testToken = "0";
  }

  @Test
  public void testToToken() {
    String testToken = this.testToken;
    PersonalAccessTokenPlace place = tokenizer.getPlace(testToken);

    Assert.assertEquals(this.testToken, place.toToken());
    Assert.assertEquals(testToken, tokenizer.getToken(place));
  }
}
