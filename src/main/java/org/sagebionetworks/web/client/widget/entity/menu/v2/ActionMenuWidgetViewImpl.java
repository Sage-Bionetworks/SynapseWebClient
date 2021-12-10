package org.sagebionetworks.web.client.widget.entity.menu.v2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDown;
import org.gwtbootstrap3.client.ui.DropDownHeader;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
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

	public interface Binder extends UiBinder<Widget, ActionMenuWidgetViewImpl> {
	}

	@UiField
	DropDown dropdown;
	@UiField
	Div controllerContainer;
	@UiField
	Button toolsMenu;

	FlowPanel widget;
	@UiField
	Divider actDivider;
	@UiField
	DropDownHeader noActionsAvailable;
	@UiField
	DropDownHeader actHeader;
	@UiField
	Button tableDownloadOptions;
	@UiField
	ActionMenuItem addToDownloadListMenuItem;
	@UiField
	ActionMenuItem programmaticOptionsMenuItem;
	@UiField
	Tooltip addToDownloadListMenuItemTooltip;
	@UiField
	Tooltip programmaticOptionsMenuItemTooltip;

	@Inject
	public ActionMenuWidgetViewImpl(Binder binder) {
		widget = (FlowPanel)binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public Iterable<ActionView> listActionViews() {
		List<ActionView> list = new LinkedList<ActionView>();
		recursiveSearch(list, widget);
		return list;
	}

	/**
	 * Recursive function to find all ActionView within this widget.
	 * 
	 * @param results
	 * @param toSearch
	 */
	private static void recursiveSearch(List<ActionView> results, ComplexPanel toSearch) {
		Iterator<Widget> childIterator = toSearch.iterator();
		if (childIterator != null) {
			while (childIterator.hasNext()) {
				Widget child = childIterator.next();
				if (child instanceof ActionView) {
					results.add((ActionView) child);
				} else if (child instanceof ComplexPanel) {
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
	public void setACTDividerVisible(boolean visible) {
		actDivider.setVisible(visible);
		actHeader.setVisible(visible);
	}
	
	@Override
	public void setTableDownloadOptionsVisible(boolean visible) {
		tableDownloadOptions.setVisible(visible);
	}

	@Override
	public void setDownloadActionsEnabled(boolean enabled) {
		addToDownloadListMenuItem.setEnabled(enabled);
		programmaticOptionsMenuItem.setEnabled(enabled);
		if (enabled) {
			// The tooltips only show information about why it's disabled, so hide the tooltips if enabled
			addToDownloadListMenuItemTooltip.setTrigger(Trigger.MANUAL);
			programmaticOptionsMenuItemTooltip.setTrigger(Trigger.MANUAL);
			addToDownloadListMenuItemTooltip.hide();
			programmaticOptionsMenuItemTooltip.hide();
		} else {
			addToDownloadListMenuItemTooltip.setTrigger(Trigger.HOVER);
			programmaticOptionsMenuItemTooltip.setTrigger(Trigger.HOVER);
		}

		// Commit the changes to the tooltips
		addToDownloadListMenuItemTooltip.recreate();
		programmaticOptionsMenuItemTooltip.recreate();
	}
	
	@Override
	public void setToolsButtonIcon(String text, IconType icon) {
		toolsMenu.setText(text);
		toolsMenu.setIcon(icon);
	}

	@Override
	public void setNoActionsAvailableVisible(boolean visible) {
		noActionsAvailable.setVisible(visible);
	}
}
