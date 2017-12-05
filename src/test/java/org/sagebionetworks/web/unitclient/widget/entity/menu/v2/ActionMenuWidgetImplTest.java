package org.sagebionetworks.web.unitclient.widget.entity.menu.v2;

import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.widget.amplitude.AmplitudeSDK;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.mixpanel.MixPanelSdk;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidgetView;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionView;

public class ActionMenuWidgetImplTest {
	
	@Mock
	ActionView mockRename;
	@Mock
	ActionView mockDelete;
	@Mock
	ActionMenuWidgetView mockView;
	@Mock
	ActionListener mockActionListener;
	@Mock
	MixPanelSdk mockMixPanelSdk;
	@Mock
	AmplitudeSDK mockAmplitudeSdk;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockRename.getAction()).thenReturn(Action.CHANGE_ENTITY_NAME);
		when(mockDelete.getAction()).thenReturn(Action.DELETE_ENTITY);
		List<ActionView> actionView = Arrays.asList(mockRename, mockDelete);
		when(mockView.listActionViews()).thenReturn(actionView);
	}
	
	@Test
	public void testConstructorHappy(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		// Each view should be hidden
		verify(mockRename).setVisible(false);
		verify(mockDelete).setVisible(false);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testConstructorDoubleAction(){
		// Double bind the rename action
		List<ActionView> actionView = Arrays.asList(mockRename, mockRename);
		mockView = Mockito.mock(ActionMenuWidgetView.class);
		when(mockView.listActionViews()).thenReturn(actionView);
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testConstructorNullAction(){
		when(mockRename.getAction()).thenReturn(null);
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
	}
	
	@Test
	public void testSetActionListener(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		ActionListener mockActionListener2 = Mockito.mock(ActionListener.class);
		widget.setActionListener(Action.CHANGE_ENTITY_NAME, mockActionListener2);
		widget.setActionListener(Action.CHANGE_ENTITY_NAME, mockActionListener);
		widget.onAction(Action.CHANGE_ENTITY_NAME);
		// Should get forwarded to the listener
		verify(mockActionListener).onAction(Action.CHANGE_ENTITY_NAME);
		verify(mockActionListener2, never()).onAction(Action.CHANGE_ENTITY_NAME);
	}
	
	@Test
	public void testAddActionListener(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		ActionListener mockActionListener2 = Mockito.mock(ActionListener.class);
		widget.addActionListener(Action.CHANGE_ENTITY_NAME, mockActionListener2);
		widget.addActionListener(Action.CHANGE_ENTITY_NAME, mockActionListener);
		widget.onAction(Action.CHANGE_ENTITY_NAME);
		// Should get forwarded to the listeners
		verify(mockActionListener).onAction(Action.CHANGE_ENTITY_NAME);
		verify(mockActionListener2).onAction(Action.CHANGE_ENTITY_NAME);
	}
	
	@Test
	public void testReset(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		widget.setActionListener(Action.CHANGE_ENTITY_NAME, mockActionListener);
		// Now reset the the widget
		widget.reset();
		// nothing should happen here.
		widget.onAction(Action.CHANGE_ENTITY_NAME);
		// Should get forwarded to the listener
		verify(mockActionListener, never()).onAction(any(Action.class));
	}
	
	@Test
	public void testActionVisible(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		verify(mockDelete).setVisible(false);
		widget.setActionVisible(Action.DELETE_ENTITY, true);
		verify(mockDelete).setVisible(true);
		verify(mockRename, never()).setVisible(true);
	}
	
	@Test
	public void testActionSetText(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		String text = "new delete text";
		widget.setActionText(Action.DELETE_ENTITY, text);
		verify(mockDelete).setText(text);
		verify(mockRename, never()).setText(anyString());
	}
	
	@Test
	public void testActionSetIcon(){
		ActionMenuWidgetImpl widget = new ActionMenuWidgetImpl(mockView, mockMixPanelSdk, mockAmplitudeSdk);
		IconType icon = IconType.TRASH_O;
		widget.setActionIcon(Action.DELETE_ENTITY, icon);
		verify(mockDelete).setIcon(icon);
		verify(mockRename, never()).setIcon(any(IconType.class));
	}
}
