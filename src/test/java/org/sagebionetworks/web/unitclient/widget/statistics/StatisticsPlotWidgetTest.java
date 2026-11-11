package org.sagebionetworks.web.unitclient.widget.statistics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidget;
import org.sagebionetworks.web.client.widget.statistics.StatisticsPlotWidgetView;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsPlotWidgetTest {

  @Mock
  StatisticsPlotWidgetView mockView;

  @Mock
  AuthenticationController mockAuthController;

  StatisticsPlotWidget widget;

  @Before
  public void setUp() {
    widget = new StatisticsPlotWidget(mockView, mockAuthController);
  }

  @Test
  public void testConfigureAndShowLoggedIn() {
    when(mockAuthController.isLoggedIn()).thenReturn(true);

    widget.configureAndShow("syn1");

    verify(mockView).configureAndShow("syn1");
  }

  @Test
  public void testConfigureAndShowAnonymous() {
    when(mockAuthController.isLoggedIn()).thenReturn(false);

    widget.configureAndShow("syn1");

    verify(mockView, never()).configureAndShow(any());
  }
}
