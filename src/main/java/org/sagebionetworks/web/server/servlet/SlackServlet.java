package org.sagebionetworks.web.server.servlet;

import static org.sagebionetworks.repo.model.EntityBundle.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

public class SlackServlet extends HttpServlet {

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
		SlackServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	@Override
	protected void doGet(HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
			throws ServletException, IOException {
		//instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L);
		String text = request.getParameter("text");
		String token = request.getParameter("token");
		String teamId = request.getParameter("team_id");
		String teamDomain = request.getParameter("team_domain");
		String channelId =  request.getParameter("channel_id");
		String channelName =  request.getParameter("channel_name");
		String userName = request.getParameter("user_name");
		String command = request.getParameter("command");
		String responseUrl = request.getParameter("response_url");
		PrintWriter out = response.getWriter();
		try {
			response.setContentType("application/json");
			if (command.toLowerCase().equals("/synapse")) {
				String title = null;
				StringBuilder sb = new StringBuilder();
				if (text.toLowerCase().equals("help")) {
					sb.append("Given a Synapse ID (like syn123), post information about that entity.");
				} else {
					SynapseClient client = createNewClient();
//					int partsMask = ENTITY | ENTITY_PATH | ANNOTATIONS | ROOT_WIKI_ID | FILE_HANDLES | PERMISSIONS | BENEFACTOR_ACL | THREAD_COUNT;
					int partsMask = ENTITY | ENTITY_PATH  | THREAD_COUNT;
					EntityBundle bundle = client.getEntityBundle(text, partsMask);
					title = bundle.getEntity().getName();
					sb.append("https://www.synapse.org/#!Synapse:" + text);
					List<EntityHeader> path = bundle.getPath().getPath();
					if (path.size() > 2) {
						sb.append("\n*Project:* " + path.get(1).getName());	
					}
					if (bundle.getThreadCount() > 0) {
						sb.append("\n*Discussion thread count:* " + bundle.getThreadCount());	
					}
				}
					
				JSONObject json = new JSONObject();
				json.put("response_type", "in_channel");
				JSONObject attachments = new JSONObject();
				if (title != null) {
					attachments.put("title", title);	
				}
				attachments.put("text", sb.toString());
				JSONArray jsonArray = new JSONArray();
				jsonArray.put("text");
				attachments.put("mrkdwn_in", jsonArray);
				jsonArray = new JSONArray();
				jsonArray.put(attachments);
				json.put("attachments", jsonArray);
				out.println(json.toString());
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				sendFailure(out, "Sorry, I didn't recognize your command.");
			}
		} catch (Exception e) {
			try {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				sendFailure(out, e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			out.close();
		}
	}
	
	private void sendFailure(PrintWriter out, String error) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("response_type", "ephemeral");
		json.put("text", error);
		out.println(json.toString());
	}

	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setFileEndpoint(StackConfiguration.getFileServiceEndpoint());
		return client;
	}

}
