package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBadgeViewImpl implements ProjectBadgeView {
	
	private Presenter presenter;
	
	public interface Binder extends UiBinder<Widget, ProjectBadgeViewImpl> {	}
	
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
	@Inject
	public ProjectBadgeViewImpl(final Binder uiBinder,
			SynapseJSNIUtils synapseJSNIUtils,
			SageImageBundle sageImageBundle
			) {
		widget = uiBinder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
	}
	
	@Override
	public void setProject(String projectName, String href) {
		isPopoverInitialized = false;
		anchor.setText(projectName);
		anchor.setHref(href);
	}
	
	@Override
	public void configure(String projectName, String href, String tooltip) {
		anchor.setText(projectName);
		anchor.setHref(href);
		this.tooltip.setTitle(tooltip);
	}
	
	@Override
	public String getSimpleDateString(Date date) {
		return DisplayUtils.converDateaToSimpleString(date);
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

}
