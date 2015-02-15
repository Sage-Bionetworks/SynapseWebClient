package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageParamsPanelViewImpl implements ImageParamsPanelView {
	public interface ImageParamsPanelViewImplUiBinder extends UiBinder<Widget, ImageParamsPanelViewImpl> {}
	
	Widget widget;
	Presenter presenter;
	
	@UiField
	Button noneButton;
	@UiField
	Button leftButton;
	@UiField
	Button centerButton;
	@UiField
	Button rightButton;
	
	@Inject
	public ImageParamsPanelViewImpl(ImageParamsPanelViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
	}
	
	private void initClickHandlers() {
		noneButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.noneButtonClicked();
			}
		});
		
		leftButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.leftButtonClicked();
			}
		});
		
		centerButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.centerButtonClicked();
			}
		});
		rightButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.rightButtonClicked();
			}
		});
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
	public void setNoneButtonActive() {
		clearActive();
		noneButton.setActive(true);
	}

	@Override
	public void setLeftButtonActive() {
		clearActive();
		leftButton.setActive(true);
	}

	@Override
	public void setCenterButtonActive() {
		clearActive();
		centerButton.setActive(true);
	}

	@Override
	public void setRightButtonActive() {
		clearActive();
		rightButton.setActive(true);
	}
	
	private void clearActive() {
		noneButton.setActive(false);
		leftButton.setActive(false);
		centerButton.setActive(false);
		rightButton.setActive(false);
	}

}
