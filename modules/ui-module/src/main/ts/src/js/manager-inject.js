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

(async function() {
  // Basis-URLs aus Parent ableiten
  const parentManager = window.parent?.manager || {};
  const baseUrl = parentManager.baseUrl || '/manager';
  const contextPath = parentManager.contextPath || '/';
  
  const importMap = {
    imports: {
      "@cms/js/": `${baseUrl}/js/`,
      "@cms/libs/": `${baseUrl}/js/libs/`,
      "@cms/manager/": `${baseUrl}/js/manager/`,
      "@cms/modules/": `${baseUrl}/js/modules/`
    }
  };
  
  // 2. Import Map setzen
  const script = document.createElement('script');
  script.type = 'importmap';
  script.textContent = JSON.stringify(importMap);
  script.onload = () => {
      resolve();
    };
  document.head.appendChild(script);
  
  // 3. window.manager kopieren
  window.manager = parentManager;
  
  // 4. Plugins laden
  const { initIframe } = await import('@cms/js/manager-inject-init.js');
  initIframe()
})();