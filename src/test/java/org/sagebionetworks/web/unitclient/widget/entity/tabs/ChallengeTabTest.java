package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;

import com.google.gwt.user.client.ui.Widget;

public class ChallengeTabTest {
	@Mock
	Tab mockTab;
	@Mock
	ChallengeTabView mockView;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	AdministerEvaluationsList mockAdministerEvaluationsList;
	
	ChallengeTab tab;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new ChallengeTab(mockView, mockTab, mockAdministerEvaluationsList);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setEvaluationList(any(Widget.class));
	}
	
	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigure() {
		String entityId = "syn1"; 
		String entityName = "challenge project test";
		tab.configure(entityId, entityName);
		ArgumentCaptor<CallbackP> callbackCaptor = ArgumentCaptor.forClass(CallbackP.class);
		
		verify(mockAdministerEvaluationsList).configure(eq(entityId), callbackCaptor.capture());
		
		verify(mockTab).setTabListItemVisible(false);
		verify(mockTab, never()).setTabListItemVisible(true);
		callbackCaptor.getValue().invoke(true);
		verify(mockTab).setTabListItemVisible(true);
		
		ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
		verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
		Synapse place = captor.getValue();
		assertEquals(entityId, place.getEntityId());
		assertNull(place.getVersionNumber());
		assertEquals(EntityArea.ADMIN, place.getArea());
		assertNull(place.getAreaToken());
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

}
