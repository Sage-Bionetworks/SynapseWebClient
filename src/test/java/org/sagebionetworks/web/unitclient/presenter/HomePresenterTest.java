package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HomePresenterTest {

  HomePresenter presenter;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  AuthenticationController mockAuthController;

  @Mock
  HomeView mockView;

  @Before
  public void before() {
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    presenter =
      new HomePresenter(
        mockView,
        mockAuthController,
        mockGlobalApplicationState
      );
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    when(mockAuthController.isLoggedIn()).thenReturn(false);
  }

  @Test
  public void testStart() {
    GWTMockUtilities.disarm();

    AcceptsOneWidget mockPanel = mock(AcceptsOneWidget.class);
    EventBus mockEventBus = mock(EventBus.class);
    when(mockView.asWidget()).thenReturn(mock(Widget.class));

    // Method under test
    presenter.start(mockPanel, mockEventBus);

    verify(mockView).render();
    verify(mockPanel).setWidget(mockView);

    GWTMockUtilities.restore();
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(Mockito.mock(Home.class));
    verify(mockView).refresh();
    verify(mockPlaceChanger, never()).goTo(any(Profile.class));
  }

  @Test
  public void testRedirect() {
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    presenter.setPlace(Mockito.mock(Home.class));
    verify(mockView).refresh();
    verify(mockPlaceChanger).goTo(any(Profile.class));
  }

  @Test
  public void testForceNoRedriect() {
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    Home place = Mockito.mock(Home.class);
    when(place.toToken()).thenReturn(Home.LOGGED_IN_FORCE_NO_REDIRECT_TOKEN);
    presenter.setPlace(place);
    verify(mockView).refresh();
    verify(mockPlaceChanger, never()).goTo(any(Profile.class));
  }
}
