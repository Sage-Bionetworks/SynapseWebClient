package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.Reference;
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
	public interface VideoConfigViewImplUiBinder extends UiBinder<Widget, VideoConfigViewImpl> {
	}

	private Presenter presenter;
	@UiField
	TextBox entity;
	@UiField
	Button button;
	@UiField
	Heading videoFormatWarning;
	EntityFinder entityFinder;
	@UiField
	TabListItem synapseTabListItem;
	@UiField
	TabListItem youtubeTabListItem;
	@UiField
	TabListItem vimeoTabListItem;
	@UiField
	TabPane tab1;
	@UiField
	TabPane tab2;
	@UiField
	TabPane tab3;

	@UiField
	TextBox youtubeUrlField;
	@UiField
	TextBox vimeoUrlField;
	Widget widget;

	@Inject
	public VideoConfigViewImpl(VideoConfigViewImplUiBinder binder, EntityFinder entityFinder) {
		widget = binder.createAndBindUi(this);
		this.entityFinder = entityFinder;
		button.addClickHandler(getClickHandler(entity));
	}

	@Override
	public void initView() {
		youtubeUrlField.setValue("");
		vimeoUrlField.setValue("");
	}

	public ClickHandler getClickHandler(final TextBox textBox) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				entityFinder.configure(EntityFilter.ALL_BUT_LINK, false, new SelectedHandler<Reference>() {
					@Override
					public void onSelected(Reference selected) {
						presenter.validateSelection(selected);
					}
				});
				entityFinder.show();
			}
		};
	}

	@Override
	public void hideFinder() {
		entityFinder.hide();
	}

	@Override
	public void checkParams() throws IllegalArgumentException {
		if ((isSynapseEntity() && entity.getValue().trim().isEmpty()) || (isYouTubeVideo() && youtubeUrlField.getValue().trim().isEmpty()) || (isVimeoVideo() && vimeoUrlField.getValue().trim().isEmpty())) {
			throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_VIDEO_FILE);
		}
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
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public String getEntity() {
		return entity.getValue();
	}

	@Override
	public void setEntity(String entityString) {
		entity.setValue(entityString);
	}

	@Override
	public void clear() {
		entity.setValue("");
		setVideoFormatWarningVisible(false);
	}

	@Override
	public void setVideoFormatWarningVisible(boolean visible) {
		videoFormatWarning.setVisible(visible);
	}

	@Override
	public void showFinderError(String error) {
		entityFinder.showError(error);
	}

	@Override
	public String getVimeoVideoUrl() {
		return vimeoUrlField.getValue();
	}

	@Override
	public String getYouTubeVideoUrl() {
		return youtubeUrlField.getValue();
	}

	@Override
	public boolean isSynapseEntity() {
		return synapseTabListItem.isActive();
	}

	@Override
	public boolean isVimeoVideo() {
		return vimeoTabListItem.isActive();
	}

	@Override
	public boolean isYouTubeVideo() {
		return youtubeTabListItem.isActive();
	}

	@Override
	public void setVimeoVideoUrl(String value) {
		vimeoUrlField.setValue(value);
	}

	@Override
	public void setYouTubeVideoUrl(String value) {
		youtubeUrlField.setValue(value);
	}

	@Override
	public void showSynapseTab() {
		synapseTabListItem.setActive(true);
		tab1.setActive(true);
	}

	@Override
	public void showYouTubeTab() {
		youtubeTabListItem.setActive(true);
		tab2.setActive(true);
	}

	@Override
	public void showVimeoTab() {
		vimeoTabListItem.setActive(true);
		tab3.setActive(true);
	}
}
