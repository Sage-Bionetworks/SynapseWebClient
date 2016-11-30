package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
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
	
	DiscussionTab tab;
	public static final String FORUM_SYNAPSE_ID = "syn99990";
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DiscussionTab(mockTab, mockPortalGinInjector);
		when(mockGlobalApplicationState.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY)).thenReturn(FORUM_SYNAPSE_ID);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockPortalGinInjector.getDiscussionTabView()).thenReturn(mockView);
		when(mockPortalGinInjector.getForumWidget()).thenReturn(mockForumWidget);
		when(mockPortalGinInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
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
		verifyZeroInteractions(mockPlaceChanger);
	}
	@Test
	public void testConfigureSynapseForum() {
		String entityId = FORUM_SYNAPSE_ID; 
		String entityName = "discussion project test";
		String areaToken = "a=b&c=d";
		boolean canModerate = false;
		tab.configure(entityId, entityName, areaToken, canModerate);
		
		ArgumentCaptor<SynapseForumPlace> captor = ArgumentCaptor.forClass(SynapseForumPlace.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		
		SynapseForumPlace place = captor.getValue();
		//params, like threadId or replyId, are passed to the SynapseForum place
		assertEquals("b", place.getParam("a"));
		assertEquals("d", place.getParam("c"));
	}
	

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}
}
