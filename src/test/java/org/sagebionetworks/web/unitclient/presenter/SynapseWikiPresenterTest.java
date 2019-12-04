package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.presenter.SynapseWikiPresenter;
import org.sagebionetworks.web.client.view.SynapseWikiView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SynapseWikiPresenterTest {

	SynapseWikiPresenter presenter;
	SynapseWikiView mockView;
	SynapseClientAsync mockSynapseClient;
	Wiki testPlace;

	@Before
	public void setup() {
		mockView = mock(SynapseWikiView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new SynapseWikiPresenter(mockView, mockSynapseClient);
		verify(mockView).setPresenter(presenter);

		testPlace = Mockito.mock(Wiki.class);
		when(testPlace.getOwnerId()).thenReturn("syn123");
		when(testPlace.getOwnerType()).thenReturn(ObjectType.ENTITY.toString());
	}

	@Test
	public void testSetPlace() {
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockView).showPage(any(WikiPageKey.class), anyBoolean());
	}

	@Test
	public void testSetPlaceFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(anyString());
	}
}
