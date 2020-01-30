package org.sagebionetworks.web.unitclient.widget.biodalliance13;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceConfigInterface;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidget;
import org.sagebionetworks.web.client.widget.biodalliance13.BiodallianceWidgetView;
import org.sagebionetworks.web.client.widget.biodalliance13.HumanBiodallianceConfig;
import org.sagebionetworks.web.client.widget.biodalliance13.MouseBiodallianceConfig;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;

public class BiodallianceWidgetTest {

	BiodallianceWidgetView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	HumanBiodallianceConfig mockHumanConfig;
	MouseBiodallianceConfig mockMouseConfig;

	BiodallianceWidget widget;
	SynapseAlert mockSynAlert;

	@Before
	public void before() {
		mockView = mock(BiodallianceWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockHumanConfig = mock(HumanBiodallianceConfig.class);
		mockMouseConfig = mock(MouseBiodallianceConfig.class);
		mockSynAlert = mock(SynapseAlert.class);
		when(mockView.isAttached()).thenReturn(false);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		widget = new BiodallianceWidget(mockView, mockAuthenticationController, mockGlobalApplicationState, mockHumanConfig, mockMouseConfig, mockSynAlert);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		assertFalse(widget.isConfigured());
	}


	@Test
	public void testConfigure() {
		// configure when the view is not yet attached.
		WikiPageKey key = null;
		Map<String, String> descriptor = new HashMap<String, String>();
		Long wikiVersionInView = null;
		Callback widgetRefreshRequired = null;
		widget.configure(key, descriptor, widgetRefreshRequired, wikiVersionInView);
		// verify that view does not try to show
		verify(mockView, times(0)).showBiodallianceBrowser(anyString(), anyString(), anyString(), anyInt(), anyInt(), any(BiodallianceConfigInterface.class), anyList());

		// now, simulate the view calling back to the present to let it know that it's been attached
		widget.viewAttached();
		// now we should have shown the genome browser
		verify(mockView).showBiodallianceBrowser(anyString(), anyString(), anyString(), anyInt(), anyInt(), any(BiodallianceConfigInterface.class), anyList());
	}

	@Test
	public void testConfigureNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		WikiPageKey key = null;
		Map<String, String> descriptor = new HashMap<String, String>();
		Long wikiVersionInView = null;
		Callback widgetRefreshRequired = null;
		widget.configure(key, descriptor, widgetRefreshRequired, wikiVersionInView);
		// verify that view does not try to show
		verify(mockSynAlert).showLogin();
	}

	@Test
	public void testConfigureIsAlreadyAttached() {
		when(mockView.isAttached()).thenReturn(true);

		WikiPageKey key = null;
		Map<String, String> descriptor = new HashMap<String, String>();
		Long wikiVersionInView = null;
		Callback widgetRefreshRequired = null;
		widget.configure(key, descriptor, widgetRefreshRequired, wikiVersionInView);
		// verify that view now tries to show, with default values (since descriptor was empty)
		verify(mockView).showBiodallianceBrowser(eq(BiodallianceWidget.PORTAL_URL_PREFIX), anyString(), // container id
				eq(BiodallianceWidget.DEFAULT_CHR), eq(BiodallianceWidget.DEFAULT_VIEW_START), eq(BiodallianceWidget.DEFAULT_VIEW_END), any(BiodallianceConfigInterface.class), anyList());
	}

	@Test
	public void testViewAttachedIsNotConfigured() {
		widget.viewAttached();

		// does not try to show, since not yet configured
		assertFalse(widget.isConfigured());
		verify(mockView, times(0)).showBiodallianceBrowser(anyString(), anyString(), anyString(), anyInt(), anyInt(), any(BiodallianceConfigInterface.class), anyList());

	}

	@Test
	public void testFileResolverURL() {
		String expectedUrl = BiodallianceWidget.FILE_RESOLVER_URL + "entityId=syn123&version=4";
		assertEquals(expectedUrl, BiodallianceWidget.getFileResolverURL("syn123", 4L));
		assertEquals(expectedUrl, BiodallianceWidget.getFileResolverURL("syn123.4"));
		assertNull(BiodallianceWidget.getFileResolverURL("syn123.4.5"));
		assertNull(BiodallianceWidget.getFileResolverURL("syn123"));
	}
}
