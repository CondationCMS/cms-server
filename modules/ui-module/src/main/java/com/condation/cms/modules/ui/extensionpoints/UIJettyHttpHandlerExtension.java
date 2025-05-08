package com.condation.cms.modules.ui.extensionpoints;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.extensions.HttpRoutesExtensionPoint;
import com.condation.cms.api.extensions.Mapping;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.modules.ui.commands.GetContentCommand;
import com.condation.modules.api.annotation.Extension;
import com.condation.cms.modules.ui.commands.GetContentNodeCommand;
import com.condation.cms.modules.ui.commands.SetContentCommand;
import com.condation.cms.modules.ui.commands.SetMetaCommand;
import com.condation.cms.modules.ui.http.CommandHandler;
import com.condation.cms.modules.ui.http.HookHandler;
import com.condation.cms.modules.ui.http.JsModuleHandler;
import com.condation.cms.modules.ui.http.ResourceHandler;
import com.condation.cms.modules.ui.services.CommandService;
import com.condation.cms.modules.ui.utils.ActionFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;

/**
 *
 * @author t.marx
 */
@Extension(HttpRoutesExtensionPoint.class)
@Slf4j
public class UIJettyHttpHandlerExtension extends HttpRoutesExtensionPoint {

	public static FileSystem fileSystem;

	public static synchronized FileSystem getFileSystem() {
		if (fileSystem == null) {
			fileSystem = createFileSystem();
		}
		return fileSystem;
	}

	public static FileSystem createFileSystem() {
		try {
			URL resource = UIJettyHttpHandlerExtension.class.getResource("/manager");

			final Map<String, String> env = new HashMap<>();
			final String[] array = resource.toURI().toString().split("!");
			try {
				return FileSystems.getFileSystem(URI.create(array[0]));
			} catch (FileSystemNotFoundException fsnfe) {
				log.error("", fsnfe);
			}

			return FileSystems.newFileSystem(URI.create(array[0]), env);

		} catch (URISyntaxException | IOException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Mapping getMapping() {
		
		Mapping mapping = new Mapping();
		
		var siteProperties = getContext().get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		if (!siteProperties.uiManagerEnabled()) {
			return mapping;
		}
		

		var hookSystem = getRequestContext().get(HookSystemFeature.class).hookSystem();
		var moduleManager = getContext().get(ModuleManagerFeature.class).moduleManager();
		var actionFactory = new ActionFactory(hookSystem, moduleManager);


		var commandService = new CommandService();
		commandService.register("test", (cmd) -> "Hallo Leute!");
		commandService.register(GetContentNodeCommand.NAME,
				GetContentNodeCommand.getHandler(getContext(), getRequestContext()));
		commandService.register(GetContentCommand.NAME, GetContentCommand.getHandler(context, requestContext));
		commandService.register(SetMetaCommand.NAME, SetMetaCommand.getHandler(context, requestContext));
		commandService.register(SetContentCommand.NAME, SetContentCommand.getHandler(context, requestContext));

		try {
			/*
			mapping.add(PathSpec.from("/file-system/list"), new FileSystemListHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/create"), new FileSystemCreateHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/delete"), new FileSystemDeleteHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/read"), new FileSystemReadHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/write"), new FileSystemWriteHandler(UILifecycleExtension.fileSystemService));
			 */

			mapping.add(PathSpec.from("/manager/command"), new CommandHandler(commandService));

			mapping.add(PathSpec.from("/manager/hooks"), new HookHandler(hookSystem));

			mapping.add(PathSpec.from("/manager/menu/action/test-modal"), new JsModuleHandler("actions/test-modal.js"));
			mapping.add(PathSpec.from("/manager/menu/action/test-sidebar"), new JsModuleHandler("actions/test-sidebar.js"));
			mapping.add(PathSpec.from("/manager/menu/action/test-command"), new JsModuleHandler("actions/test-command.js"));
			
			mapping.add(PathSpec.from("/manager/*"), new ResourceHandler(actionFactory, getFileSystem(), "/manager", getContext()));


		} catch (Exception ex) {
			log.error(null, ex);
		}
		return mapping;
	}

}
