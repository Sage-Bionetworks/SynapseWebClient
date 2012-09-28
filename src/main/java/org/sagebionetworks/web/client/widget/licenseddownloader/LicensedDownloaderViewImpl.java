package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.GovernanceDialogHelper;
import org.sagebionetworks.web.shared.FileDownload;

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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LicensedDownloaderViewImpl extends LayoutContainer implements LicensedDownloaderView {

	private Presenter presenter;
	private APPROVAL_REQUIRED approvalRequired;	
	private String licenseTextHtml;
	private SafeHtml safeDownloadHtml;
	private Window downloadWindow;
	private LayoutContainer downloadContentContainer;
	private IconsImageBundle icons;
	private SageImageBundle sageImageBundle;
	private int downloadWindowWidth;

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
		if (!presenter.isDownloadAllowed()) return;
		
		if (approvalRequired==APPROVAL_REQUIRED.NONE) {
			createDownloadWindow();
			downloadWindow.show();
		} else {
			Callback termsOfUseCallback = presenter.getTermsOfUseCallback();
			GovernanceDialogHelper.showAccessRequirement(
					approvalRequired, 
					false/*isAnonymous*/, 
					false/*hasAdministrativeAccess*/,
					false/*accessApproved*/, 
					icons, 
					licenseTextHtml, 
					null/*imposeRestrictionsCallback*/, 
					termsOfUseCallback, 
					presenter.getRequestAccessCallback(), 
					null/*loginCallback*/, 
					null/*jiraFlagLink*/);
		}
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
 
	@Override
	public Widget asWidget() {
		final LicensedDownloaderView view = this;
		Button downloadButton = new Button(DisplayConstants.BUTTON_DOWNLOAD, AbstractImagePrototype.create(icons.NavigateDown16()));		
		downloadButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				view.showWindow();
			}
		});		
		return downloadButton;
	}

	@Override
	public void setApprovalRequired(APPROVAL_REQUIRED approvalRequired) {
		this.approvalRequired = approvalRequired;		
	}

	@Override
	public void setLicenseHtml(String licenseHtml) {
		this.licenseTextHtml = licenseHtml;
	}
	
	@Deprecated
	@Override
	public void setDownloadUrls(List<FileDownload> downloads) {
		if(downloads != null && downloads.size() > 0) {
			// build a list of links in HTML
			SafeHtmlBuilder sb = new SafeHtmlBuilder();
			for(int i=0; i<downloads.size(); i++) {
				FileDownload dl = downloads.get(i);
				sb.appendHtmlConstant("<a href=\"" + dl.getUrl() + "\" target=\"new\">")
				.appendEscaped(dl.getDisplay())
				.appendHtmlConstant("</a> " + AbstractImagePrototype.create(icons.external16()).getHTML());
				if(dl.getChecksum() != null) {
					sb.appendHtmlConstant("&nbsp;<small>md5 checksum: ")
					.appendEscaped(dl.getChecksum())
					.appendHtmlConstant("</small>");
				}
				sb.appendHtmlConstant("<br/>");				
			}
			safeDownloadHtml = sb.toSafeHtml();

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
	public void setDownloadLocations(List<LocationData> locations, String md5) {
		if(locations != null && locations.size() > 0) {
			// build a list of links in HTML
			SafeHtmlBuilder sb = new SafeHtmlBuilder();
			String displayString = "Download";  // TODO : add display to LocationData
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
		licenseTextHtml = "";
		safeDownloadHtml = SafeHtmlUtils.fromSafeConstant("");		
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
		downloadWindow.setBlinkModal(true);
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
