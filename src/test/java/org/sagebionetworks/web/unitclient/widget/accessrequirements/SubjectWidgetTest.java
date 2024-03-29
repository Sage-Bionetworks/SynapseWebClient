package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

public class SubjectWidgetTest {

  TeamSubjectWidget widget;

  @Mock
  TeamSubjectWidgetView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  EntityIdCellRenderer mockEntityIdCellRendererImpl;

  @Mock
  TeamBadge mockTeamBadge;

  @Mock
  RestrictableObjectDescriptor mockRestrictableObjectDescriptor;

  @Mock
  CallbackP<TeamSubjectWidget> mockSubjectDeletedCallback;

  public static final String ID = "876787";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget = new TeamSubjectWidget(mockView, mockGinInjector);
    when(mockGinInjector.createEntityIdCellRenderer())
      .thenReturn(mockEntityIdCellRendererImpl);
    when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
    when(mockRestrictableObjectDescriptor.getId()).thenReturn(ID);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(widget);
  }

  @Test
  public void testConfigureEntity() {
    when(mockRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.ENTITY);

    widget.configure(
      mockRestrictableObjectDescriptor,
      mockSubjectDeletedCallback
    );

    verifyZeroInteractions(mockGinInjector);
  }

  @Test
  public void testConfigureTeamNoDelete() {
    when(mockRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.TEAM);
    widget.configure(mockRestrictableObjectDescriptor, null);

    verify(mockGinInjector).getTeamBadgeWidget();
    verify(mockTeamBadge).configure(ID);
    verify(mockView).setDeleteVisible(false);
  }
}
