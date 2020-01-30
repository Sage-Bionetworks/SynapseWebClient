package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageParamsPanelViewImpl implements ImageParamsPanelView {
	public interface ImageParamsPanelViewImplUiBinder extends UiBinder<Widget, ImageParamsPanelViewImpl> {
	}

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
	@UiField
	Div scaleSliderContainer;
	@UiField
	TextBox altText;
	Slider scaleSlider;

	@Inject
	public ImageParamsPanelViewImpl(ImageParamsPanelViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		initClickHandlers();
		setScale(100);
		altText.clear();
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

	@Override
	public Integer getScale() {
		return scaleSlider.getValue().intValue();
	}

	@Override
	public void setScale(Integer scale) {
		// Slider does not behave well when it is detached and then re-attached. Recreate.
		scaleSliderContainer.clear();
		scaleSlider = new Slider(1.0, 100.0, scale.doubleValue());
		scaleSlider.setStep(1.0);
		scaleSliderContainer.add(scaleSlider);
	}

	@Override
	public void setAltText(String altTextValue) {
		altText.setValue(altTextValue);
	}

	@Override
	public String getAltText() {
		return altText.getValue();
	}
}
