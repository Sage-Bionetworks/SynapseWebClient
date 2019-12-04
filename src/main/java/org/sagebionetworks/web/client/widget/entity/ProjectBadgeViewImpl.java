package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBadgeViewImpl implements ProjectBadgeView {

	public static final String ATTRIBUTE_PROJECT_ID = "data-project-id";

	public interface Binder extends UiBinder<Widget, ProjectBadgeViewImpl> {
	}

	@UiField
	Tooltip tooltip;
	@UiField
	Anchor anchor;
	@UiField
	Span additionalText;
	@UiField
	Span additionalTextUI;
	@UiField
	Span favoritesWidgetContainer;

	boolean isPopoverInitialized, isPopover;
	SageImageBundle sageImageBundle;

	Widget widget;
	DateTimeUtils dateTimeUtils;
	String projectId;
	public static PlaceChanger placeChanger = null;
	public static final ClickHandler PROJECT_BADGE_CLICKHANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			Anchor anchor = (Anchor) event.getSource();
			event.preventDefault();
			String projectId = anchor.getElement().getAttribute(ATTRIBUTE_PROJECT_ID);
			placeChanger.goTo(new Synapse(projectId));
		}
	};

	@Inject
	public ProjectBadgeViewImpl(final Binder uiBinder, SynapseJSNIUtils synapseJSNIUtils, SageImageBundle sageImageBundle, DateTimeUtils dateTimeUtils, GlobalApplicationState globalAppState) {
		widget = uiBinder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
		this.dateTimeUtils = dateTimeUtils;
		placeChanger = globalAppState.getPlaceChanger();
		anchor.addClickHandler(PROJECT_BADGE_CLICKHANDLER);
	}

	@Override
	public void configure(String projectName, String projectId) {
		anchor.setText(projectName);
		anchor.getElement().setAttribute(ATTRIBUTE_PROJECT_ID, projectId);
		this.projectId = projectId;
		anchor.setHref(DisplayUtils.getSynapseHistoryToken(projectId));
	}

	@Override
	public void setTooltip(String tooltipText) {
		tooltip.setTitle(tooltipText);
	}

	@Override
	public String getSimpleDateString(Date date) {
		return dateTimeUtils.getDateString(date);
	}

	@Override
	public void setLastActivityText(String text) {
		additionalText.setText(text);
	}

	@Override
	public void setLastActivityVisible(boolean isVisible) {
		additionalTextUI.setVisible(isVisible);
	}

	@Override
	public void setFavoritesWidget(Widget widget) {
		favoritesWidgetContainer.clear();
		favoritesWidgetContainer.add(widget);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void addStyleName(String style) {
		widget.addStyleName(style);
	}

	@Override
	public void addClickHandler(ClickHandler clickHandler) {
		anchor.addClickHandler(clickHandler);
	}
}
