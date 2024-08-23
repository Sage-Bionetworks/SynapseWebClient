package org.sagebionetworks.web.client.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EntityModalProps;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EntityModalWidgetTest {

  @Mock
  EntityModalWidgetView mockView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  EntityModalProps.Callback mockOnClose;

  @Captor
  ArgumentCaptor<EntityModalProps> propsCaptor;

  EntityModalWidget widget;

  public static final String ENTITY_ID = "syn42";
  public static final Long VERSION_NUMBER = 5L;

  public static final String INITIAL_TAB = "ANNOTATIONS";
  public static final boolean SHOW_TABS = false;

  @Before
  public void before() {
    widget = new EntityModalWidget(mockView, mockGlobalAppState);
  }

  @After
  public void validate() {
    validateMockitoUsage();
  }

  @Test
  public void testConstruction() {
    widget.configure(
      ENTITY_ID,
      VERSION_NUMBER,
      mockOnClose,
      INITIAL_TAB,
      SHOW_TABS
    );
    verify(mockView).renderComponent(propsCaptor.capture());
    EntityModalProps capturedProps = propsCaptor.getValue();

    assertFalse(capturedProps.show);
    assertEquals(ENTITY_ID, capturedProps.entityId);
    assertEquals(VERSION_NUMBER, capturedProps.versionNumber);
    assertEquals(mockOnClose, capturedProps.onClose);
    assertEquals(INITIAL_TAB, capturedProps.initialTab);
    assertEquals(SHOW_TABS, capturedProps.showTabs);
    assertNotNull(capturedProps.onEditModeChanged);
  }

  @Test
  public void testOnEditModeChanged() {
    widget.configure(
      ENTITY_ID,
      VERSION_NUMBER,
      mockOnClose,
      INITIAL_TAB,
      SHOW_TABS
    );

    verify(mockView).renderComponent(propsCaptor.capture());
    EntityModalProps capturedProps = propsCaptor.getValue();

    capturedProps.onEditModeChanged.run(true);
    verify(mockGlobalAppState).setIsEditing(true);

    capturedProps.onEditModeChanged.run(false);
    verify(mockGlobalAppState).setIsEditing(false);
  }

  @Test
  public void testSetOpen() {
    widget.configure(
      ENTITY_ID,
      VERSION_NUMBER,
      mockOnClose,
      INITIAL_TAB,
      SHOW_TABS
    );

    widget.setOpen(true);
    verify(mockGlobalAppState, never()).setIsEditing(anyBoolean());
    verify(mockView, times(2)).renderComponent(propsCaptor.capture());
    assertTrue(propsCaptor.getValue().show);

    widget.setOpen(false);
    verify(mockGlobalAppState).setIsEditing(false);
    verify(mockView, times(3)).renderComponent(propsCaptor.capture());
    assertFalse(propsCaptor.getValue().show);
  }

  @Test
  public void testAsWidget() {
    widget.asWidget();
    verify(mockView).asWidget();
  }
}
