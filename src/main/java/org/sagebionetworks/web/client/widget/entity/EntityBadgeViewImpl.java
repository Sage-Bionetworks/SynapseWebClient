package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadgeViewImpl extends Composite implements EntityBadgeView {
	SynapseJSNIUtils synapseJSNIUtils;
	SageImageBundle sageImageBundle;
	Widget modifiedByWidget;
	Presenter presenter;
	public interface Binder extends UiBinder<Widget, EntityBadgeViewImpl> {	}
	
	@UiField
	FocusPanel iconContainer;
	@UiField
	Icon icon;
	@UiField
	FlowPanel entityContainer;
	@UiField
	TextBox idField;
	@UiField
	SimplePanel modifiedByField;
	@UiField
	Label modifiedOnField;
	
	@UiField
	Tooltip annotationsField;
	@UiField
	Label sizeField;
	@UiField
	TextBox md5Field;
	@UiField
	Icon publicIcon;
	@UiField
	Icon privateIcon;
	@UiField
	Icon sharingSetIcon;
	@UiField
	Icon wikiIcon;
	@UiField
	Icon annotationsIcon;
	@UiField
	Tooltip errorField;
	@UiField
	Icon errorIcon;
	@UiField
	Span fileDownloadButtonContainer;
	@UiField
	Icon discussionIcon;
	@UiField
	Tooltip discussion;
	@UiField
	Icon deleteIcon;
	Callback onAttachCallback;
	Anchor entityAnchor;
	
	@Inject
	public EntityBadgeViewImpl(final Binder uiBinder,
			final SynapseJSNIUtils synapseJSNIUtils,
			SageImageBundle sageImageBundle, 
			PortalGinInjector ginInjector) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		initWidget(uiBinder.createAndBindUi(this));
		idField.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				idField.selectAll();
			}
		});
		md5Field.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				md5Field.selectAll();
			}
		});
		deleteIcon.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onDelete();
			}
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
	}
	
	@Override
	public void setEntity(final EntityHeader entityHeader) {
		clear();
		if(entityHeader == null)  throw new IllegalArgumentException("Entity is required");
		
		if(entityHeader != null) {
			entityAnchor = new Anchor();
			entityAnchor.setText(entityHeader.getName());
			entityAnchor.addStyleName("link");
			entityAnchor.setHref("#!Synapse:" + entityHeader.getId());
			
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					entityAnchor.fireEvent(event);
				}
			};
			iconContainer.setWidget(icon);
			iconContainer.addClickHandler(clickHandler);
			entityContainer.add(entityAnchor);
			idField.setText(entityHeader.getId());
		} 		
	}
	@Override
	public void setIcon(IconType iconType) {
		icon.setType(iconType);
	}
	
	@Override
	public void showLoadError(String principalId) {
		clear();
		entityContainer.add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	@Override
	public void showLoading() {
		clear();
		entityContainer.add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}

	@Override
	public void clear() {
		iconContainer.clear();
		entityContainer.clear();
	}
	
	@Override
	public void showLoadingIcon() {
		iconContainer.setWidget(new Image(sageImageBundle.loading16()));
	}
	
	@Override
	public void hideLoadingIcon() {
		iconContainer.setWidget(icon);
	}
	
	@Override
	public void addClickHandler(final ClickHandler handler) {
		entityAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.preventDefault();
				handler.onClick(event);
			}
		});
	}
	
	@Override
	public void setModifiedByWidget(Widget w) {
		modifiedByField.setWidget(w);
		this.modifiedByWidget = w;
	}
	
	@Override
	public void setModifiedOn(String modifiedOnString) {
		modifiedOnField.setText(modifiedOnString);
	}
	
	@Override
	public void setModifiedByWidgetVisible(boolean visible) {
		modifiedByWidget.setVisible(visible);
	}


	@Override
	public String getFriendlySize(Long contentSize, boolean abbreviatedUnits) {
		return DisplayUtils.getFriendlySize(contentSize, abbreviatedUnits);
	}
	
	@Override
	public void setAnnotations(String html) {
		annotationsField.setHtml(SafeHtmlUtils.fromTrustedString(html));
		annotationsField.reconfigure();
	}
	@Override
	public void showAnnotationsIcon() {
		annotationsIcon.setVisible(true);
	}
	
	@Override
	public void setError(String error) {
		errorField.setTitle(error);
		errorField.reconfigure();
	}
	@Override
	public void showErrorIcon() {
		errorIcon.setVisible(true);
	}
	
	@Override
	public void setSize(String s) {
		sizeField.setText(s);
	}
	@Override
	public void setMd5(String s) {
		md5Field.setText(s);
	}

	@Override
	public void showHasWikiIcon() {
		wikiIcon.setVisible(true);
	}
	@Override
	public void showPrivateIcon() {
		privateIcon.setVisible(true);
	}
	@Override
	public void showPublicIcon() {
		publicIcon.setVisible(true);
	}
	@Override
	public void showSharingSetIcon() {
		sharingSetIcon.setVisible(true);
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(this);
	}
	@Override
	public void setFileDownloadButton(Widget w) {
		fileDownloadButtonContainer.clear();
		fileDownloadButtonContainer.add(w);
	}

	@Override
	public void setDiscussionThreadIconVisible(boolean visible){
		discussionIcon.setVisible(visible);
	}
	@Override
	public void showDeleteIcon() {
		deleteIcon.setVisible(true);
	}
	

}
