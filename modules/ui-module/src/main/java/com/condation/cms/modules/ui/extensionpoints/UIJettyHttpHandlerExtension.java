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
import com.condation.cms.api.Constants;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.extensions.HttpRoutesExtensionPoint;
import com.condation.cms.api.extensions.Mapping;
import com.condation.cms.api.feature.features.CacheManagerFeature;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.modules.api.annotation.Extension;
import com.condation.cms.modules.ui.http.HookHandler;
import com.condation.cms.modules.ui.http.JSActionHandler;
import com.condation.cms.modules.ui.http.RemoteCallHandler;
import com.condation.cms.modules.ui.http.ResourceHandler;
import com.condation.cms.modules.ui.http.UploadHandler;
import com.condation.cms.modules.ui.http.CompositeHttpHandler;
import com.condation.cms.modules.ui.http.PublicResourceHandler;
import com.condation.cms.modules.ui.http.auth.AjaxLoginHandler;
import com.condation.cms.modules.ui.http.auth.CSRFHandler;
import com.condation.cms.modules.ui.http.auth.LoginResourceHandler;
import com.condation.cms.modules.ui.http.auth.LogoutHandler;
import com.condation.cms.modules.ui.http.auth.UIAuthHandler;
import com.condation.cms.modules.ui.http.auth.UIAuthRedirectHandler;
import com.condation.cms.modules.ui.services.RemoteMethodService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;

/**
 *
 * @author t.marx
 */
@Extension(HttpRoutesExtensionPoint.class)
@Slf4j
public class UIJettyHttpHandlerExtension extends HttpRoutesExtensionPoint {

	private static FileSystem managerFileSystem = null;

	public static FileSystem createFileSystem(String base) {
		// Schneller Pfad: bereits initialisiert
		if (managerFileSystem != null && managerFileSystem.isOpen()) {
			return managerFileSystem;
		}

		synchronized (UIJettyHttpHandlerExtension.class) {
			// Doppelte Prüfung, falls zwei Threads gleichzeitig reinlaufen
			if (managerFileSystem != null && managerFileSystem.isOpen()) {
				return managerFileSystem;
			}

			try {
				URL resource = UIJettyHttpHandlerExtension.class.getResource(base);
				if (resource == null) {
					throw new IllegalStateException("Resource '" + base + "' not found");
				}

				String[] array = resource.toURI().toString().split("!");
				URI uri = URI.create(array[0]);

				try {
					managerFileSystem = FileSystems.getFileSystem(uri);
				} catch (FileSystemNotFoundException e) {
					final Map<String, String> env = new HashMap<>();
					managerFileSystem = FileSystems.newFileSystem(uri, env);
				}

			} catch (URISyntaxException | IOException ex) {
				log.error("Fehler beim Erstellen des FileSystems", ex);
				throw new RuntimeException(ex);
			}

			return managerFileSystem;
		}
	}

	@Override
	public Mapping getMapping() {

		Mapping mapping = new Mapping();

		var siteProperties = getContext().get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		if (!siteProperties.ui().managerEnabled()) {
			return mapping;
		}

		var hookSystem = getRequestContext().get(HookSystemFeature.class).hookSystem();
		var moduleManager = getContext().get(ModuleManagerFeature.class).moduleManager();

		ICache<String, AtomicInteger> failedLoginsCounter = getContext().get(CacheManagerFeature.class).cacheManager().get("loginFails",
				new CacheManager.CacheConfig(10_000l, Duration.ofMinutes(1)),
				key -> new AtomicInteger(0)
		);

		ICache<String, AjaxLoginHandler.Login> logins = getContext().get(CacheManagerFeature.class).cacheManager().get("logins",
				new CacheManager.CacheConfig(10_000l, Duration.ofMinutes(5))
		);

		RemoteMethodService remoteCallService = new RemoteMethodService();
		remoteCallService.init(moduleManager);

		try {

			mapping.add(PathSpec.from("/manager/login"), new LoginResourceHandler(getContext(), getRequestContext()));
			//mapping.add(PathSpec.from("/manager/login.action"), new LoginHandler(getContext(), getRequestContext(), failedLoginsCounter));
			mapping.add(PathSpec.from("/manager/login.action"), new AjaxLoginHandler(getContext(), getRequestContext(), failedLoginsCounter, logins));
			mapping.add(PathSpec.from("/manager/logout"), new LogoutHandler(getRequestContext()));

			mapping.add(PathSpec.from("/manager/upload"),
					new CompositeHttpHandler(List.of(
							new UIAuthHandler(getContext(), getRequestContext()),
							new CSRFHandler(getContext()),
							new UploadHandler(
									"/manager/upload",
									getContext().get(DBFeature.class).db().getFileSystem().resolve(Constants.Folders.ASSETS))
					)));
			mapping.add(PathSpec.from("/manager/upload2"),
					new CompositeHttpHandler(List.of(
							new UIAuthHandler(getContext(), getRequestContext()),
							new CSRFHandler(getContext()),
							new UploadHandler(
									"/manager/upload2",
									getContext().get(DBFeature.class).db().getFileSystem().resolve(Constants.Folders.ASSETS),
									true
							)
					)));
			mapping.add(PathSpec.from("/manager/rpc"),
					new CompositeHttpHandler(List.of(
							new UIAuthHandler(getContext(), getRequestContext()),
							new CSRFHandler(getContext()),
							new RemoteCallHandler(remoteCallService, getContext())
					)));

			mapping.add(PathSpec.from("/manager/hooks"),
					new CompositeHttpHandler(List.of(
							new UIAuthHandler(getContext(), getRequestContext()),
							new CSRFHandler(getContext()),
							new HookHandler(hookSystem)
					)));

			mapping.add(PathSpec.from("/manager/actions/*"),
					new CompositeHttpHandler(List.of(
							new UIAuthHandler(getContext(), getRequestContext()),
							new JSActionHandler(createFileSystem("/manager/actions"), "/manager/actions", getContext())
					)));

			mapping.add(PathSpec.from("/manager/bootstrap/*"),
					new PublicResourceHandler(
							getContext(),
							createFileSystem("/manager"),
							"/manager",
							List.of(
									"bootstrap/bootstrap.bundle.min.js",
									"bootstrap/bootstrap.bundle.min.js.map",
									"bootstrap/bootstrap.min.css",
									"bootstrap/bootstrap-superhero.min.css",
									"bootstrap/bootstrap-icons.min.css",
									"bootstrap/fonts/bootstrap-icons.woff",
									"bootstrap/fonts/bootstrap-icons.woff2"
							)
					)
			);
			mapping.add(PathSpec.from("/manager/public/*"),
					new PublicResourceHandler(
							getContext(),
							createFileSystem("/manager"),
							"/manager",
							List.of(
									"public/manager-login.js"
							)
					)
			);

			mapping.add(PathSpec.from("/manager/*"),
					new CompositeHttpHandler(
							List.of(
									new UIAuthRedirectHandler(getContext(), getRequestContext()),
									new ResourceHandler(createFileSystem("/manager"), "/manager", getContext(), getRequestContext())
							)
					)
			);

		} catch (Exception ex) {
			log.error(null, ex);
		}
		return mapping;
	}

}
