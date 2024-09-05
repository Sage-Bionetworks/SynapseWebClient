package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.verify;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.place.DataAccessManagementPlace;
import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;
import org.sagebionetworks.web.client.view.DataAccessManagementView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DataAccessManagementPresenterTest {

  @Mock
  DataAccessManagementView mockView;

  @Mock
  DataAccessManagementPlace mockPlace;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  DataAccessManagementPresenter presenter;

  @Before
  public void setUp() {
    presenter = new DataAccessManagementPresenter(mockView);
  }

  @Test
  public void testStart() {
    presenter.start(mockPanel, mockEventBus);
    verify(mockPanel).setWidget(mockView);
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(mockPlace);

    verify(mockView).render();
  }
}
