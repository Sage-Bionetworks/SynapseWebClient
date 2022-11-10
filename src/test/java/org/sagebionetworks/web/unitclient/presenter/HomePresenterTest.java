package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.view.HomeView;

@RunWith(MockitoJUnitRunner.class)
public class HomePresenterTest {

  @InjectMocks
  HomePresenter homePresenter;

  @Mock
  HomeView mockView;

  @Test
  public void testStart() {
    GWTMockUtilities.disarm();

    AcceptsOneWidget mockPanel = mock(AcceptsOneWidget.class);
    EventBus mockEventBus = mock(EventBus.class);
    when(mockView.asWidget()).thenReturn(mock(Widget.class));

    // Method under test
    homePresenter.start(mockPanel, mockEventBus);

    verify(mockView).render();
    verify(mockPanel).setWidget(mockView);

    GWTMockUtilities.restore();
  }

  @Test
  public void testSetPlace() {
    Home place = Mockito.mock(Home.class);
    homePresenter.setPlace(place);
    verify(mockView).refresh();
  }
}
