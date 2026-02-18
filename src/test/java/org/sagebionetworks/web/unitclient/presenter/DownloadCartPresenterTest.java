package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.DownloadCartPlace;
import org.sagebionetworks.web.client.presenter.DownloadCartPresenter;
import org.sagebionetworks.web.client.view.DownloadCartPageView;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidget;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DownloadCartPresenterTest {

  DownloadCartPresenter presenter;

  @Mock
  DownloadCartPageView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  PopupUtilsView mockPopupUtilsView;

  @Mock
  EntityAccessControlListModalWidget mockACLModalWidget;

  @Before
  public void setup() throws Exception {
    presenter = new DownloadCartPresenter(mockView, mockGinInjector);
    when(mockGinInjector.getEntityAccessControlListModalWidget())
      .thenReturn(mockACLModalWidget);
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(presenter);
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(new DownloadCartPlace(""));

    verify(mockView).render();
  }

  @Test
  public void testOnViewSharingSettingsClicked() {
    String testEntityId = "syn1";

    presenter.onViewSharingSettingsClicked(testEntityId);

    verify(mockACLModalWidget).configure(eq(testEntityId), any());
  }
}
