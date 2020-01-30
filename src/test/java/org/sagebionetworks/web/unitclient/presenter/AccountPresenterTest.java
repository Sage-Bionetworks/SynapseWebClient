package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Account;
import org.sagebionetworks.web.client.presenter.AccountPresenter;
import org.sagebionetworks.web.client.view.AccountView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccountPresenterTest {

	AccountPresenter presenter;
	AccountView mockView;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	Account place;

	@Before
	public void setup() {
		mockView = mock(AccountView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new AccountPresenter(mockView, mockSynapseClient, mockGlobalApplicationState);

		verify(mockView).setPresenter(presenter);
		place = Mockito.mock(Account.class);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).addEmail(any(EmailValidationSignedToken.class), any(AsyncCallback.class));
	}

	@Test
	public void testAddEmail() {
		presenter.addEmail(new EmailValidationSignedToken());
		verify(mockSynapseClient).addEmail(any(EmailValidationSignedToken.class), any(AsyncCallback.class));
		// notify user of success
		verify(mockView).showInfo(anyString());
		// and go to the settings page
		verify(mockPlaceChanger).goTo(isA(Place.class));
	}

	@Test
	public void testAddEmailFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).addEmail(any(EmailValidationSignedToken.class), any(AsyncCallback.class));
		presenter.addEmail(new EmailValidationSignedToken());
		verify(mockSynapseClient).addEmail(any(EmailValidationSignedToken.class), any(AsyncCallback.class));

		verify(mockView).showErrorInPage(anyString(), anyString());
	}
}
