package org.sagebionetworks.web.client.widget.table;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * This implementation uses Scheduler.get().scheduleDeferred() to set the focus and select.
 * 
 * @author John
 *
 */
public class FocusSetterImpl implements FocusSetter {

	@Override
	public void attemptSetFocus(final IsWidget widget, final boolean shouldSelectAll) {
		// Can only set focus on a focusable
		if (widget instanceof Focusable) {
			final Focusable focusableWidget = (Focusable) widget;
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					// Set the focus on the widget.
					focusableWidget.setFocus(true);
					// Select all if requested.
					if (shouldSelectAll) {
						// Select all if we can.
						if (widget instanceof ValueBoxBase) {
							((ValueBoxBase) widget).selectAll();
						}
					}
				}
			});
		}
	}
}
