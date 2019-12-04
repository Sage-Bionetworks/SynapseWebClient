package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.user.client.ui.Widget;

public class DiscussionTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DiscussionTabView mockView;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	ForumWidget mockForumWidget;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	SynapseProperties mockSynapseProperties;
	DiscussionTab tab;
	public static final String FORUM_SYNAPSE_ID = "syn99990";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockTab, mockPortalGinInjector);
		when(mockSynapseProperties.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY)).thenReturn(FORUM_SYNAPSE_ID);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockPortalGinInjector.getDiscussionTabView()).thenReturn(mockView);
		when(mockPortalGinInjector.getForumWidget()).thenReturn(mockForumWidget);
		when(mockPortalGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockPortalGinInjector.getSynapseProperties()).thenReturn(mockSynapseProperties);
		tab.lazyInject();
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
		tab.configure(entityId, entityName, areaToken, canModerate, mockActionMenuWidget);

		ArgumentCaptor<CallbackP> paramCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockForumWidget).configure(anyString(), any(ParameterizedToken.class), anyBoolean(), eq(mockActionMenuWidget), paramCaptor.capture(), any(Callback.class));
		// SWC-3994: verify place is initialized with area token.
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertTrue(place.getAreaToken().contains("a=b"));
		assertTrue(place.getAreaToken().contains("c=d"));

		// simulate the forum calling back to the tab with the parameter
		areaToken = "e=f&g=h";
		paramCaptor.getValue().invoke(new ParameterizedToken(areaToken));

		verify(mockTab, times(2)).setEntityNameAndPlace(eq(entityName), captor.capture());
		place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.DISCUSSION, place.getArea());
		assertTrue(place.getAreaToken().contains("e=f"));
		assertTrue(place.getAreaToken().contains("g=h"));
		verifyZeroInteractions(mockPlaceChanger);
	}

	@Test
	public void testConfigureSynapseForum() {
		String entityId = FORUM_SYNAPSE_ID;
		String entityName = "discussion project test";
		String areaToken = "a=b&c=d";
		boolean canModerate = false;
		tab.configure(entityId, entityName, areaToken, canModerate, mockActionMenuWidget);

		ArgumentCaptor<SynapseForumPlace> captor = ArgumentCaptor.forClass(SynapseForumPlace.class);
		verify(mockPlaceChanger).goTo(captor.capture());

		SynapseForumPlace place = captor.getValue();
		// params, like threadId or replyId, are passed to the SynapseForum place
		assertEquals("b", place.getParam("a"));
		assertEquals("d", place.getParam("c"));
	}


	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

	@Test
	public void testUpdateActionMenuCommands() {
		tab.updateActionMenuCommands();
		verify(mockForumWidget).updateActionMenuCommands();
	}

}
