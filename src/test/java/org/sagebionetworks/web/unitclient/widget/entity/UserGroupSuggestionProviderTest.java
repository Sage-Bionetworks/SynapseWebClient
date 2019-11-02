package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestionBundle;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserGroupSuggestionProviderTest {

	UserGroupSuggestionProvider presenter;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	AsyncCallback<SynapseSuggestionBundle> mockCallback;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Mock
	PublicPrincipalIds mockPublicPrincipalIds;
	int offset = 0;
	int pageSize = 10;
	int width = 568;
	String prefix = "test";
	public static final Long ANONYMOUS_USER_ID = 11L;
	public static final Long AUTHENTICATED_USERS_ID = 12L;
	public static final Long PUBLIC_ACL_ID = 13L;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockSynapseProperties.getPublicPrincipalIds()).thenReturn(mockPublicPrincipalIds);
		when(mockPublicPrincipalIds.getAnonymousUserPrincipalId()).thenReturn(ANONYMOUS_USER_ID);
		when(mockPublicPrincipalIds.getAuthenticatedAclPrincipalId()).thenReturn(AUTHENTICATED_USERS_ID);
		when(mockPublicPrincipalIds.getPublicAclPrincipalId()).thenReturn(PUBLIC_ACL_ID);
		presenter = new UserGroupSuggestionProvider(mockSynapseJavascriptClient, mockSynapseProperties);
	}

	@Test
	public void testGetSuggestions() {
		ArgumentCaptor<SynapseSuggestionBundle> captor = ArgumentCaptor.forClass(SynapseSuggestionBundle.class);
		UserGroupHeaderResponsePage testPage = getResponsePage();
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), any(TypeFilter.class), anyLong(), anyLong(), any(AsyncCallback.class));
		presenter.getSuggestions(TypeFilter.TEAMS_ONLY, offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), eq(TypeFilter.TEAMS_ONLY), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(captor.capture());
		SynapseSuggestionBundle testBundle = captor.getValue();
		assertEquals(testBundle.getTotalNumberOfResults(), 6);
		assertEquals(testBundle.getSuggestionBundle().size(), 2);
	}

	@Test
	public void testGetSuggestionsFailure() {
		Exception caught = new Exception("this is an exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), any(TypeFilter.class), anyLong(), anyLong(), any(AsyncCallback.class));
		presenter.getSuggestions(TypeFilter.ALL, offset, pageSize, width, prefix, mockCallback);
		verify(mockSynapseJavascriptClient).getUserGroupHeadersByPrefix(anyString(), any(TypeFilter.class), anyLong(), anyLong(), any(AsyncCallback.class));
		verify(mockCallback).onFailure(caught);
	}

	private UserGroupHeaderResponsePage getResponsePage() {
		UserGroupHeaderResponsePage testPage = new UserGroupHeaderResponsePage();
		testPage.setPrefixFilter("test");

		UserGroupHeader head1 = new UserGroupHeader();
		head1.setFirstName("Test");
		head1.setLastName("One");

		UserGroupHeader head2 = new UserGroupHeader();
		head1.setFirstName("Test");
		head1.setLastName("Two");

		UserGroupHeader head3 = new UserGroupHeader();
		head3.setOwnerId(ANONYMOUS_USER_ID.toString());
		head3.setFirstName("Anonymous");

		UserGroupHeader head4 = new UserGroupHeader();
		head4.setOwnerId(AUTHENTICATED_USERS_ID.toString());
		head4.setFirstName("Authenticated Users");

		UserGroupHeader head5 = new UserGroupHeader();
		head5.setOwnerId(PUBLIC_ACL_ID.toString());
		head5.setFirstName("Public");


		List<UserGroupHeader> children = new ArrayList<UserGroupHeader>();
		children.add(head1);
		children.add(head2);
		children.add(head3);
		children.add(head4);
		children.add(head5);

		testPage.setChildren(children);

		testPage.setTotalNumberOfResults((long) 6);

		return testPage;
	}

}
