package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

public class ProgrammaticClientCode extends Composite implements SynapseWidgetPresenter {

	private static int numSeq = 0;
	
	public static LayoutContainer createLoadWidget(String entityId, Long versionNumber, SynapseJSNIUtils synapseJSNIUtils, SageImageBundle sageImageBundle) {
		Anchor rLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoR45())));
		DisplayUtils.addClickPopover(synapseJSNIUtils, rLink, "Synapse R Client", getRClientEntityLoad(entityId, versionNumber).asString(), TOOLTIP_POSITION.BOTTOM);

		Anchor pythonLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoPython45())));
		DisplayUtils.addClickPopover(synapseJSNIUtils, pythonLink, "Synapse Python Client", getPythonClientEntityLoad(entityId, versionNumber).asString(), TOOLTIP_POSITION.BOTTOM);

		Anchor javaLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoJava45())));
		DisplayUtils.addClickPopover(synapseJSNIUtils, javaLink, "Synapse Java Client", getJavaClientEntityLoad(entityId, versionNumber).asString(), TOOLTIP_POSITION.BOTTOM);

		Anchor shellLink = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.logoCommandLine45())));
		DisplayUtils.addClickPopover(synapseJSNIUtils, shellLink, "Synapse Command Line Client", getCommandLineClientEntityLoad(entityId, versionNumber).asString(), TOOLTIP_POSITION.BOTTOM);

		LayoutContainer lc = new LayoutContainer();
		lc.add(rLink);
		lc.add(pythonLink);
		lc.add(javaLink);
		lc.add(shellLink);
		return lc;
	}
	
	public static SafeHtml getRClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String idString = "id='" + safeId + "'";
		String versionString = versionNumber == null ? "" : ", version='"+versionNumber+"'";
		String load = "<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">" 
			+ ("library(synapseClient)<br/>" 
				+ "synapseLogin('usename','password')<br/><br/>"
				+"# " + DisplayConstants.LABEL_CLIENT_GET_ENTITY + " <br/>"
				+ safeId + " &lt;- synGet(" + idString +   versionString+")" 
				+"<br/><br/># " + DisplayConstants.LABEL_CLIENT_LOAD_ENTITY + " <br/>"
				+ safeId + " &lt;- synGet(" + idString + versionString+", load=T)").replaceAll(" ", "&nbsp;")
			+ "</div>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_R_CLIENT_INSTALL,
					"<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">"
					+ DisplayUtils.R_CLIENT_DOWNLOAD_CODE.replaceAll(" ", "&nbsp;")
					+ "</div>"); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}	

	public static SafeHtml getPythonClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String idString = "'" + safeId + "'";
		String versionString = versionNumber == null ? "" : ", version="+versionNumber;
		String load = "<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">" 
			+ ("import synapseclient<br/><br/>"
				+ "syn = synapseclient.Synapse()<br/>"
				+ "syn.login('synapse_username','password')<br/><br/>"
				+ "# " + DisplayConstants.LABEL_CLIENT_GET_ENTITY + " <br/>"
				+ safeId + " = syn.get(" + idString +versionString+")" 
				+ "<br/><br/># " + DisplayConstants.GET_PATH_CLIENT_ENTITY + " <br/>"
				+ "filepath = "+safeId+".path").replaceAll(" ", "&nbsp;")
			+ "</div>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_PYTHON_CLIENT_INSTALL,
			"<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">"
			+ DisplayUtils.PYTHON_CLIENT_DOWNLOAD_CODE.replaceAll(" ", "&nbsp;")
			+ "</div>"); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}	

	public static SafeHtml getJavaClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String entityString = versionNumber == null ?  
				"Entity "+ safeId +" = synapseClient.getEntityById(\""+ safeId +"\");" :
				"Entity "+ safeId +" = synapseClient.getEntityByIdForVersion(\""+ safeId +"\", "+versionNumber+"L);";
		
		String load = "<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">" 
		+ ("import org.sagebionetworks.client.Synapse;<br/><br/>"
			+ "Synapse synapseClient = new Synapse();<br/>"
			+ "synapseClient.login('synapse_username', 'password');<br/><br/>"
			+ entityString).replaceAll(" ", "&nbsp;")
		+ "</div>";
		
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.INSTALL_JAVA_MAVEN,
				"<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">"	
				+ ("# Using Maven, add to pom.xml:"
					+ "&lt;distributionManagement&gt;<br/>"
					+ "    &lt;repository&gt;<br/>"
					+ "        &lt;id&gt;sagebionetworks&lt;/id&gt;<br/>"
					+ "        &lt;name&gt;sagebionetworks-releases&lt;/name&gt;<br/>"
					+ "        &lt;url&gt;http://sagebionetworks.artifactoryonline.com/sagebionetworks/ext-releases-local&lt;/url&gt;<br/>"
					+ "    &lt;/repository&gt;<br/>"
					+ "&lt;/distributionManagement&gt;<br/>"
					+ "<br/>"
					+ "&lt;dependency&gt;<br/>"
					+ "    &lt;groupId&gt;org.sagebionetworks&lt;/groupId&gt;<br/>"
					+ "    &lt;artifactId&gt;synapseJavaClient&lt;/artifactId&gt;<br/>"
					+ "    &lt;version&gt;See Repository for newest Version&lt;/version&gt;<br/>"
					+ "&lt;/dependency&gt;<br/>").replaceAll(" ", "&nbsp;")
				+ "</div>"
		);
		return SafeHtmlUtils.fromSafeConstant(load);
	}	

	public static SafeHtml getCommandLineClientEntityLoad(String id, Long versionNumber) {
		String safeId = SafeHtmlUtils.fromString(id).asString();
		String load = "<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">" 
			+ ("# Login<br/>"	
				+ "synapse -u synapse_username -p pw<br/><br/>" 
				+ "# " + DisplayConstants.DOWNLOAD_FILE_LOCAL + "<br/>"
				+ "synapse get " + safeId + "<br/>").replaceAll(" ", "&nbsp;")
			+ "</div>";

		// add install code
		load += "<br/>"
		+ wrapCollapse(DisplayConstants.LABEL_CL_CLIENT_INSTALL,
			"<div class=\"" + DisplayUtils.STYLE_CODE_CONTENT + "\">"
			+ DisplayUtils.PYTHON_CLIENT_DOWNLOAD_CODE.replaceAll(" ", "&nbsp;")
			+ "</div>"); 
		return SafeHtmlUtils.fromSafeConstant(load);
	}
	
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

	
}
