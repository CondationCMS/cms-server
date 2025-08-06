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
import com.condation.cms.api.extensions.AbstractExtensionPoint;
import com.condation.cms.api.ui.annotations.HookAction;
import com.condation.cms.api.ui.annotations.ShortCut;
import com.condation.cms.api.ui.extensions.UIActionsExtensionPoint;
import com.condation.modules.api.annotation.Extension;
import com.condation.modules.api.annotation.Extensions;

/**
 *
 * @author t.marx
 */
@Extensions({
	@Extension(UIActionsExtensionPoint.class),})
public class SiteMenuExtension extends AbstractExtensionPoint implements UIActionsExtensionPoint {

	@com.condation.cms.api.ui.annotations.MenuEntry(
			id = "toolMenu",
			name = "Tools",
			position = 10
	)
	public void parentDefinition() {

	}

	@com.condation.cms.api.ui.annotations.MenuEntry(
			parent = "toolMenu",
			id = "media-cache-clear",
			name = "Clear media cache",
			position = 1,
			hookAction = @HookAction(value = "ui/manager/tools/media/cache/clear")
	)
	@ShortCut(
			id = "media-cache-clear",
			title = "Clear media cache",
			section = "tools",
			hookAction = @HookAction(value = "ui/manager/tools/media/cache/clear")
	)
	public void create_page() {

	}

}
