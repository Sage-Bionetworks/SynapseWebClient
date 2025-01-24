package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;

@RunWith(GwtMockitoTestRunner.class)
public class PasswordResetPresenterTest {

  PasswordResetPresenter presenter;

  @Mock
  PasswordReset place;

  @Mock
  OneSageUtils oneSageUtils;

  @Before
  public void setup() {
    presenter = new PasswordResetPresenter(oneSageUtils);
    when(place.toToken()).thenReturn(ClientProperties.DEFAULT_PLACE_TOKEN);
  }

  @Test
  public void testStart() {
    presenter.setPlace(place);

    AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
    EventBus eventBus = mock(EventBus.class);

    presenter.start(panel, eventBus);
  }
}
