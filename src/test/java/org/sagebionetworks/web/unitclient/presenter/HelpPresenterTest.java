package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.HelpPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class HelpPresenterTest {
	
	HelpPresenter presenter;
	HelpView mockView;
	SynapseClientAsync mockSynapseClient;
	Help place;
	WikiPageKey userGuideKey;
	
	@Before
	public void setup(){
		mockView = mock(HelpView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new HelpPresenter(mockView, mockSynapseClient);

		verify(mockView).setPresenter(presenter);
		place = Mockito.mock(Help.class);
		when(place.toToken()).thenReturn(WebConstants.GETTING_STARTED);
		HashMap<String, WikiPageKey> pageName2WikiKeyMap = new HashMap<String, WikiPageKey>();
		String userGuideEntity = "syn1113";
		String userGuideWiki = "44442";
		userGuideKey = new WikiPageKey(userGuideEntity, ObjectType.ENTITY.toString(), userGuideWiki);
		pageName2WikiKeyMap.put(WebConstants.GETTING_STARTED, userGuideKey);
		
		AsyncMockStubber.callSuccessWith(pageName2WikiKeyMap).when(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
	}	
	
	@Test
	public void testSetPlaceMapInit() {
		presenter.setPlace(place);
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		verify(mockView).showHelpPage(eq(userGuideKey));
		
		//if we go there again, it should not call the synapse client again (map has been initialized)
		presenter.setPlace(place);
		//verify this was only called once (above)
		verify(mockSynapseClient, times(1)).getHelpPages(any(AsyncCallback.class));
		//but we are showing the page from the pre-initialized map now
		verify(mockView, times(2)).showHelpPage(eq(userGuideKey));
	}	
	
	@Test
	public void testSetPlaceInitFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		presenter.setPlace(place);
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}	
}
