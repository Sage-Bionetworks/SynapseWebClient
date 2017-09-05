package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.util.SerializationUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

/**
 * Handles given an alias, will redirect to the profile or team page assocated with that alias
 *
 * @author jay
 *
 */
public class AliasRedirectorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	
	/**
	 * Injected with Gin
	 */
	@SuppressWarnings("unused")
	private ServiceUrlProvider urlProvider;
	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	
	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Essentially the constructor. Setup synapse client.
	 *
	 * @param provider
	 */
	@Inject
	public void setServiceUrlProvider(ServiceUrlProvider provider) {
		this.urlProvider = provider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		AliasRedirectorServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy
		HttpServletRequest httpRqst = (HttpServletRequest)request;
		URL requestURL = new URL(httpRqst.getRequestURL().toString());
		try {
			String alias = httpRqst.getParameter("alias");
			SynapseClient client = createNewClient();
			perThreadRequest.set(httpRqst);
			
			// TODO: use new service call to resolve
//			List<UserGroupHeader> ughList = client.getUserGroupHeadersByAliases(Collections.singletonList(alias));
			UserGroupHeader temp = new UserGroupHeader(); 
			temp.setOwnerId(alias);
			temp.setIsIndividual(true);
			List<UserGroupHeader> ughList = Collections.singletonList(temp);
			if (!ughList.isEmpty()) {
				UserGroupHeader ugh = ughList.get(0);
				String place = ugh.getIsIndividual() ? "/#!Profile:" : "/#!Team:";
				StringBuilder newPathBuilder = new StringBuilder();
				newPathBuilder.append(place);
				newPathBuilder.append(ugh.getOwnerId());
				String newPath = newPathBuilder.toString();
				URL redirectURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), newPath);
				response.sendRedirect(response.encodeRedirectURL(redirectURL.toString()));
			}
		} catch (Exception e) {
			//redirect to error place with an entry
			LogEntry entry = new LogEntry();
			entry.setLabel("Sorry");
			entry.setMessage(e.getMessage());
//			entry.setStacktrace(ExceptionUtils.getStackTrace(e));
			String entryString = SerializationUtils.serializeAndHexEncode(entry);
			response.sendRedirect(new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), "/#!Error:"+entryString).toString());
		}
	}
		
	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		return client;
	}

}