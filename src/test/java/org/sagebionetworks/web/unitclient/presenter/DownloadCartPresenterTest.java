package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.place.DownloadCartPlace;
import org.sagebionetworks.web.client.presenter.DownloadCartPresenter;
import org.sagebionetworks.web.client.view.DownloadCartPageView;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DownloadCartPresenterTest {

  DownloadCartPresenter presenter;

  @Mock
  DownloadCartPageView mockView;

  @Before
  public void setup() throws Exception {
    presenter = new DownloadCartPresenter(mockView);
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(new DownloadCartPlace(""));

    verify(mockView).render();
  }
}
