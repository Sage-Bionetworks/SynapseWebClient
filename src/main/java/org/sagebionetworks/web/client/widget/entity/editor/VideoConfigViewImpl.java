package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VideoConfigViewImpl extends FlowPanel implements VideoConfigView {
	private Presenter presenter;
	TextBox mp4Entity, oggEntity, webmEntity;
	IconsImageBundle iconsImageBundle;
	EntityFinder entityFinder;
	
	@Inject
	public VideoConfigViewImpl(IconsImageBundle iconsImageBundle, EntityFinder entityFinder) {
		this.iconsImageBundle = iconsImageBundle;
		this.entityFinder = entityFinder;
	}
	
	@Override
	public void initView() {
		clear();
		mp4Entity = new TextBox();
		oggEntity = new TextBox();
		webmEntity = new TextBox();
		add(new HTML("<h5 class=\"margin-10\">The browser viewing the video will use the first format that it recognizes. "+
				"<small>(<a class=\"link\" target=\"_blank\" href=\""+ClientProperties.VIDEO_HTML5_BROWSER_LINK+"\">more information</a>)</small></h5>"));
		
		add(initTextBox(mp4Entity, "Find MP4", "MPEG 4 files with H264 video codec and AAC audio codec"));
		add(initTextBox(oggEntity, "Find Ogg", "Ogg files with Theora video codec and Vorbis audio codec"));
		add(initTextBox(webmEntity, "Find WebM", "WebM files with VP8 video codec and Vorbis audio codec"));
	}

	private Widget initTextBox(final TextBox textBox, String label, String tooltip){
		LayoutContainer horizontalTable = new LayoutContainer();
		horizontalTable.addStyleName("margin-top-left-10");
		
		textBox.addStyleName("form-control inline-block");
		textBox.setWidth("200px");
		DisplayUtils.addToolTip(horizontalTable, tooltip);
		textBox.setEnabled(false);
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(false, new SelectedHandler<Reference>() {					
					@Override
					public void onSelected(Reference selected) {
						if(selected.getTargetId() != null) {					
							textBox.setValue(selected.getTargetId());
							entityFinder.hide();
						} else {
							showErrorMessage(DisplayConstants.PLEASE_MAKE_SELECTION);
						}
					}
				});
				entityFinder.show();
			}
		};
		textBox.addClickHandler(clickHandler);
		Button searchButton = DisplayUtils.createButton(label);
		searchButton.addClickHandler(clickHandler);
		
		// add to table and page
		horizontalTable.add(textBox);
		horizontalTable.add(searchButton);
		
		return horizontalTable;
		
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if ("".equals(mp4Entity.getValue()) && "".equals(oggEntity.getValue()) && "".equals(webmEntity.getValue()))
			throw new IllegalArgumentException(DisplayConstants.ERROR_ENTER_AT_LEAST_ONE_VIDEO_FILE);
	}

	@Override
	public Widget asWidget() {
		return this;
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
	
}
