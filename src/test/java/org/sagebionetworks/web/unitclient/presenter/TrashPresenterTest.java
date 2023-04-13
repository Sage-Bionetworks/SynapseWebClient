package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.presenter.TrashPresenter;
import org.sagebionetworks.web.client.view.TrashView;

@RunWith(MockitoJUnitRunner.class)
public class TrashPresenterTest {

  TrashPresenter presenter;

  @Mock
  TrashView mockView;

  @Mock
  SynapseReactClientFullContextPropsProvider mockPropsProvider;

  @Mock
  Trash mockPlace;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  @Before
  public void setup() throws JSONObjectAdapterException {
    mockView = mock(TrashView.class);
    presenter = new TrashPresenter(mockView, mockPropsProvider);
  }

  @Test
  public void testStart() {
    presenter.start(mockPanel, mockEventBus);
    verify(mockPanel).setWidget(mockView);
    verify(mockView).createReactComponentWidget(mockPropsProvider);
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(mockPlace);
  }
}
