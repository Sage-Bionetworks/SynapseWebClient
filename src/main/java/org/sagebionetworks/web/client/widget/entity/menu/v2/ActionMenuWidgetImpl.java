package org.sagebionetworks.web.client.widget.entity.menu.v2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ActionMenuWidgetImpl implements ActionMenuWidget, ActionView.Presenter {
	
	PortalGinInjector ginInjector;
	ActionMenuWidgetView view;
	
	Map<Action, ActionView> actionViewMap;
	Map<Action, List<ActionListener>> actionListenerMap;
	
	@Inject
	public ActionMenuWidgetImpl(PortalGinInjector ginInjector, ActionMenuWidgetView view) {
		this.ginInjector = ginInjector;
		this.view = view;
		reset();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void reset() {
		this.actionViewMap = new HashMap<Action, ActionView>();
		this.actionListenerMap = new HashMap<Action, List<ActionListener>>();
		// Add each action to the view
		view.clear();
		for(Action action: Action.values()){
			ActionView actionView;
			if(ActionType.BUTTON.equals(action.getType())){
				actionView = ginInjector.createActionButton();
				view.addButton(actionView);
			}else if(ActionType.TOOL_MENU.equals(action.getType())){
				actionView = ginInjector.createActionMenuItem();
				view.addMenuItem(actionView);
			}else{
				throw new IllegalArgumentException("Unknown action type: "+action.getType());
			}
			actionView.setText(action.getText());
			actionView.setIcon(action.getIcon());
			// map this action to the view
			actionViewMap.put(action, actionView);
			// bind this view to this presenter.
			actionView.setPresenter(this, action);
			// Each view starts off as not visible.
			actionView.setVisible(false);
			this.actionListenerMap.put(action, new LinkedList<ActionListener>());
		}
	}

	@Override
	public void setActionEnabled(Action action, boolean enabled) {
		getActionView(action).setEnabled(enabled);
	}

	@Override
	public void setActionVisible(Action action, boolean visible) {
		getActionView(action).setVisible(visible);
	}

	@Override
	public void setActionText(Action action, String text) {
		getActionView(action).setText(text);
		
	}

	@Override
	public void setActionIcon(Action action, IconType icon) {
		getActionView(action).setIcon(icon);
	}

	/**
	 * Get the view for the given action from the map.
	 * @param action
	 * @throws IllegalArgumentException When there is no view mapped to the given action.
	 * @return
	 */
	private ActionView getActionView(Action action){
		ActionView actionView = this.actionViewMap.get(action);
		if(actionView == null){
			throw new IllegalArgumentException("No action view found for action: "+action);
		}
		return actionView;
	}
	
	private List<ActionListener> getActionListeners(Action action){
		List<ActionListener> list = this.actionListenerMap.get(action);
		if(list == null){
			throw new IllegalArgumentException("No action list found for action: "+action);
		}
		return list;
	}

	@Override
	public void onClicked(Action action) {
		// forward to the listener
		for(ActionListener listener: getActionListeners(action)){
			listener.onAction(action);
		}
	}

	@Override
	public void addActionListener(Action action, ActionListener listener) {
		getActionListeners(action).add(listener);
	}
	
}
