package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MathJaxConfigViewImpl extends LayoutContainer implements MathJaxConfigView {
	private Presenter presenter;
	private TextField<String> field;
	
	@Inject
	public MathJaxConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlign(VerticalAlignment.MIDDLE);
		field = new TextField<String>();
		field.setAllowBlank(false);
		Label urlLabel = new Label(DisplayConstants.EQUATION_LABEL);
		urlLabel.setWidth(30);
		field.setWidth(300);
		hp.add(urlLabel);
		hp.add(field);
		hp.addStyleName("margin-top-left-10");
		
		add(hp);
	}
	
	@Override
	public void checkParams() throws IllegalArgumentException {
		if (!field.isValid())
			throw new IllegalArgumentException(field.getErrorMessage());
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
	public int getDisplayHeight() {
		return 60;
	}
	@Override
	public int getAdditionalWidth() {
		return 0;
	}
	@Override
	public void clear() {
		if (field != null)
			field.setValue("");
	}

	@Override
	public String getEquation() {
		return field.getValue();
	}
	
	@Override
	public void setEquation(String equation) {
		if (equation != null)
			field.setValue(equation);
		else
			field.setValue("");
	}
	/*
	 * Private Methods
	 */

}
