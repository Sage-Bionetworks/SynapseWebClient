package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.shared.WidgetConstants;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageParamsPanel implements ImageParamsPanelView.Presenter {

	String selectedAlignment;
	ImageParamsPanelView view;

	@Inject
	public ImageParamsPanel(ImageParamsPanelView view) {
		this.view = view;
		view.setPresenter(this);
		setAlignment(WidgetConstants.FLOAT_NONE);
	}

	public String getAlignment() {
		return selectedAlignment;
	}

	public void setAlignment(String alignmentValue) {
		selectedAlignment = alignmentValue;
		if (WidgetConstants.FLOAT_LEFT.equals(alignmentValue)) {
			view.setLeftButtonActive();
		} else if (WidgetConstants.FLOAT_CENTER.equals(alignmentValue)) {
			view.setCenterButtonActive();
		} else if (WidgetConstants.FLOAT_RIGHT.equals(alignmentValue)) {
			view.setRightButtonActive();
		} else {
			view.setNoneButtonActive();
		}
	}

	@Override
	public void centerButtonClicked() {
		setAlignment(WidgetConstants.FLOAT_CENTER);
	}

	@Override
	public void leftButtonClicked() {
		setAlignment(WidgetConstants.FLOAT_LEFT);
	}

	@Override
	public void noneButtonClicked() {
		setAlignment(WidgetConstants.FLOAT_NONE);
	}

	@Override
	public void rightButtonClicked() {
		setAlignment(WidgetConstants.FLOAT_RIGHT);
	}

	public void setScale(Integer scale) {
		view.setScale(scale);
	}

	public Integer getScale() {
		return view.getScale();
	}

	public String getAltText() {
		return view.getAltText();
	}

	public void setAltText(String altText) {
		view.setAltText(altText);
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void clear() {
		setAlignment(WidgetConstants.FLOAT_NONE);
		setScale(100);
	}
}

