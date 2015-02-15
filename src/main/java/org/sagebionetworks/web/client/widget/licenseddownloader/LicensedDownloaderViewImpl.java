package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LicensedDownloaderViewImpl extends LayoutContainer implements LicensedDownloaderView {

	private Presenter presenter;
	private SafeHtml safeDownloadHtml;
	private Window downloadWindow;
	private LayoutContainer downloadContentContainer;
	private IconsImageBundle icons;
	private SageImageBundle sageImageBundle;
	private int downloadWindowWidth;
	private List<LocationData> locations;
	private String directDownloadURL;
	
	/*
	 * Constructors
	 */
	@Inject
	public LicensedDownloaderViewImpl(IconsImageBundle icons, SageImageBundle sageImageBundle) {
		this.icons = icons;
		this.sageImageBundle = sageImageBundle;
		clear();
	}

	
	/**
	 * This method isn't used as the view is shown via showWindow() 
	 */
	@Override
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);		
	}

	/*
	 * View Impls
	 */
	@Override
	public void showWindow() {
		if (directDownloadURL != null) {
			DisplayUtils.newWindow(directDownloadURL, "", "");
		} else {
			createDownloadWindow();
			downloadWindow.show();
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
 
	@Override
	public Widget asWidget() {
		final LicensedDownloaderView view = this;
		Image downloadButton = new Image(icons.NavigateDown16());
		downloadButton.addStyleName("imageButton");
		downloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.showWindow();
			}
		});
				
		return downloadButton;
	}

	@Override
	public void setDownloadLocation(String md5, String directDownloadUrl) {
		this.directDownloadURL = directDownloadUrl;
		// build a list of links in HTML
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		String displayString = "Download";
		sb.appendHtmlConstant("<a href=\"" + directDownloadURL + "\" target=\"_blank\">") 
		.appendEscaped(displayString)
		.appendHtmlConstant("</a> " + AbstractImagePrototype.create(icons.external16()).getHTML());
		downloadWindowWidth = 300;
		if(md5 != null) {
			sb.appendHtmlConstant("&nbsp;<small>md5 checksum: ")
			.appendEscaped(md5)
			.appendHtmlConstant("</small>");
			sb.appendHtmlConstant("<br/>");
			downloadWindowWidth = 600;
		}
		safeDownloadHtml = sb.toSafeHtml();
		
		// replace the view content if this is after initialization
		if(downloadContentContainer != null) {
			safeDownloadHtml = sb.toSafeHtml();
			fillDownloadContentContainer();
		}

	}
	
	@Override
	public String getDirectDownloadURL() {
		return directDownloadURL;
	}
	
	@Override
	@Deprecated
	public void setDownloadLocations(List<LocationData> locations, String md5) {
		this.locations = locations;
		if(locations != null && locations.size() > 0) {
			// build a list of links in HTML
			SafeHtmlBuilder sb = new SafeHtmlBuilder();
			String displayString = "Download";
			for(int i=0; i<locations.size(); i++) {
				LocationData dl = locations.get(i);
				sb.appendHtmlConstant("<a href=\"" + dl.getPath() + "\" target=\"_blank\">")
				.appendEscaped(displayString)
				.appendHtmlConstant("</a> " + AbstractImagePrototype.create(icons.external16()).getHTML());
				if(md5 != null) {
					sb.appendHtmlConstant("&nbsp;<small>md5 checksum: ")
					.appendEscaped(md5)
					.appendHtmlConstant("</small>");
				}
				sb.appendHtmlConstant("<br/>");				
			}
			if (locations.size() == 1) {
				directDownloadURL = locations.get(0).getPath();
			} else directDownloadURL = null;
			safeDownloadHtml = sb.toSafeHtml();
			if (md5 == null)
				downloadWindowWidth = 300;
			else
				downloadWindowWidth = 600;
			
			// replace the view content if this is after initialization
			if(downloadContentContainer != null) {
				safeDownloadHtml = sb.toSafeHtml();
				fillDownloadContentContainer();
			}
		} else {
			setNoDownloads();
		}
	}
	
	@Override
	public void clear() {
		// defaults
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant("");
		locations = null;
	}

	@Override
	public void showDownloadFailure() {
		// TODO Auto-generated method stub		
	}


	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	
	@Override
	public void showDownloadsLoading() {
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading16()) + " Loading...");
		fillDownloadContentContainer();
	}
	
	@Override
	public void setNoDownloads() {
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant(DisplayConstants.TEXT_NO_DOWNLOADS);
		fillDownloadContentContainer();		
	}

	@Override
	public void setUnauthorizedDownloads() {
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant(DisplayConstants.TEXT_UNAUTH_DOWNLOADS);
		fillDownloadContentContainer();		
	}

	@Override
	public void setNeedToLogIn() {
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant(DisplayConstants.ERROR_LOGIN_REQUIRED);
	}

	/*
	 * Protected Methods
	 */	

	protected void createDownloadWindow() {
		downloadWindow = new Window();
		downloadWindow.setSize(downloadWindowWidth, 200);
		downloadWindow.setPlain(true);
		downloadWindow.setModal(false);
		downloadWindow.setHeading("Download");
		downloadWindow.setLayout(new FitLayout());
		downloadWindow.setResizable(false);		
		
		RowData standardPadding = new RowData();
		standardPadding.setMargins(new Margins(15, 15, 0, 15));
		
		ContentPanel panel = new ContentPanel();		
		panel.setLayoutData(new RowLayout(Orientation.VERTICAL));		
		panel.setBorders(false);
		panel.setBodyBorder(false);
		panel.setHeaderVisible(false);
		
		downloadContentContainer = new LayoutContainer();
		downloadContentContainer.setHeight(100);
		downloadContentContainer.addStyleName("pad-text");
		downloadContentContainer.setStyleAttribute("backgroundColor", "white");
		downloadContentContainer.setBorders(false);
		downloadContentContainer.setScrollMode(Style.Scroll.AUTOY);
		if(safeDownloadHtml == null || safeDownloadHtml.asString().equals("")) {
			setNoDownloads();
		} else {
			fillDownloadContentContainer();
		}
		panel.add(downloadContentContainer, standardPadding);
		
		Button cancelLicenseButton = new Button("Close", new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						downloadWindow.hide();
					}
				});

		downloadWindow.addButton(cancelLicenseButton);
		downloadWindow.add(panel);		
		downloadWindow.layout(true);
	}
	
	private void fillDownloadContentContainer() {
		if(downloadContentContainer != null) {
			downloadContentContainer.removeAll();				
			downloadContentContainer.add(new HTML(safeDownloadHtml));
			downloadContentContainer.layout(true);
		}
	}


}
