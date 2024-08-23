package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.verify;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.presenter.QuizPresenter;
import org.sagebionetworks.web.client.view.CertificationQuizView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class QuizPresenterTest {

  QuizPresenter presenter;

  @Mock
  CertificationQuizView mockSRCView;

  @Mock
  org.sagebionetworks.web.client.place.Quiz place;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  @Before
  public void setup() throws JSONObjectAdapterException {
    presenter = new QuizPresenter(mockSRCView);
  }

  @Test
  public void testSRCComponent() {
    presenter.start(mockPanel, mockEventBus);
    verify(mockPanel).setWidget(mockSRCView);
    verify(mockSRCView).createReactComponentWidget();
  }
}
