package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BiodallianceEditorViewImpl implements BiodallianceEditorView {
	public interface BiodallianceEditorViewImplUiBinder extends UiBinder<Widget, BiodallianceEditorViewImpl> {}
	private Presenter presenter;
	@UiField
	TextBox chrField;
	@UiField
	TextBox viewStartField;
	@UiField
	TextBox viewEndField;
	@UiField
	FlowPanel tracksContainer;
	@UiField
	Button addTrackButton;
	@UiField
	RadioButton humanButton;
	@UiField
	RadioButton mouseButton;
	
	Widget widget;
	
	
	@Inject
	public BiodallianceEditorViewImpl(BiodallianceEditorViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		addTrackButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.addTrackClicked();
			}
		});
	}
	
	@Override
	public void initView() {
	}

	
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if ("".equals(chrField.getValue())){
			throw new IllegalArgumentException("Missing chr");
		} else if ("".equals(viewStartField.getValue())){
			throw new IllegalArgumentException("Missing view start");
		} else if ("".equals(viewEndField.getValue())){
			throw new IllegalArgumentException("Missing view end");
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
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void addTrack(Widget w) {
		tracksContainer.add(w);
	}
	
	@Override
	public void clear() {
		tracksContainer.clear();
		chrField.setValue("");
		viewStartField.setValue("");
		viewEndField.setValue("");
	}

	public String getChr() {
		return chrField.getValue();
	}

	public void setChr(String chr) {
		this.chrField.setValue(chr);
	}

	public String getViewStart() {
		return viewStartField.getValue();
	}

	public void setViewStart(String viewStart) {
		this.viewStartField.setValue(viewStart);
	}

	public String getViewEnd() {
		return viewEndField.getValue();
	}

	public void setViewEnd(String viewEnd) {
		this.viewEndField.setValue(viewEnd);
	}
	@Override
	public boolean isHuman() {
		return humanButton.getValue();
	}
	@Override
	public void setHuman() {
		humanButton.setActive(true);
		humanButton.setValue(true, true);
		mouseButton.setActive(false);
	}
	
	@Override
	public boolean isMouse() {
		return mouseButton.getValue();
	}
	@Override
	public void setMouse() {
		mouseButton.setActive(true);
		mouseButton.setValue(true, true);
		humanButton.setActive(false);
	}
}
