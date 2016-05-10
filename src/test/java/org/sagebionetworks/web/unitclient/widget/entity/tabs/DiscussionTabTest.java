package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DiscussionTabView mockView;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CookieProvider mockCookies;
	@Mock
	ForumWidget mockForumWidget;
	
	DiscussionTab tab;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockView, mockTab, mockCookies, mockForumWidget);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(tab);
		verify(mockView).setForum(any(Widget.class));
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigure() {
		String entityId = "syn1"; 
		String entityName = "discussion project test";
		String areaToken = "a=b&c=d";
		boolean canModerate = false;
		tab.configure(entityId, entityName, areaToken, canModerate);

		ArgumentCaptor<CallbackP> paramCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockForumWidget).configure(anyString(), any(ParameterizedToken.class), anyBoolean(), paramCaptor.capture(), any(Callback.class));
		
		//simulate the forum calling back to the tab with the parameter
		paramCaptor.getValue().invoke(new ParameterizedToken(areaToken));
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertTrue(place.getAreaToken().contains("a=b"));
		assertTrue(place.getAreaToken().contains("c=d"));

	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}
}
