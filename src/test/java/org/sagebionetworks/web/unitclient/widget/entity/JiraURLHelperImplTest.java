package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.JiraGovernanceConstants;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;

/**
 * 
 * We just tests that the URLs are well formed
 * 
 * @author brucehoff
 *
 */
public class JiraURLHelperImplTest {
	
	
	private JiraURLHelperImpl helper = null;
	
	@Before
	public void before() throws Exception {
		helper = new JiraURLHelperImpl(new JiraGovernanceConstants() {
			@Override
			public int jiraProjectId() {
				return 0;
			}
			@Override
			public String confluenceEndpoint() {
				return "http://foo.bar.bas";
			}
			@Override
			public int flagIssueType() {
				return 0;
			}
			@Override
			public int accessRestrictionType() {
				return 0;
			}
			@Override
			public int accessRequestIssueType() {
				return 0;
			}
			@Override
			public String defaultIssueReporter() {
				return "me";
			}
			@Override
			public String issueFieldPrincipalId() {
				return "100";
			}
			@Override
			public String issueFieldUserDisplayName() {
				return "400";
			}
			@Override
			public String issueFieldDataObjectId() {
				return "800";
			}
			@Override
			public String issueFieldUserEmail() {
				return "900";
			}
			@Override
			public String issueFieldAccessRequirementObjectId() {
				return "300";
			}
			@Override
			public int majorPriority() {
				return 10;
			}
		});
	}

	@Test
	public void testCreateFlagIssue() throws MalformedURLException {
		String urlString = helper.createFlagIssue("me@place.domain", "me me", "syn123");
		URL url = new URL(urlString); // this will check that the url is well formed
	}

	@Test
	public void testCreateAccessRestrictionIssue() throws MalformedURLException {
		String urlString = helper.createAccessRestrictionIssue("me@place.domain", "me me", "syn123");
		URL url = new URL(urlString); // this will check that the url is well formed
	}

	@Test
	public void testCreateRequestAccessIssue() throws MalformedURLException {
		String urlString = helper.createRequestAccessIssue("101", "me me", "me@place.domain", 
				"syn123", "432");
		URL url = new URL(urlString); // this will check that the url is well formed
	}

}
