package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.verify;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.GridPlace;
import org.sagebionetworks.web.client.presenter.GridPagePresenter;
import org.sagebionetworks.web.client.view.GridPageView;

@RunWith(MockitoJUnitRunner.class)
public class GridPagePresenterTest {

  @Mock
  GridPageView mockView;

  @Mock
  SynapseJSNIUtils mockJsniUtils;

  @Mock
  GridPlace mockPlace;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  GridPagePresenter presenter;

  @Before
  public void setUp() {
    presenter = new GridPagePresenter(mockView, mockJsniUtils);
  }

  @Test
  public void testConstructorSetsTitles() {
    verify(mockJsniUtils).setPageTitle(DisplayConstants.WORKING_COPY);
  }

  @Test
  public void testStartSetsWidget() {
    presenter.start(mockPanel, mockEventBus);
    verify(mockPanel).setWidget(mockView.asWidget());
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(mockPlace);

    verify(mockView).render();
  }
}
