package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.utils.CallbackP;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class QueryWrapperPlotNavProps extends ReactComponentProps {
	@FunctionalInterface
	@JsFunction
	public interface OnQueryRequestBundleCallback {
		void run(String newQueryRequestBundle);
	}

	Query initQuery;
	String sql;
	@JsNullable
	OnQueryRequestBundleCallback onQueryBundleRequestChange;
	@JsNullable
	boolean shouldDeepLink;
	@JsNullable
	String downloadCartPageUrl;
	@JsNullable
	boolean hideSqlEditorControl;
	@JsNullable
	SynapseTableProps tableConfiguration;

	@JsOverlay
	public static QueryWrapperPlotNavProps create(Query initQuery, CallbackP<String> onQueryBundleRequestChange,
			boolean hideSqlEditorControl) {
		QueryWrapperPlotNavProps props = new QueryWrapperPlotNavProps();
		props.sql = initQuery.getSql();
		props.initQuery = initQuery;
		props.hideSqlEditorControl = hideSqlEditorControl;
		props.onQueryBundleRequestChange = (newQueryBundleRequest) -> {
			onQueryBundleRequestChange.invoke(newQueryBundleRequest);
		};
		props.tableConfiguration = SynapseTableProps.create();
		props.shouldDeepLink = true;
		props.downloadCartPageUrl = "#!DownloadCart:0";
		return props;
	}
}
