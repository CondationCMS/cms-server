package com.condation.cms.e2e;

/*-
 * #%L
 * integration-tests
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

import com.condation.cms.test.e2e.CMSServerExtension;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@UsePlaywright
@ExtendWith(CMSServerExtension.class)
class DemoFrontendTest {

    private static final String BASE_URL = "http://localhost:2020";

    @Test
    void mainMenuAndContentLinksNavigateToPublicPages(Page page) {
        page.navigate(BASE_URL);

        var menu = page.locator("body > ul").nth(1);
        assertThat(menu.locator("a").allTextContents()).containsExactly(
                "Startseite", "das ist eine neue seite", "Content example", "Locations"
        );

        menu.locator("a[href='/content']").click();
        assertThat(page.url()).endsWith("/content");
        assertThat(page.locator("#content h1").innerText()).isEqualTo("Content examples");

        page.locator("body > ul").nth(1).locator("a[href='/locations']").click();
        assertThat(page.locator("h1").innerText()).isEqualTo("Radius search example");

        page.navigate(BASE_URL);
        page.locator("#content a[href='/about']").click();
        assertThat(page.url()).endsWith("/about");
        assertThat(page.locator("#content > .container h1").innerText()).isEqualTo("About");
    }

    @Test
    void languageSwitcherOpensGermanAlternate(Page page) {
        page.navigate(BASE_URL);

        assertThat(page.locator(".language-buttons a.lang").count()).isEqualTo(2);
        assertThat(page.locator(".language-buttons a.lang.active").count()).isEqualTo(1);
        page.locator(".language-buttons a.lang:not(.active)").click();

        assertThat(page.url()).endsWith("/de/");
        assertThat(page.locator("title").innerText()).isEqualTo("Startseite");
        assertThat(page.locator("#content h1").innerText()).isEqualTo("Demo de Projekt");
    }

    @Test
    void homeRendersPublishedSectionsAndThemeAssets(Page page) {
        page.navigate(BASE_URL);

        assertThat(page.locator("title").innerText()).isEqualTo("Startpage");
        assertThat(page.locator("#content h1").innerText()).isEqualTo("Demo Project");
        assertThat(page.locator(".sections > .section").count()).isEqualTo(1);
        assertThat(page.locator(".sections > .section h1").innerText()).isEqualTo("This is a section: bla bla");
        assertThat(page.locator(".sections_v > .section_v").count()).isEqualTo(1);
        assertThat(page.locator(".sections_v > .section_v h1").innerText()).isEqualTo("This is a section: bla");
        assertThat(page.locator("link[rel='stylesheet'][href='/theme/assets/style.css']").count()).isEqualTo(1);
        assertThat(page.locator("#content img[src='/media/images/test.jpg?format=small']").evaluate(
                "img => img.complete && img.naturalWidth > 0"
        )).isEqualTo(true);
    }

    @Test
    void contentSectionsRenderOnlyPublishedEntries(Page page) {
        page.navigate(BASE_URL + "/content/sections");

        assertThat(page.locator("#content h1").innerText()).isEqualTo("Section examples");
        assertThat(page.locator(".sections > .section").count()).isEqualTo(1);
        assertThat(page.locator(".sections > .section h1").innerText()).isEqualTo("This is a section: bla");
    }

    @Test
    void collectionsLinkToPublishedBlogAndAuthorDetails(Page page) {
        page.navigate(BASE_URL + "/collections");

        assertThat(page.locator("#content h2").allTextContents())
                .containsExactly("Blog collection", "Author collection");
        assertThat(page.locator("#content a").allTextContents())
                .contains("Blog item 1", "Blog item 2", "Thorsten")
                .doesNotContain("Blog item 3");

        page.locator("#content a:has-text('Blog item 1')").click();
        assertThat(page.url()).contains("/blog/2026/04/item_1");
        assertThat(page.locator("#content h1").innerText()).isEqualTo("Blog item 1");
        assertThat(page.locator("#content").innerText()).contains("This is the first item");

        page.navigate(BASE_URL + "/collections");
        page.locator("#content a:has-text('Thorsten')").click();
        assertThat(page.url()).endsWith("/collections/authors/thorsten");
        assertThat(page.locator("#content h2").innerText()).isEqualTo("Thorsten");
        assertThat(page.locator("#content").innerText()).contains("that my author");
    }

    @Test
    void brandTaxonomyRendersOverviewAndBrandContent(Page page) {
        page.navigate(BASE_URL + "/marken");

        assertThat(page.locator("#content > h1").innerText()).isEqualTo("Marken");
        assertThat(page.locator("#content .container").first().innerText()).contains("Out brands");

        page.navigate(BASE_URL + "/marken/brand1");
        assertThat(page.locator("#content > h1").innerText()).isEqualTo("Marken");
        assertThat(page.locator("#content .container").first().innerText()).contains("Brand 1", "Best brand in the world");
    }

    @Test
    void blogPaginationAndArticleLinksWork(Page page) {
        page.navigate(BASE_URL + "/usecases/blog");

        assertThat(page.locator("h1").innerText()).isEqualTo("Example Blog");
        assertThat(page.locator("article").count()).isEqualTo(3);
        assertThat(page.locator("nav[aria-label='Blog pagination']").innerText()).contains("Page 1 of 2");

        var firstPageArticles = page.locator("article h2").allTextContents();
        page.locator("nav a[rel='next']").click();
        assertThat(page.url()).contains("page=2");
        assertThat(page.locator("article").count()).isEqualTo(3);
        assertThat(page.locator("article h2").allTextContents()).doesNotContainAnyElementsOf(firstPageArticles);
        assertThat(page.locator("nav a[rel='prev']").count()).isEqualTo(1);

        page.locator("article a").first().click();
        assertThat(page.locator("article h1").innerText()).contains("blog post");
        assertThat(page.locator("article").innerText()).contains("This is our");
    }

    @Test
    void radiusSearchLinksToLocationDetailsAndBack(Page page) {
        page.navigate(BASE_URL + "/locations");

        assertThat(page.locator("h1").innerText()).isEqualTo("Radius search example");
        assertThat(page.locator(".container li a").allTextContents()).containsExactlyInAnyOrder(
                "Deutsches Bergbau-Museum Bochum",
                "Eisenbahnmuseum Bochum-Dahlhausen",
                "UNESCO-Welterbe Zollverein"
        );
        assertThat(page.locator(".container li").first().innerText()).contains("km");

        page.locator(".container li a:has-text('Deutsches Bergbau-Museum Bochum')").click();
        assertThat(page.locator("h1").innerText()).isEqualTo("Deutsches Bergbau-Museum Bochum");
        assertThat(page.locator(".container").innerText()).contains("Coordinates:", "51.4882", "7.216");

        page.locator("a[href='/locations']").click();
        assertThat(page.locator("h1").innerText()).isEqualTo("Radius search example");
    }
}
