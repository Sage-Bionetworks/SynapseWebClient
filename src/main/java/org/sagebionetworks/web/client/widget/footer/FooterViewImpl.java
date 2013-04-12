package org.sagebionetworks.web.client.widget.footer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.CookieProviderUtils;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FooterViewImpl extends Composite implements FooterView {

	public interface Binder extends UiBinder<Widget, FooterViewImpl> {
	}

	@UiField
	ScriptElement searchScript;
	
	@UiField
	FlowPanel debugModePanel;
	
	private Presenter presenter;
	private CookieProvider cookies;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public FooterViewImpl(Binder binder, CookieProvider cookies, SynapseJSNIUtils synapseJSNIUtils) {
		this.initWidget(binder.createAndBindUi(this));
		searchScript.setText("var TRANSMART_SEARCH = \"http://transmart.sagebase.org/transmart/search/search?sourcepage=search&id=\";		$(function() {			$( \"#query\" ).autocomplete({				source: function( request, response ) {					$.ajax({						url: \"http://transmart.sagebase.org/transmart/search/loadSearch\",						dataType: \"jsonp\",						data: {							query: \"all:\" + request.term												},						success: function( data ) {							response( $.map( data.rows, function( item ) {								return {									label: item.display + \": \" + item.keyword,									value: item.name,									id: item.id								}							}));						}					});				},				minLength: 1,				select: function( event, ui ) {					if(ui.item.id) {											document.location =  TRANSMART_SEARCH + ui.item.id;					}				}			});		});");
		this.cookies = cookies;
		this.synapseJSNIUtils = synapseJSNIUtils;
		addDebugModeLink();
	}
	
	private void addDebugModeLink() {
		final Anchor debugModeLink = new Anchor();
		debugModeLink.addStyleName("link");
		debugModeLink.setText("@");
		debugModeLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!CookieProviderUtils.isInTestWebsite(cookies)) {
					//verify
					MessageBox box = new MessageBox();
				    box.setButtons(MessageBox.YESNO);
				    box.setIcon(MessageBox.WARNING);
				    box.setTitle("Alpha Test Mode");
				    box.addCallback(new Listener<MessageBoxEvent>() {					
						@Override
						public void handleEvent(MessageBoxEvent be) { 												
							Button btn = be.getButtonClicked();
							if(Dialog.YES.equals(btn.getItemId())) {
								//switch to pre-release test website mode
								CookieProviderUtils.setTestWebsite(true, cookies);
								Window.Location.reload();
							}
						}
					});
				    box.setMessage(DisplayConstants.TEST_MODE_WARNING);
				    box.show();
				} else {
					//switch back to standard mode
					CookieProviderUtils.setTestWebsite(false, cookies);
					Window.Location.reload();
				}
				
			}
		});
		debugModePanel.addStyleName("inline-block");
		debugModePanel.add(debugModeLink);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
