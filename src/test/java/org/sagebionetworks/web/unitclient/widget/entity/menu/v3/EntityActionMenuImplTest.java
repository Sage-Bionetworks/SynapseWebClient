package org.sagebionetworks.web.unitclient.widget.entity.menu.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.ActionListener;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuProps;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuView;

@RunWith(MockitoJUnitRunner.class)
public class EntityActionMenuImplTest {

  @Mock
  EntityActionMenuView mockView;

  @Mock
  ActionListener mockActionListener;

  @Mock
  ReactMouseEvent mockMouseEvent;

  @Captor
  ArgumentCaptor<EntityActionMenuProps> propsCaptor;

  @Mock
  IsWidget mockControllerWidget;

  @Mock
  EntityActionMenuLayout mockLayout;

  EntityActionMenuImpl entityActionMenu;

  @Before
  public void before() {
    entityActionMenu = new EntityActionMenuImpl(mockView);
  }

  @Test
  public void testConstructorHappy() {
    verify(mockView).configure(propsCaptor.capture());

    EntityActionMenuProps props = propsCaptor.getValue();
    Map<Action, ActionConfiguration> actionConfiguration = props.getActionConfiguration();
    // Every action should be configured
    assertEquals(Action.values().length, actionConfiguration.keySet().size());
    // All actions should not be visible
    assertTrue(
      actionConfiguration
        .entrySet()
        .stream()
        .noneMatch(entry -> entry.getValue().isVisible())
    );
  }

  @Test
  public void testSetActionListener() {
    ActionListener mockActionListener2 = Mockito.mock(ActionListener.class);
    entityActionMenu.setActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener2
    );
    entityActionMenu.setActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener
    );
    // Call under test
    entityActionMenu.onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    // Should get forwarded to the listener
    verify(mockActionListener)
      .onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    // The old listener should have been replaced
    verify(mockActionListener2, never())
      .onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
  }

  @Test
  public void testAddActionListener() {
    ActionListener mockActionListener2 = Mockito.mock(ActionListener.class);
    entityActionMenu.addActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener2
    );
    entityActionMenu.addActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener
    );
    // Call under test
    entityActionMenu.onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    // Should get forwarded to the listeners
    verify(mockActionListener)
      .onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    verify(mockActionListener2)
      .onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
  }

  @Test
  public void testSetActionHref() {
    String href = "test";
    entityActionMenu.setActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener
    );
    entityActionMenu.setActionHref(Action.CHANGE_ENTITY_NAME, href);
    // Call under test - the action should have been removed
    try {
      entityActionMenu.onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    } catch (IllegalArgumentException e) {
      // Verify the exception contains the action name (for reproducing the issue) and a helpful portion of the message
      assertTrue(e.getMessage().contains(Action.CHANGE_ENTITY_NAME.name()));
      assertTrue(e.getMessage().contains("no listeners present"));
    } catch (Exception e) {
      fail("Unexpected exception");
    }

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertEquals(
      href,
      actionConfiguration.get(Action.CHANGE_ENTITY_NAME).getHref()
    );
  }

  @Test
  public void testAddActionHref() {
    String href = "test";
    entityActionMenu.setActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener
    );
    entityActionMenu.addActionHref(Action.CHANGE_ENTITY_NAME, href);
    // Call under test - the action listener should not be removed
    entityActionMenu.onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    // Should get forwarded to the listeners
    verify(mockActionListener)
      .onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertEquals(
      href,
      actionConfiguration.get(Action.CHANGE_ENTITY_NAME).getHref()
    );
  }

  @Test
  public void testReset() {
    entityActionMenu.setActionListener(
      Action.CHANGE_ENTITY_NAME,
      mockActionListener
    );
    // Now reset the component
    entityActionMenu.reset();
    // we'll get an exception since there are no action listeners.
    try {
      entityActionMenu.onAction(Action.CHANGE_ENTITY_NAME, mockMouseEvent);
    } catch (IllegalArgumentException e) {
      // Verify the exception contains the action name (for reproducing the issue) and a helpful portion of the message
      assertTrue(e.getMessage().contains(Action.CHANGE_ENTITY_NAME.name()));
      assertTrue(e.getMessage().contains("no listeners present"));
    } catch (Exception e) {
      fail("Unexpected exception");
    }
    // Should get forwarded to the listener
    verify(mockActionListener, never()).onAction(any(Action.class), any());
  }

  @Test
  public void testSetActionVisible() {
    // Call under test
    entityActionMenu.setActionVisible(Action.DELETE_ENTITY, true);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertTrue(actionConfiguration.get(Action.DELETE_ENTITY).isVisible());
  }

  @Test
  public void testActionSetText() {
    String text = "new delete text";
    // Call under test
    entityActionMenu.setActionText(Action.DELETE_ENTITY, text);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertEquals(text, actionConfiguration.get(Action.DELETE_ENTITY).getText());
  }

  @Test
  public void testSetIsLoading() {
    // Call under test
    entityActionMenu.setIsLoading(true);

    verify(mockView).setIsLoading(true);
  }

  @Test
  public void testSetActionEnabled() {
    // Call under test
    entityActionMenu.setActionEnabled(Action.DELETE_ENTITY, false);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertTrue(actionConfiguration.get(Action.DELETE_ENTITY).isDisabled());
  }

  @Test
  public void testSetActionTooltipText() {
    String tooltipText = "test tooltip text";
    // Call under test
    entityActionMenu.setActionTooltipText(Action.DELETE_ENTITY, tooltipText);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    Map<Action, ActionConfiguration> actionConfiguration = propsCaptor
      .getValue()
      .getActionConfiguration();
    assertEquals(
      tooltipText,
      actionConfiguration.get(Action.DELETE_ENTITY).getTooltipText()
    );
  }

  @Test
  public void testSetDownloadMenuEnabled() {
    // Call under test
    entityActionMenu.setDownloadMenuEnabled(false);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    EntityActionMenuDropdownMap dropdownConfigs = propsCaptor
      .getValue()
      .getMenuConfiguration();
    assertTrue(dropdownConfigs.getDownloadMenuConfiguration().isDisabled());
  }

  @Test
  public void testSetDownloadMenuTooltipText() {
    String tooltipText = "test tooltip";
    // Call under test
    entityActionMenu.setDownloadMenuTooltipText(tooltipText);

    verify(mockView, times(2)).configure(propsCaptor.capture());
    EntityActionMenuDropdownMap dropdownConfigs = propsCaptor
      .getValue()
      .getMenuConfiguration();
    assertEquals(
      tooltipText,
      dropdownConfigs.getDownloadMenuConfiguration().getTooltipText()
    );
  }

  @Test
  public void testAddControllerWidget() {
    entityActionMenu.addControllerWidget(mockControllerWidget);
    verify(mockView).addControllerWidget(mockControllerWidget);
  }

  @Test
  public void testSetLayout() {
    // Call under test
    entityActionMenu.setLayout(mockLayout);
    verify(mockView, times(2)).configure(propsCaptor.capture());
    assertNotNull(propsCaptor.getValue().getLayout());
  }
}
