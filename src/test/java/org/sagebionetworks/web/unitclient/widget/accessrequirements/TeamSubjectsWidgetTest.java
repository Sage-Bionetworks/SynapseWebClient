package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectsWidget;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class TeamSubjectsWidgetTest {

  TeamSubjectsWidget widget;

  @Mock
  DivView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;

  @Mock
  RestrictableObjectDescriptor mockRestrictableObjectDescriptor;

  @Captor
  ArgumentCaptor<CallbackP<Boolean>> callbackCaptor;

  @Mock
  TeamSubjectWidget mockSubjectWidget;

  @Mock
  CallbackP<RestrictableObjectDescriptor> mockDeleteCallback;

  @Captor
  ArgumentCaptor<CallbackP<TeamSubjectWidget>> subjectWidgetCallbackCaptor;

  public static final String ID = "876787";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget =
      new TeamSubjectsWidget(
        mockView,
        mockGinInjector,
        mockIsACTMemberAsyncHandler
      );
    when(mockGinInjector.getSubjectWidget()).thenReturn(mockSubjectWidget);
    when(mockRestrictableObjectDescriptor.getId()).thenReturn(ID);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setVisible(false);
  }

  @Test
  public void testConfigureEntity() {
    when(mockRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.ENTITY);

    widget.configure(
      Collections.singletonList(mockRestrictableObjectDescriptor)
    );

    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackCaptor.capture());
    CallbackP<Boolean> callback = callbackCaptor.getValue();

    // verify no widget created if not ACT
    callback.invoke(false);
    verifyZeroInteractions(mockGinInjector);

    // Verify widget is not created, even if ACT, because the TeamSubjectsWidget only supports Teams.
    // Entity subjects are now handled by the EntitySubjectsWidget (using a SRC EntityHeaderTable)
    callback.invoke(true);
    verify(mockGinInjector, never()).getSubjectWidget();
  }

  @Test
  public void testConfigureTeamWithDeleteCallback() {
    when(mockRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.TEAM);
    widget.configure(
      Collections.singletonList(mockRestrictableObjectDescriptor)
    );
    when(mockSubjectWidget.getRestrictableObjectDescriptor())
      .thenReturn(mockRestrictableObjectDescriptor);
    widget.setDeleteCallback(mockDeleteCallback);
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackCaptor.capture());
    CallbackP<Boolean> callback = callbackCaptor.getValue();
    callback.invoke(true);

    verify(mockGinInjector).getSubjectWidget();
    verify(mockSubjectWidget)
      .configure(
        eq(mockRestrictableObjectDescriptor),
        subjectWidgetCallbackCaptor.capture()
      );
    CallbackP<TeamSubjectWidget> callbackP =
      subjectWidgetCallbackCaptor.getValue();

    // simulate subject deleted by the subjects widget
    callbackP.invoke(mockSubjectWidget);
    verify(mockView).remove(mockSubjectWidget);
    verify(mockDeleteCallback).invoke(mockRestrictableObjectDescriptor);
  }
}
