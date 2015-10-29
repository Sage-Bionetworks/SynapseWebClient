package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TabView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;

public class TabTest {
	@Mock
	TabView mockView;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	
	Tab tab;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		tab = new Tab(mockView, mockGlobalAppState, mockSynapseJSNIUtils);
		when(mockView.isActive()).thenReturn(true);
	}
	
	@Test
	public void testConfigure() {
		//test construction
		verify(mockView).setPresenter(tab);
		//and configure
		String tabTitle = "TestTab";
		Widget content = null;
		tab.configure(tabTitle, content);
		verify(mockView).configure(tabTitle, content);
	}

	@Test
	public void testSetEntityNameAndPlace() {
		//verify page title is set during this process
		//note: tab view is configured to reply that tab is active
		String entityName = "one project to rule them all";
		String entityId = "syn123";
		Synapse place = new Synapse(entityId);
		tab.setEntityNameAndPlace(entityName, place);
		verify(mockSynapseJSNIUtils).setPageTitle(entityName + " - " + entityId);
	}

	@Test
	public void testShowTab() {
		tab.setEntityName("entity name");
		tab.showTab();
		verify(mockGlobalAppState).pushCurrentPlace(any(Place.class));
		verify(mockView).setActive(true);
		//verify showing tab also attempts to update the page title
		verify(mockSynapseJSNIUtils).setPageTitle(anyString());
	}
	
	@Test
	public void testSetEntityNameAndPlaceNotActive() {
		when(mockView.isActive()).thenReturn(false);
		//verify page title is not set during this process (if tab is not active)
		String entityName = "one project to rule them all";
		String entityId = "syn123";
		Synapse place = new Synapse(entityId);
		tab.setEntityNameAndPlace(entityName, place);
		verify(mockSynapseJSNIUtils, never()).setPageTitle(anyString());
	}
	
}
