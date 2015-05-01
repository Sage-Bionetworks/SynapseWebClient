package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class ProgrammaticClientCode extends Composite implements SynapseWidgetPresenter {

	private static int numSeq = 0;
	
	// Likely want to move all of this off this widget, but for now...
	
	static PortalGinInjector ginInjector;
	
	@Inject
	public ProgrammaticClientCode(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}
	
	public static FlowPanel createLoadWidget(String entityId, Long versionNumber, SynapseJSNIUtils synapseJSNIUtils, SageImageBundle sageImageBundle) {
		Anchor rLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoR45())));
		GWT.debugger();
//		rClientEntityLoadWidgetViewImpl rLoadWidget = ginInjector.createRClientEntityLoadWidgetViewImpl();
//		rLoadWidget.configure(entityId, versionNumber);
		//DisplayUtils.addClickPopover(rLoadWidget.asWidget(), "Synapse R Client", Placement.BOTTOM);

		Anchor pythonLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoPython45())));
		DisplayUtils.addClickPopover(pythonLink, "Synapse Python Client", getPythonClientEntityLoad(entityId, versionNumber).asString(), Placement.BOTTOM);

		Anchor shellLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoCommandLine45())));
		DisplayUtils.addClickPopover(shellLink, "Synapse Command Line Client", getCommandLineClientEntityLoad(entityId, versionNumber).asString(), Placement.BOTTOM);

		Anchor javaLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoJava45())));
		DisplayUtils.addClickPopover(javaLink, "Synapse Java Client", getJavaClientEntityLoad(entityId, versionNumber).asString(), Placement.BOTTOM);
		
		FlowPanel lc = new FlowPanel();
		lc.add(pythonLink);
		lc.add(shellLink);
		lc.add(javaLink);
		return lc;
	}
	
	public static SafeHtml getRClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String idString = "id='" + safeId + "'";
		String versionString = versionNumber == null ? "" : ", version='"+versionNumber+"'";
		String load = "<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">" 
			+ ("library(synapseClient)<br/>" 
				+ "synapseLogin('usename','password')<br/><br/>"
				+"# " + DisplayConstants.LABEL_CLIENT_GET_ENTITY + " <br/>"
				+ safeId + " &lt;- synGet(" + idString +   versionString+")" 
				+"<br/><br/># " + DisplayConstants.LABEL_CLIENT_LOAD_ENTITY + " <br/>"
				+ safeId + " &lt;- synGet(" + idString + versionString+", load=T)").replaceAll(" ", "&nbsp;")
			+ "</div>"
			+ getAPIandExampleCodeLinks(ClientProperties.CLIENT_R_API_URL, ClientProperties.CLIENT_R_EXAMPLE_CODE_URL)
			+ "<br/>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_R_CLIENT_INSTALL,
					getRClientInstallHTML().asString()); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}

	public static SafeHtml getRClientInstallHTML() {
		return SafeHtmlUtils.fromSafeConstant("<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">"
		+ ClientProperties.R_CLIENT_DOWNLOAD_CODE.replaceAll(" ", "&nbsp;")
		+ "</div>");
	}	

	public static SafeHtml getPythonClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String idString = "'" + safeId + "'";
		String versionString = versionNumber == null ? "" : ", version="+versionNumber;
		String load = "<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">" 
			+ ("import synapseclient<br/><br/>"
				+ "syn = synapseclient.Synapse()<br/>"
				+ "syn.login('synapse_username','password')<br/><br/>"
				+ "# " + DisplayConstants.LABEL_CLIENT_GET_ENTITY + " <br/>"
				+ safeId + " = syn.get(" + idString +versionString+")" 
				+ "<br/><br/># " + DisplayConstants.GET_PATH_CLIENT_ENTITY + " <br/>"
				+ "filepath = "+safeId+".path").replaceAll(" ", "&nbsp;")
			+ "</div>"
			+ getAPIandExampleCodeLinks(ClientProperties.CLIENT_PYTHON_API_URL, ClientProperties.CLIENT_PYTHON_EXAMPLE_CODE_URL)
			+ "<br/>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_PYTHON_CLIENT_INSTALL, getPythonClientInstallHTML().asString()); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}

	public static SafeHtml getPythonClientInstallHTML() {
		return SafeHtmlUtils.fromSafeConstant("<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">"
		+ ClientProperties.PYTHON_CLIENT_DOWNLOAD_CODE.replaceAll(" ", "&nbsp;")
		+ "</div>");
	}	

	public static SafeHtml getCommandLineClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String load = "<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">" 
			+ ("# Login<br/>"	
				+ "synapse -u synapse_username -p pw<br/><br/>" 
				+ "# " + DisplayConstants.DOWNLOAD_FILE_LOCAL + "<br/>"
				+ "synapse get " + safeId + "<br/>").replaceAll(" ", "&nbsp;")
			+ "</div>"
			+ getAPIandExampleCodeLinks(ClientProperties.CLIENT_CL_API_URL, ClientProperties.CLIENT_CL_EXAMPLE_CODE_URL)
			+ "<br/>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_CL_CLIENT_INSTALL, getPythonClientInstallHTML().asString()); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}
	
	public static SafeHtml getJavaClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String entityString = versionNumber == null ?  
				"Entity "+ safeId +" = synapseClient.getEntityById(\""+ safeId +"\");" :
				"Entity "+ safeId +" = synapseClient.getEntityByIdForVersion(\""+ safeId +"\", "+versionNumber+"L);";
		
		String load = "<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">" 
		+ ("import org.sagebionetworks.client.Synapse;<br/><br/>"
			+ "Synapse synapseClient = new Synapse();<br/>"
			+ "synapseClient.login('synapse_username', 'password');<br/><br/>"
			+ entityString).replaceAll(" ", "&nbsp;")
		+ "</div>"
		+ getAPIandExampleCodeLinks(ClientProperties.CLIENT_JAVA_API_URL, ClientProperties.CLIENT_JAVA_EXAMPLE_CODE_URL)
		+ "<br/>";
		
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.INSTALL_JAVA_MAVEN,	getJavaClientInstallHTML().asString());
		return SafeHtmlUtils.fromSafeConstant(load);
	}

	public static SafeHtml getJavaClientInstallHTML() {
		return SafeHtmlUtils.fromSafeConstant("<div class=\"" + ClientProperties.STYLE_CODE_CONTENT + "\">"	
		+ ("# Using Maven, add to pom.xml:<br/>"
			+ "&lt;distributionManagement&gt;<br/>"
			+ "    &lt;repository&gt;<br/>"
			+ "        &lt;id&gt;sagebionetworks&lt;/id&gt;<br/>"
			+ "        &lt;name&gt;sagebionetworks-releases&lt;/name&gt;<br/>"
			+ "        &lt;url&gt;http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local&lt;/url&gt;<br/>"
			+ "    &lt;/repository&gt;<br/>"
			+ "&lt;/distributionManagement&gt;<br/>"
			+ "<br/>"
			+ "&lt;dependency&gt;<br/>"
			+ "    &lt;groupId&gt;org.sagebionetworks&lt;/groupId&gt;<br/>"
			+ "    &lt;artifactId&gt;synapseJavaClient&lt;/artifactId&gt;<br/>"
			+ "    &lt;version&gt;See Repository for newest Version&lt;/version&gt;<br/>"
			+ "&lt;/dependency&gt;<br/>").replaceAll(" ", "&nbsp;")
		+ "</div>");
	}	

	/*
	 * Private Methods
	 */
	private static String wrapCollapse(String link, String content) {		
		String collapseId = "collapse-" + numSeq++;
		String wrap = "<button type=\"button\" class=\"btn btn-info\" data-toggle=\"collapse\" data-target=\"#" + collapseId + "\">" 
		+ link
		+ "</button>"
		+ "<div id=\"" + collapseId + "\" class=\"accordion-body collapse\">"
		+ content
		+ "</div>";

		return wrap;			
	}

	private static String getAPIandExampleCodeLinks(String apiUrl, String exampleCodeUrl) {
		return "<a href=\""+ apiUrl + "\" class=\"link\" target=\"_blank\">"+ DisplayConstants.API_DOCUMENTATION 
				+"</a> &amp; <a href=\"" + exampleCodeUrl + "\" class=\"link\" target=\"_blank\">"+ DisplayConstants.EXAMPLE_CODE +"</a>";
	}
	
}
