package org.sagebionetworks.web.server.servlet.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

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
	private ServletContext sc;
	long cacheTimeout = 1000*60*60*24*14; //2 weeks

	@Override
	public void destroy() {
		this.sc = null;
		webClient=null;
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
			
			//check the cache
			String id = uri + queryString;
			File tempDir = (File) sc.getAttribute("javax.servlet.context.tempdir");
			String temp = tempDir.getAbsolutePath();
			File file = new File(temp + id);
			FileWriter fw = null;
			try {
				long now = Calendar.getInstance().getTimeInMillis();
				// set timestamp check
				if (!file.exists() || cacheTimeout < now - file.lastModified()) {
					//not in the cache, or it has expired.  make sure the file exists
					String name = file.getAbsolutePath();
					name = name.substring(0, name.lastIndexOf("/"));
					new File(name).mkdirs();
					
					//get html page, and write it out
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
						String xml = page.asXml();
						//replace all relative links with full links due to this Google AJAX crawler support chicken-dance
						String originalUrl = url.toString();
						String toPage = originalUrl.substring(0, originalUrl.indexOf("#")+1);
						String replacedWithFullHrefs = xml.replace("href=\"#", "href=\""+toPage);
						fw = new FileWriter(file);
						fw.write(replacedWithFullHrefs);
						webClient.closeAllWindows();
					}
				}
			} catch (IOException e) {
				if (!file.exists()) {
					throw e;
				}
			}
			finally {
				if (fw != null) {
					fw.flush();
					fw.close();
				}
			}
			//now read from the cache
			FileInputStream fis = new FileInputStream(file);
			String mt = sc.getMimeType(uri);
			response.setContentType(mt);
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			ServletOutputStream out = httpResponse.getOutputStream();
			IOUtils.copy(fis, out);
			out.flush();
			out.close();
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
			String result = unescapedAmp.replace(ESCAPED_FRAGMENT, "#!");
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
	    this.sc = config.getServletContext();
    }
}
