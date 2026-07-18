package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.Constants;
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.modules.api.annotation.Extension;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.workflow.WFTransitionException;
import com.condation.cms.api.workflow.Workflow;
import com.condation.cms.core.content.io.ContentFileParser;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteWorkflowEndpointsExtension extends AbstractExtensionPoint implements UIRemoteMethodExtensionPoint {

	private Optional<ContentNode> getContentNode(String uri) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getFileSystem().contentBase();
		var contentFile = getContentFile(uri);

		if (!contentFile.exists()) {
			return Optional.empty();
		}

		var node_uri = PathUtil.toRelativeFile(contentFile, contentBase);

		var node = db.getContent().byUri(node_uri);
		if (node.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
				new ContentNode(
						node.get().uri(),
                        node.get().url(),
						node.get().name(),
						node.get().data(),
						node.get().directory(),
						node.get().children(),
						node.get().lastmodified()
				));
	}

	private ReadOnlyFile getContentFile(String uri) {
		final DB db = getContext().get(DBFeature.class).db();
		var contentBase = db.getFileSystem().contentBase();
		return contentBase.resolve(uri);
	}
    
	@RemoteMethod(name = "workflow.manager.node.status", permissions = {Permissions.CONTENT_EDIT})
	public Object nodeStatus(Map<String, Object> parameters) throws RPCException {

		var uri = (String) parameters.get("uri");
		Map<String, Object> result = new HashMap<>();

		var contentNodeOpt = getContentNode(uri);

		if (contentNodeOpt.isEmpty()) {
			return result;
		}

        var node = contentNodeOpt.get();
        final Workflow workflow = getContext().get(WorkflowFeature.class).workflow();

		var status = workflow.getStatusProvider().status(node);

		result.put("status", status);
        result.put("transitions", workflow.getNextTransitions(node));

		return result;
	}

	@RemoteMethod(name = "workflow.transitions.get", permissions = {Permissions.CONTENT_EDIT})
	public Object getTransitions(Map<String, Object> parameters) throws RPCException {

		var uri = (String) parameters.get("uri");

		Map<String, Object> result = new HashMap<>();

		var contentNodeOpt = getContentNode(uri);
		if (contentNodeOpt.isEmpty()) {
			result.put("transitions", java.util.List.of());
			return result;
		}

		var validTransitions = getContext().get(WorkflowFeature.class).workflow().getNextTransitions(contentNodeOpt.get());

		var transitions = validTransitions.stream().map(
				transition -> Map.of(
						"id", transition.id(),
						"label", transition.label()
				)).toList();

		result.put("transitions", transitions);

		return result;
	}

	@RemoteMethod(name = "workflow.transit", permissions = {Permissions.CONTENT_EDIT})
	public Object transit(Map<String, Object> parameters) throws RPCException {
		var result = new HashMap<String, Object>();
		try {
			var uri = (String) parameters.get("uri");
			var transition = (String) parameters.get("transitionId");

			final DB db = getContext().get(DBFeature.class).db();

			var contentNodeOpt = getContentNode(uri);
			if (contentNodeOpt.isEmpty()) {
				throw new RPCException(404, "content node not found");
			}

			var contentNode = contentNodeOpt.get();

			getContext().get(WorkflowFeature.class).workflow().transit(transition, contentNode);

			var contentFile = getContentFile(uri);

			ContentFileParser parser = new ContentFileParser(contentFile);

			var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);
			YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, contentNode.data(), parser.getContent());

			result.put("success", true);
		} catch (IOException | WFTransitionException ex) {
			log.error("error transit workflow", ex);
			throw new RPCException(0, ex.getMessage());
		}
		return result;
	}

}
