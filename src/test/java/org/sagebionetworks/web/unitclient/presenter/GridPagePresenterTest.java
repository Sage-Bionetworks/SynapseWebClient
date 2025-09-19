package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.RecordSet;
import org.sagebionetworks.repo.model.grid.GridSession;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
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
  SynapseJavascriptClient mockJsClient;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  GridPlace mockPlace;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  @Captor
  ArgumentCaptor<AsyncCallback<Entity>> getEntityCallbackCaptor;

  GridPagePresenter presenter;

  @Before
  public void setUp() {
    presenter =
      new GridPagePresenter(
        mockView,
        mockJsniUtils,
        mockJsClient,
        mockPopupUtils
      );
  }

  @Test
  public void testConstructorSetsTitles() {
    verify(mockJsniUtils).setPageTitle(DisplayConstants.WORKING_COPY);
    verify(mockView).setTitle(DisplayConstants.WORKING_COPY);
  }

  @Test
  public void testStartSetsWidget() {
    presenter.start(mockPanel, mockEventBus);
    verify(mockPanel).setWidget(mockView.asWidget());
  }

  @Test
  public void testSetPlaceSuccess() {
    String sessionId = "session123";
    String entityId = "syn456";
    String entityName = "Test Entity";
    when(mockPlace.getGridSessionId()).thenReturn(sessionId);
    GridSession session = new GridSession();
    session.setSourceEntityId(entityId);
    Entity entity = new RecordSet();
    entity.setName(entityName);

    when(mockJsClient.getGridSession(sessionId))
      .thenReturn(getDoneFuture(session));

    presenter.setPlace(mockPlace);

    verify(mockJsClient)
      .getEntity(eq(entityId), getEntityCallbackCaptor.capture());

    verify(mockView).render(sessionId);
    verify(mockJsClient).getGridSession(eq(sessionId));
    verify(mockJsClient).getEntity(eq(entityId), any());

    // Simulate entity fetch success
    getEntityCallbackCaptor.getValue().onSuccess(entity);

    verify(mockJsniUtils)
      .setPageTitle(DisplayConstants.WORKING_COPY + " - " + entityName);
    verify(mockView)
      .setTitle(DisplayConstants.WORKING_COPY + " of " + entityName);
  }

  @Test
  public void testSetPlaceGridSessionFailure() {
    String sessionId = "session123";
    when(mockPlace.getGridSessionId()).thenReturn(sessionId);
    Throwable error = new Exception("Session error");
    when(mockJsClient.getGridSession(sessionId))
      .thenReturn(getFailedFuture(error));
    presenter.setPlace(mockPlace);
    verify(mockView).render(sessionId);
    verify(mockJsClient).getGridSession(eq(sessionId));
    verify(mockPopupUtils)
      .notify(
        eq("Error"),
        eq("Session error"),
        eq(DisplayUtils.NotificationVariant.DANGER)
      );
  }

  @Test
  public void testSetPlaceEntityFailure() {
    String sessionId = "session123";
    String entityId = "syn456";
    when(mockPlace.getGridSessionId()).thenReturn(sessionId);
    GridSession session = new GridSession();
    session.setSourceEntityId(entityId);
    Throwable error = new Exception("Entity error");
    when(mockJsClient.getGridSession(sessionId))
      .thenReturn(getDoneFuture(session));
    doNothing()
      .when(mockJsClient)
      .getEntity(eq(entityId), getEntityCallbackCaptor.capture());

    presenter.setPlace(mockPlace);

    verify(mockView).render(sessionId);
    verify(mockJsClient).getGridSession(eq(sessionId));
    verify(mockJsClient).getEntity(eq(entityId), any());
    // Simulate entity fetch failure
    getEntityCallbackCaptor.getValue().onFailure(error);
    verify(mockPopupUtils)
      .notify(
        eq("Error"),
        eq("Entity error"),
        eq(DisplayUtils.NotificationVariant.DANGER)
      );
  }
}
