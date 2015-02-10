package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.provenance.ProvViewUtil;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	
	boolean isPopoverInitialized;
	boolean isPopover;
	SageImageBundle sageImageBundle;
	
	Widget widget;
	@Inject
	public ProjectBadgeViewImpl(final Binder uiBinder,
			SynapseJSNIUtils synapseJSNIUtils,
			SageImageBundle sageImageBundle
			) {
		widget = uiBinder.createAndBindUi(this);
		this.sageImageBundle = sageImageBundle;
		
		isPopover = false;
		anchor.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				showPopover();
				isPopover = true;
			}
		});
		anchor.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				isPopover = false;
				tooltip.hide();
			}
		});
		
		anchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.entityClicked();
			}
		});
	}
	
	@Override
	public void setProject(String projectName, String projectId) {
		isPopoverInitialized = false;
		tooltip.setIsHtml(true);
		tooltip.setTitle(projectName);
		tooltip.setText(DisplayUtils.getLoadingHtml(sageImageBundle));
		anchor.setText(projectName);
	}
	
	@Override
	public String getSimpleDateString(Date date) {
		return DisplayUtils.converDateaToSimpleString(date);
	}
	
	@Override
	public void setLastActivityText(String text) {
		additionalText.setText(text);
	}
	
	public void showPopover() {
		if (!isPopoverInitialized) {
			presenter.getInfo(new AsyncCallback<KeyValueDisplay<String>>() {						
				@Override
				public void onSuccess(KeyValueDisplay<String> result) {
					renderPopover(ProvViewUtil.createEntityPopoverHtml(result).asString());
				}
				
				@Override
				public void onFailure(Throwable caught) {
					renderPopover(DisplayConstants.DETAILS_UNAVAILABLE);						
				}
				
				private void renderPopover(final String content) {
					isPopoverInitialized = true;
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							tooltip.setText(content);
							tooltip.reconfigure();
							if (isPopover)
								tooltip.show();
						}
					});
				}
			});
		}
		tooltip.show();
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
	/*
	 * Private Methods
	 */

}
