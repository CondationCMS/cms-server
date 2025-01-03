/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.condation.cms.api.featureflags;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatureFlagsTest {

    @BeforeEach
    void setup() {
        // Initialisiere die FeatureFlags vor jedem Test
        FeatureFlags.initialize(Map.of(
            "featureX", true,
            "newDashboard", false
        ));
    }

    @Test
    void testFeatureIsEnabled() {
        // Prüfe, dass ein aktives Feature korrekt erkannt wird
        assertThat(FeatureFlags.isEnabled("featureX")).isTrue();
    }

    @Test
    void testFeatureIsDisabled() {
        // Prüfe, dass ein inaktives Feature korrekt erkannt wird
        assertThat(FeatureFlags.isEnabled("newDashboard")).isFalse();
    }

    @Test
    void testDefaultFlagIsDisabled() {
        // Prüfe, dass ein nicht initialisiertes Feature false zurückgibt
        assertThat(FeatureFlags.isEnabled("nonExistentFeature")).isFalse();
    }

    @Test
    void testSetFlag() {
        // Aktiviere ein neues Feature und prüfe den Status
        FeatureFlags.setFlag("betaFeature", true);
        assertThat(FeatureFlags.isEnabled("betaFeature")).isTrue();

        // Deaktiviere es wieder und prüfe den Status
        FeatureFlags.setFlag("betaFeature", false);
        assertThat(FeatureFlags.isEnabled("betaFeature")).isFalse();
    }

    @Test
    void testGetFlags() {
        // Prüfe, dass die initialisierten Flags korrekt zurückgegeben werden
        Map<String, Boolean> flags = FeatureFlags.getFlags();

        assertThat(flags).containsEntry("featureX", true)
                          .containsEntry("newDashboard", false)
                          .doesNotContainKey("nonExistentFeature");

        // Prüfe, dass die Map unveränderlich ist
        assertThatThrownBy(() -> flags.put("shouldFail", true))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}

