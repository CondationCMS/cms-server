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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import com.condation.modules.api.annotation.Extension;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Manager RPC endpoints for all menu CRUD operations.
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteMenuEndpoints extends AbstractRemoteMethodeExtension {

	@RemoteMethod(name = "menu.list", permissions = {Permissions.MENU_MANAGE})
	public Object list(Map<String, Object> parameters) throws RPCException {
		try {
			return service().list();
		} catch (Exception ex) {
			throw rpcException("Could not load menus", ex);
		}
	}

	@RemoteMethod(name = "menu.get", permissions = {Permissions.MENU_MANAGE})
	public Object get(Map<String, Object> parameters) throws RPCException {
		String id = requiredString(parameters, "id");
		try {
			return service().get(id)
					.orElseThrow(() -> new RPCException(1, "Menu not found: " + id));
		} catch (RPCException ex) {
			throw ex;
		} catch (Exception ex) {
			throw rpcException("Could not load menu", ex);
		}
	}

	@RemoteMethod(name = "menu.create", permissions = {Permissions.MENU_MANAGE})
	public Object create(Map<String, Object> parameters) throws RPCException {
		try {
			return service().create(menu(parameters));
		} catch (Exception ex) {
			throw rpcException("Could not create menu", ex);
		}
	}

	@RemoteMethod(name = "menu.update", permissions = {Permissions.MENU_MANAGE})
	public Object update(Map<String, Object> parameters) throws RPCException {
		try {
			return service().update(menu(parameters));
		} catch (Exception ex) {
			throw rpcException("Could not update menu", ex);
		}
	}

	@RemoteMethod(name = "menu.delete", permissions = {Permissions.MENU_MANAGE})
	public Object delete(Map<String, Object> parameters) throws RPCException {
		String id = requiredString(parameters, "id");
		try {
			return Map.of("deleted", service().delete(id));
		} catch (Exception ex) {
			throw rpcException("Could not delete menu", ex);
		}
	}

	private MenuService service() {
		return getContext()
				.get(InjectorFeature.class)
				.injector()
				.getInstance(MenuService.class);
	}

	private Menu menu(Map<String, Object> parameters) throws RPCException {
		Object value = parameters.get("menu");
		if (value == null) {
			throw new RPCException(1, "Menu is required");
		}
		try {
			return value instanceof Menu menu
					? menu
					: UIGsonProvider.INSTANCE.fromJson(UIGsonProvider.INSTANCE.toJson(value), Menu.class);
		} catch (RuntimeException ex) {
			throw new RPCException(1, "Invalid menu data");
		}
	}

	private String requiredString(Map<String, Object> parameters, String name) throws RPCException {
		Object value = parameters.get(name);
		if (!(value instanceof String stringValue) || stringValue.isBlank()) {
			throw new RPCException(1, name + " is required");
		}
		return stringValue;
	}

	private RPCException rpcException(String action, Exception cause) {
		log.error(action, cause);
		String detail = cause.getMessage();
		return new RPCException(1, detail == null || detail.isBlank() ? action : action + ": " + detail);
	}
}
