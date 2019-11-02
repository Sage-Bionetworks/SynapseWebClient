package org.sagebionetworks.web.client.view.bootstrap;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

/**
 * Helpers for bootstrap buttons.
 * 
 * @author jmhill
 *
 */
public class ButtonUtils {

	/**
	 * Helper to change the enable state of a button and the button type at the same time. When a button
	 * is enabled the passed ButtonType will be applied. Wehen the button is disabled, the
	 * ButtonType.DEFAULT will be applied.
	 * 
	 * @param enabled Should the button be enabled?
	 * @param button The button the change.
	 * @param enabledType The ButtonType that should be applied when the button is enabled. The
	 *        ButtonType.DEFAULT will be applied when a button is disabled.
	 */
	public static void setEnabledAndType(boolean enabled, Button button, ButtonType enabledType) {
		button.setEnabled(enabled);
		if (enabled) {
			button.setType(enabledType);
		} else {
			button.setType(ButtonType.DEFAULT);
		}
	}
}
