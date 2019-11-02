package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.Styles;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.text.shared.testing.PassthroughParser;
import com.google.gwt.text.shared.testing.PassthroughRenderer;


public class NumberBox extends TextBox {
	public NumberBox() {
		this(Document.get().createTextInputElement());
	}

	public NumberBox(final Element element) {
		this(element, PassthroughRenderer.instance(), PassthroughParser.instance());
	}

	public NumberBox(Element element, Renderer<String> renderer, Parser<String> parser) {
		super(element, renderer, parser);
		setStyleName(Styles.FORM_CONTROL);

		getElement().setAttribute("type", "number");
	}

	public Double getNumberValue() {
		String v = getValue();
		if (v != null && !v.trim().isEmpty()) {
			return Double.parseDouble(v);
		} else {
			return null;
		}
	}
}
