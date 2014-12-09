package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReferenceConfigViewImpl extends LayoutContainer implements ReferenceConfigView {
	private Presenter presenter;
	private TextField<String> refField;
	private TextField<String> linkField;
	
	@Inject
	public ReferenceConfigViewImpl() {
	}

	@Override
	public void initView() {
		VerticalPanel vp = new VerticalPanel();
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		refField = new TextField<String>();
		refField.setAllowBlank(false);
		Label refLabel = new Label(DisplayConstants.REFERENCE_LABEL);
		refLabel.setWidth(60);
		refField.setWidth(270);
		hp.add(refLabel);
		hp.add(refField);
		hp.addStyleName("margin-top-left-10");
		vp.add(hp);
		add(vp);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if(!refField.isValid()) {
			throw new IllegalArgumentException(refField.getErrorMessage());
		}
	}

	@Override
	public void showLoading() {

	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
		if(refField != null) {
			refField.setValue("");
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setReference(String reference) {
		refField.setValue(reference);
	}

	@Override
	public String getReference() {
		return refField.getValue();
	}

}
