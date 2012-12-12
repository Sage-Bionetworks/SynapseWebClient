package org.sagebionetworks.web.client.utils;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface AnimationProtectorView {

	public HandlerRegistration addClickHandler(ClickHandler handler);

	public boolean isContainerVisible();

	public boolean isContainerRendered();

	public void setContainerVisible(boolean setVisible);

	public void slideContainerIn(Direction direction, FxConfig config);

	public void slideContainerOut(Direction direction, FxConfig config);

}
