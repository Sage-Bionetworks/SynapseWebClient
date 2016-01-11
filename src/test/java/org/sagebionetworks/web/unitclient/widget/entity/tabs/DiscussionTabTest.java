package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewThreadModal;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;

import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DiscussionTabView mockView;
	@Mock
	ThreadListWidget mockDiscussionListWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	NewThreadModal mockNewThreadModal;
	@Mock
	CookieProvider mockCookies;

	DiscussionTab tab;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockView, mockTab, mockDiscussionListWidget, mockNewThreadModal, mockCookies);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("not null");
	}

	@Test
	public void testConstruction() {
		verify(mockTab).configure(anyString(), any(Widget.class));
		verify(mockView).setDiscussionList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
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
		tab.configure(entityId, entityName);

		verify(mockTab).setTabListItemVisible(true);

		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertNull(place.getAreaToken());
	}

	@Test
	public void testNotInTestWebsite() {
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn(null);
		String entityId = "syn1"; 
		String entityName = "discussion project test";
		tab.configure(entityId, entityName);
		verify(mockTab).setTabListItemVisible(false);
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

	@Test
	public void onCLickNewThreadTest() {
		tab.onClickNewThread();
		verify(mockNewThreadModal).show();;
	}
}
