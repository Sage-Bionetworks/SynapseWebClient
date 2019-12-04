package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.base.HasActive;
import org.gwtbootstrap3.client.ui.base.HasBadge;
import org.gwtbootstrap3.client.ui.base.HasDataTarget;
import org.gwtbootstrap3.client.ui.base.HasHref;
import org.gwtbootstrap3.client.ui.base.HasIcon;
import org.gwtbootstrap3.client.ui.base.HasIconPosition;
import org.gwtbootstrap3.client.ui.base.HasPull;
import org.gwtbootstrap3.client.ui.base.HasSize;
import org.gwtbootstrap3.client.ui.base.HasTargetHistoryToken;
import org.gwtbootstrap3.client.ui.base.HasType;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Used so that we can mock in tests. Gin injects a bootstrap Button, and these are pass through.
 * 
 * @author jayhodgson
 *
 */
public interface Button extends HasEnabled, HasActive, HasType<ButtonType>, HasSize<ButtonSize>, HasDataTarget, HasClickHandlers, Focusable, HasAllMouseHandlers, HasTargetHistoryToken, HasHref, HasText, HasIcon, HasIconPosition, HasBadge, IsWidget, HasPull {
	void setVisible(boolean visible);

	void addStyleName(String style);
}
