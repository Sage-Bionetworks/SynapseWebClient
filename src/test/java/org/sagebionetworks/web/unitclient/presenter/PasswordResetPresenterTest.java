package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.users.PasswordResetPresenter;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetPresenterTest {

  PasswordResetPresenter presenter;

  @Mock
  PasswordReset place;

  @Mock
  OneSageUtils oneSageUtils;

  @Mock
  GWTWrapper mockGwt;

  @Before
  public void setup() {
    presenter = new PasswordResetPresenter(oneSageUtils, mockGwt);
  }

  @Test
  public void testStart() {
    String expectedUrl = "https://accounts.synapse.org/resetPassword";
    when(oneSageUtils.getOneSageURL("/resetPassword")).thenReturn(expectedUrl);
    presenter.setPlace(place);

    AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
    EventBus eventBus = mock(EventBus.class);

    presenter.start(panel, eventBus);

    verify(mockGwt).replaceCurrentWindowWith(expectedUrl);
  }
}
