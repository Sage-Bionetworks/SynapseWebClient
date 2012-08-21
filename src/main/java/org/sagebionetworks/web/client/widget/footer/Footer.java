package org.sagebionetworks.web.client.widget.footer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Footer implements FooterView.Presenter {

	private FooterView view;
	private CookieProvider cookies;
	private UserAccountServiceAsync userAccountService;
	
	@Inject
	public Footer(FooterView view, CookieProvider cookies, UserAccountServiceAsync userAccountService) {
		this.view = view;
		this.cookies = cookies;
		this.userAccountService = userAccountService;
		view.setPresenter(this);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	@Override
	public void gotoSupport() {
//		cookies.removeCookie(CookieKeys.FASTPASS);
		try {
			userAccountService.getFastPassSupportUrl(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					//TODO: communicate via cookie instead
//					cookies.setCookie(CookieKeys.FASTPASS, URL.encodeQueryString(result));
//					openSupportSite();
					Window.open("http://support.sagebase.org/sagebase?fastpass="+URL.encodeQueryString(result),"_blank","");
				}
				
				@Override
				public void onFailure(Throwable caught) {
					//failed, just go
					openWindowToSupportSite();
				}
			});
		} catch (RestServiceException e) {
			//if it fails, go to the support site without the fastpass url?
			openWindowToSupportSite();
		}
	}
	
	private static void openWindowToSupportSite(){
		Window.open("http://"+DisplayUtils.SUPPORT_URL,"_blank","");
	}
	
}
