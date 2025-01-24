package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.http.client.RequestException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.place.TrustCenterPlace;
import org.sagebionetworks.web.client.presenter.TrustCenterPresenter;
import org.sagebionetworks.web.client.view.TrustCenterView;

public class TrustCenterPresenterTest {

  @Mock
  TrustCenterView mockView;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  TrustCenterPlace mockPlace;

  TrustCenterPresenter presenter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    presenter = new TrustCenterPresenter(mockView, mockPopupUtils);
  }

  @Test
  public void testValidDocumentKey() throws RequestException {
    when(mockPlace.getDocumentKey())
      .thenReturn(TrustCenterPlace.PRIVACY_POLICY_KEY);

    presenter.setPlace(mockPlace);

    verify(mockPopupUtils, never()).showErrorMessage(anyString());
    verify(mockView)
      .render(
        TrustCenterPresenter.REPO_OWNER,
        TrustCenterPresenter.REPO_NAME,
        "privacy.md"
      );
  }

  @Test
  public void testInvalidDocumentKey() throws RequestException {
    when(mockPlace.getDocumentKey()).thenReturn("Invalid Key");

    presenter.setPlace(mockPlace);

    verify(mockPopupUtils).showErrorMessage(anyString());
  }
}
