package org.sagebionetworks.web.client.widget.entity.menu.v2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonToolBar;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * Basic implementation with zero business logic.
 * 
 * @author jhill
 *
 */
public class ActionMenuWidgetViewImpl implements ActionMenuWidgetView {
	
	public interface Binder extends UiBinder<Widget, ActionMenuWidgetViewImpl> {}

	@UiField
	Divider basicDivider;
	@UiField
	ButtonToolBar buttonToolBar;
	@UiField
	Div controllerContainer;
	@UiField
	Button toolsMenu;
	
	Widget widget;

	
	@Inject
	public ActionMenuWidgetViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public Iterable<ActionView> listActionViews() {
		List<ActionView> list = new LinkedList<ActionView>();
		recursiveSearch(list, buttonToolBar);
		return list;
	}
	/**
	 * Recursive function to find all ActionView within this widget.
	 * 
	 * @param results
	 * @param toSearch
	 */
	private static void recursiveSearch(List<ActionView> results, ComplexPanel toSearch){
		Iterator<Widget> childIterator = toSearch.iterator();
		if(childIterator != null){
			while(childIterator.hasNext()){
				Widget child = childIterator.next();
				if(child instanceof ActionView){
					results.add((ActionView) child);
				}else if(child instanceof ComplexPanel){
					ComplexPanel container = (ComplexPanel) child;
					recursiveSearch(results, container);
				}
			}
		}
	}

	@Override
	public void addControllerWidget(IsWidget controllerWidget) {
		controllerContainer.add(controllerWidget);
	}

	@Override
	public void setBasicDividerVisible(boolean visible) {
		this.basicDivider.setVisible(visible);
	}

	@Override
	public void setToolsButtonVisible(boolean visible) {
		toolsMenu.setVisible(visible);
	}

}
