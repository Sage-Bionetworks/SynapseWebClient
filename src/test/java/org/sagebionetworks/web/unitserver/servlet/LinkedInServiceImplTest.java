package org.sagebionetworks.web.unitserver.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.server.servlet.LinkedInServiceImpl;

public class LinkedInServiceImplTest {
	private static final String TEST_FIRST_NAME = "Bob";
	private static final String TEST_LAST_NAME = "Smith";
	private static final String TEST_SUMMARY = "My summary.\nWith multiple lines";
	private static final String TEST_LOCATION = "Greater Seattle Area";
	private static final String TEST_COMPANY_NAME = "MadeUp";
	private static final String MOCK_LINKEDIN_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person>  <id>rddsf8JYI9</id>  <first-name>" + TEST_FIRST_NAME + "</first-name>  <last-name>" + TEST_LAST_NAME + "</last-name>  <summary>" + TEST_SUMMARY + "</summary>  <industry>Computer Software</industry>  <location>    <name>" + TEST_LOCATION + "</name>  </location>  <positions total=\"1\">    <position>      <title>Database Wizard</title>      <is-current>true</is-current>      <company>        <name>" + TEST_COMPANY_NAME + "</name>      </company>    </position>  </positions>  <picture-url>http://m3.licdn.com/mpr/mprx/0_yq555BbiTHqTxRbrCe5m9mTo_8xZ7MYKlV5zqxYuBK8pZJr-vWIv8l7FixpxmvgcbUEt6YY3sM</picture-url></person>";

	@Test
	public void testLinkedInParsing() throws JSONObjectAdapterException {
		UserProfile userProfile = LinkedInServiceImpl.parseLinkedInResponse(MOCK_LINKEDIN_RESPONSE);
		Assert.assertEquals("First name does not match", TEST_FIRST_NAME, userProfile.getFirstName());
		Assert.assertEquals("Last name does not match", TEST_LAST_NAME, userProfile.getLastName());
		Assert.assertEquals("Summary does not match", TEST_SUMMARY, userProfile.getSummary());
		Assert.assertEquals("Location does not match", TEST_LOCATION, userProfile.getLocation());
		Assert.assertEquals("Company does not match", TEST_COMPANY_NAME, userProfile.getCompany());
	}


}
