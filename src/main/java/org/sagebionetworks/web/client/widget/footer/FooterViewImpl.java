package org.sagebionetworks.web.client.widget.footer;

import java.util.Date;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
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
	@UiField
	SpanElement copyrightYear;
	@UiField
	SpanElement portalVersionSpan;
	@UiField
	SpanElement repoVersionSpan;
	
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
		copyrightYear.setInnerHTML(DateTimeFormat.getFormat("yyyy").format(new Date()));
	}
	
	private void addDebugModeLink() {
		final Anchor debugModeLink = new Anchor();
		debugModeLink.setText("@");
		debugModeLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isInTestWebsite(cookies)) {
					//verify
					DisplayUtils.showConfirmDialog(
							"Alpha Test Mode", 
							DisplayConstants.TEST_MODE_WARNING, 
							new Callback() {
								@Override
								public void invoke() {
									//switch to pre-release test website mode
									DisplayUtils.setTestWebsite(true, cookies);
									Window.scrollTo(0, 0);
									Window.Location.reload();
								}
							});
				} else {
					//switch back to standard mode
					DisplayUtils.setTestWebsite(false, cookies);
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

	@Override
	public void setVersion(String portalVersion, String repoVersion) {
		if(portalVersion == null) portalVersion = "--";
		if(repoVersion == null) repoVersion = "--";
		portalVersionSpan.setInnerHTML(portalVersion);
		repoVersionSpan.setInnerHTML(repoVersion);		
	}

}
