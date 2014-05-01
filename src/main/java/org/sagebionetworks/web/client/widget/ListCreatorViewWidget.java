package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;

public class ListCreatorViewWidget extends FlowPanel {
	private List<String> restrictedValues = new ArrayList<String>();		
	private UnorderedListPanel rvDisplayList = new UnorderedListPanel();
	private TextBox addRvBox;
	private boolean isSet;
		
	public ListCreatorViewWidget(String buttonLabel, boolean isSet) {
		this.isSet = isSet;
		
		rvDisplayList.addStyleName("list-group margin-top-10");
		rvDisplayList.setVisible(false);
		
		addRvBox = new TextBox();
		addRvBox.addStyleName("form-control");
		addRvBox.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	addRestrictedValue(addRvBox.getValue());
		        }
		    }
		});
		
		Button addRvBtn = DisplayUtils.createButton(buttonLabel);
		addRvBtn.addStyleName("right");
		addRvBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				addRestrictedValue(addRvBox.getValue());
			}

		});
		
		FlowPanel fieldWrap = new FlowPanel();
		fieldWrap.addStyleName("inlineField");
		fieldWrap.add(addRvBox);		
		add(addRvBtn);
		add(fieldWrap);
		add(rvDisplayList);		
	}
	
	public void append(String value) {
		addRestrictedValue(value);
	}	
	
	public void append(List<String> values) {
		for(String value : values) append(value);
	}
	
	public List<String> getValues() {
		return new ArrayList<String>(restrictedValues); 		
	}
	
	private void addRestrictedValue(final String value) {
		if(value != null && value.length() > 0) {			
			if(!isSet || !restrictedValues.contains(value)) {
				restrictedValues.add(value);
				final FlowPanel display = new FlowPanel();
				display.add(new InlineHTML(value));
				Anchor delete = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-remove right largeIconButton largeIconShiftUp\"></span>"));
				delete.addClickHandler(new ClickHandler() {				
					@Override
					public void onClick(ClickEvent event) {
						restrictedValues.remove(value);
						rvDisplayList.remove(display);
						if(restrictedValues.size() == 0) 
							rvDisplayList.setVisible(false);
					}
				});
				display.add(delete);
				rvDisplayList.add(display, "list-group-item");				
				rvDisplayList.setVisible(true);
			}

			addRvBox.setValue(null);
			addRvBox.setFocus(true);
		}
	}

}
