package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.Widget;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.EvaluationEditorPageProps;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.ChallengeWidget;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorReactComponentPage;

public class ChallengeTabTest {

  @Mock
  Tab mockTab;

  @Mock
  ChallengeTabView mockView;

  @Mock
  CallbackP<Tab> mockOnClickCallback;

  @Mock
  AdministerEvaluationsList mockAdministerEvaluationsList;

  @Mock
  ChallengeWidget mockChallengeWidget;

  @Mock
  PortalGinInjector mockPortalGinInjector;

  @Mock
  EntityBundle mockProjectEntityBundle;

  @Mock
  EntityActionMenu mockActionMenuWidget;

  @Mock
  CookieProvider mockCookieProvider;

  ChallengeTab tab;

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  EvaluationEditorReactComponentPage mockEvaluationEditorReactComponentPage;

  @Captor
  ArgumentCaptor<Consumer<String>> consumerCaptor;

  @Captor
  ArgumentCaptor<EvaluationEditorPageProps.Callback> callbackCaptor;

  @Before
  public void setUp() {
    // The evaluation editor page does not have abstraction for all UIObjects, so we must disarm (to avoid GWT.create()) until this is fixed
    GWTMockUtilities.disarm();
    MockitoAnnotations.initMocks(this);
    tab =
      new ChallengeTab(
        mockTab,
        mockPortalGinInjector,
        mockAuthenticationController,
        mockGlobalApplicationState,
        mockCookieProvider
      );
    when(mockTab.getEntityActionMenu()).thenReturn(mockActionMenuWidget);
    when(mockPortalGinInjector.getChallengeTabView()).thenReturn(mockView);
    when(mockPortalGinInjector.getAdministerEvaluationsList())
      .thenReturn(mockAdministerEvaluationsList);
    when(mockPortalGinInjector.getChallengeWidget())
      .thenReturn(mockChallengeWidget);
    when(mockPortalGinInjector.createEvaluationEditorReactComponentPage())
      .thenReturn(mockEvaluationEditorReactComponentPage);
    tab.lazyInject();
  }

  @After
  public void after() {
    GWTMockUtilities.restore();
  }

  @Test
  public void testConstruction() {
    verify(mockView).setEvaluationList(any(Widget.class));
    verify(mockView).setChallengeWidget(any(Widget.class));
  }

  @Test
  public void testSetTabClickedCallback() {
    tab.setTabClickedCallback(mockOnClickCallback);
    verify(mockTab).addTabClickedCallback(mockOnClickCallback);
  }

  @Test
  public void testConfigure() {
    String entityId = "syn1";
    String entityName = "challenge project test";
    tab.configure(entityId, entityName, mockProjectEntityBundle);

    verify(mockAdministerEvaluationsList)
      .configure(eq(entityId), consumerCaptor.capture());
    verify(mockChallengeWidget).configure(entityId, entityName);

    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
    Synapse place = captor.getValue();
    assertEquals(entityId, place.getEntityId());
    assertNull(place.getVersionNumber());
    assertEquals(EntityArea.CHALLENGE, place.getArea());
    assertNull(place.getAreaToken());
    //verify evaluation editor
    String evaluationId = "88288282";

    consumerCaptor.getValue().accept(evaluationId);

    verify(mockGlobalApplicationState).setIsEditing(true);
    verify(mockEvaluationEditorReactComponentPage)
      .configure(
        eq(evaluationId),
        anyString(),
        anyString(),
        anyBoolean(),
        callbackCaptor.capture()
      );

    callbackCaptor.getValue().run();
    verify(mockEvaluationEditorReactComponentPage).removeFromParent();
    verify(mockGlobalApplicationState).setIsEditing(false);
  }

  @Test
  public void testAsTab() {
    assertEquals(mockTab, tab.asTab());
  }
}
