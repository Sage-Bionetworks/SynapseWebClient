package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.place.ErrorPlace;
import org.sagebionetworks.web.client.presenter.ErrorPresenter;
import org.sagebionetworks.web.client.view.ErrorView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class ErrorPresenterTest {
	ErrorPresenter presenter;
	@Mock
	ErrorView mockView;
	@Mock
	GWTWrapper mockGWT;

	@Before
	public void setup() {
		presenter = new ErrorPresenter(mockView, mockGWT);
	}

	@Test
	public void testSetPlace() {
		ErrorPlace place = Mockito.mock(ErrorPlace.class);
		presenter.setPlace(place);
	}

	@Test
	public void testShowError() throws RestServiceException {
		String error = "the%20error";
		String decodedError = "the error";
		when(mockGWT.decodeQueryString(error)).thenReturn(decodedError);

		presenter.setPlace(new ErrorPlace(error));

		verify(mockView).setErrorMessage(decodedError);
	}
}
