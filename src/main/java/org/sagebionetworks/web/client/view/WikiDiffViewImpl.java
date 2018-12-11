package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.WikiVersionAnchorListItem;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiDiffViewImpl implements WikiDiffView {
	public interface WikiDiffViewImplUiBinder extends UiBinder<Widget, WikiDiffViewImpl> {}
	@UiField
	Div synAlertContainer;
	@UiField
	Panel diffContainer;
	@UiField
	DropDownMenu version1Dropdown;
	@UiField
	DropDownMenu version2Dropdown;
	@UiField
	Button version1DropdownButton;
	@UiField
	Button version2DropdownButton;
	
	private Header headerWidget;
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	Widget w;
	public static final String DEFAULT_BUTTON_TEXT = "Select a version...";
	public final ClickHandler VERSION_1_CLICKHANDLER = event -> {
		event.preventDefault();
		Widget panel = (Widget)event.getSource();
		String wikiVersion = panel.getElement().getAttribute(WikiVersionAnchorListItem.WIKI_VERSION_ATTRIBUTE);
		presenter.onVersion1Selected(wikiVersion);
	};
	
	public final ClickHandler VERSION_2_CLICKHANDLER = event -> {
		event.preventDefault();
		Widget panel = (Widget)event.getSource();
		String wikiVersion = panel.getElement().getAttribute(WikiVersionAnchorListItem.WIKI_VERSION_ATTRIBUTE);
		presenter.onVersion2Selected(wikiVersion);
	};
	@Inject
	public WikiDiffViewImpl(
			WikiDiffViewImplUiBinder binder,
			Header headerWidget,
			PortalGinInjector ginInjector) {
		w = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.ginInjector = ginInjector;
		headerWidget.configure();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void setSynAlert(IsWidget synAlert) {
		synAlertContainer.clear();
		synAlertContainer.add(synAlert);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void clear() {
		version1Dropdown.clear();
		version2Dropdown.clear();
	}
	
	@Override
	public void setVersion1(String version) {
		String buttonText = version == null ? DEFAULT_BUTTON_TEXT : version;
		version1DropdownButton.setText(buttonText);
	}
	@Override
	public void setVersion2(String version) {
		String buttonText = version == null ? DEFAULT_BUTTON_TEXT : version;
		version2DropdownButton.setText(buttonText);
	}
	@Override
	public void showDiff(String markdown1, String markdown2) {
		_showDiff(
				diffContainer.getElement(),
				version1DropdownButton.getText(),
				version2DropdownButton.getText(),
				markdown1, 
				markdown2);
	}
	private static native void _showDiff(Element outputDivElement, String baseTextTitle, String newTextTitle, String markdown1, String markdown2) /*-{
		//split text into lines
	    var base = $wnd.difflib.stringAsLines(markdown1);
	    var newtxt = $wnd.difflib.stringAsLines(markdown2);
	    var sm = new $wnd.difflib.SequenceMatcher(base, newtxt);
	    // get the opcodes from the SequenceMatcher instance
	    // opcodes is a list of 3-tuples describing what changes should be made to the base text
	    // in order to yield the new text
	    var opcodes = sm.get_opcodes();
	    var diffoutputdiv = outputDivElement;
	    while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);
	    var contextSize = null;
	    
	    diffoutputdiv.appendChild($wnd.diffview.buildView({
	        baseTextLines: base,
	        newTextLines: newtxt,
	        opcodes: opcodes,
	        baseTextName: baseTextTitle,
	        newTextName: newTextTitle,
	        contextSize: contextSize,
	        viewType: 1 //1=inline, 0=split
	    }));
	}-*/;

	@Override
	public void setVersionHistory(List<V2WikiHistorySnapshot> wikiVersionHistory) {
		addVersions(version1Dropdown, VERSION_1_CLICKHANDLER, wikiVersionHistory);
		addVersions(version2Dropdown, VERSION_2_CLICKHANDLER, wikiVersionHistory);
	}
	
	private void addVersions(DropDownMenu dropdown, ClickHandler clickHandler, List<V2WikiHistorySnapshot> wikiVersionHistory) {
		dropdown.clear();
		for (V2WikiHistorySnapshot v2WikiHistorySnapshot : wikiVersionHistory) {
			WikiVersionAnchorListItem item = ginInjector.getWikiVersionAnchorListItem();
			item.setV2WikiHistorySnapshot(v2WikiHistorySnapshot);
			item.addClickHandler(clickHandler);
			dropdown.add(item);
		}
	}
}
