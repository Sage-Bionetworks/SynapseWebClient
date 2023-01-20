package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.jsinterop.CreatedByModifiedByProps;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ModifiedCreatedByWidgetTest {

  @Mock
  ModifiedCreatedByWidgetView mockView;

  ModifiedCreatedByWidget presenter;

  String entityId = "syn123";
  Long versionNumber = 1L;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    presenter = new ModifiedCreatedByWidget(mockView);
  }

  @Test
  public void testConfigure() {
    presenter.configure(entityId, versionNumber);

    verify(mockView).setProps(any(CreatedByModifiedByProps.class));
    verify(mockView).setVisible(true);
  }
}
