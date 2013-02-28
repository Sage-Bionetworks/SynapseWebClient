package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.UrlFetchWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This filter detects ajax crawler (Google).  If so, it takes over the renders the javascript page and handles the response.
 *
 */
public class CrawlFilter implements Filter {

	public static final String ESCAPED_FRAGMENT = "_escaped_fragment_=";
	private WebClient webClient;
	
	@Override
	public void destroy() {
		// nothing to do
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRqst = (HttpServletRequest) request;
		// Is this an ugly url that we need to convert/handle?
		String queryString = httpRqst.getQueryString();
		if (queryString != null && queryString.contains(ESCAPED_FRAGMENT)) {
			String uri = httpRqst.getRequestURI();
			int port = request.getServerPort();
			String domain = request.getServerName();
			String scheme = request.getScheme();
			String fixedQueryString = uri + rewriteQueryString(queryString);
			URL url = new URL(scheme, domain, port, fixedQueryString);

			HtmlPage page = null;
			try {
				//url = new URL("http://localhost:8080/portal-develop-SNAPSHOT/Portal.html#!Home:0");
				page = webClient.getPage(url);
				
				int n = webClient.waitForBackgroundJavaScript(15000);
				webClient.getJavaScriptEngine().pumpEventLoop(15000);
			} catch (ScriptException e) {
			}
			if (page != null) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				httpResponse.setContentType("text/html;charset=UTF-8");
				ServletOutputStream out = httpResponse.getOutputStream();
				String xml = page.asXml();
				//replace all relative links with full links due to this Google AJAX crawler support chicken-dance
				String originalUrl = url.toString();
				String toPage = originalUrl.substring(0, originalUrl.indexOf("#")+1);
				String replacedWithFullHrefs = xml.replace("href=\"#", "href=\""+toPage);
				out.println(replacedWithFullHrefs);
				out.flush();
				webClient.closeAllWindows();
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	public String rewriteQueryString(String uglyUrl) {
		try {
			String decoded = URLDecoder.decode(uglyUrl, "UTF-8");
			// dev mode
			String gwt = decoded.replace("gwt", "?gwt");
			String unescapedAmp = gwt.replace("&" + ESCAPED_FRAGMENT, "#!");
			String result = unescapedAmp.replace("?" + ESCAPED_FRAGMENT, "#!");
			return result;
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
	    webClient.setWebConnection(new UrlFetchWebConnection(webClient));
		//and configure it to chill out
		webClient.setCssEnabled(false);
	    webClient.setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String arg0, Object arg1) {
			}
		});
	    webClient.setHTMLParserListener(new HTMLParserListener() {
			
			@Override
			public void warning(String arg0, URL arg1, int arg2, int arg3, String arg4) {
			}
			
			@Override
			public void error(String arg0, URL arg1, int arg2, int arg3, String arg4) {
			}
		});
	    //even setting these to false, it throws a ScriptException (even if the page fully loads)
	    webClient.setThrowExceptionOnFailingStatusCode(false);
	    webClient.setThrowExceptionOnScriptError(false);
    }
}
