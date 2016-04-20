package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VideoConfigViewImpl implements VideoConfigView {
	public interface VideoConfigViewImplUiBinder extends UiBinder<Widget, VideoConfigViewImpl> {}
	private Presenter presenter;
	@UiField
	TextBox mp4Entity;
	@UiField
	TextBox oggEntity;
	@UiField
	TextBox webmEntity;
	@UiField
	Button mp4Button;
	@UiField
	Button oggButton;
	@UiField
	Button webmButton;
	@UiField
	Anchor moreInfoLink;
	
	EntityFinder entityFinder;
	
	Widget widget;
	
	@Inject
	public VideoConfigViewImpl(VideoConfigViewImplUiBinder binder, 
			EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		mp4Button.addClickHandler(getClickHandler(mp4Entity));
		oggButton.addClickHandler(getClickHandler(oggEntity));
		webmButton.addClickHandler(getClickHandler(webmEntity));
		moreInfoLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(ClientProperties.VIDEO_HTML5_BROWSER_LINK, "", "");
			}
		});
	}
	
	@Override
	public void initView() {
	}

	public ClickHandler getClickHandler(final TextBox textBox) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(EntityFilter.FILE, false, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						textBox.setValue(selected.getTargetId());
						entityFinder.hide();
					}
				});
				entityFinder.show();
			}
		};
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if ("".equals(mp4Entity.getValue()) && "".equals(oggEntity.getValue()) && "".equals(webmEntity.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_ENTER_AT_LEAST_ONE_VIDEO_FILE);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public String getMp4Entity() {
		return mp4Entity.getValue();
	}
	
	@Override
	public void setMp4Entity(String mp4EntityString) {
		mp4Entity.setValue(mp4EntityString);
	}
	
	@Override
	public String getOggEntity() {
		return oggEntity.getValue();
	}
	
	@Override
	public void setOggEntity(String oggEntityString) {
		oggEntity.setValue(oggEntityString);
	}
	
	@Override
	public String getWebMEntity() {
		return webmEntity.getValue();
	}
	
	@Override
	public void setWebMEntity(String webMEntityString) {
		webmEntity.setValue(webMEntityString);
	}
	
	@Override
	public void clear() {
		mp4Entity.setValue("");
		oggEntity.setValue("");
		webmEntity.setValue("");
	}
	
}
