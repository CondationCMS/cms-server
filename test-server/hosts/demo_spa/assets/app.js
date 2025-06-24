const routes = {
	"/": () => "<h1>Welcome!</h1><p>This is the starting page.</p>",
	"/about": () => "<h1>About us</h1><p>We are the team behind CondationCMS.</p>",
	"/contact": () => "<h1>Contact</h1><p>Send us a message!</p>",
};

function router() {
	const path = window.location.pathname;
	const content = routes[path] ? routes[path]() : "<h1>404</h1><p>Seite nicht gefunden</p>";
	document.getElementById("app").innerHTML = content;
}

function navigateTo(url) {
	history.pushState(null, null, url);
	router();
}

// Link-Klicks abfangen
document.addEventListener("click", e => {
	if (e.target.matches("[data-link]")) {
		e.preventDefault();
		navigateTo(e.target.href);
	}
});

// Popstate (zurück/vor)
window.addEventListener("popstate", router);

// Initialer Aufruf
document.addEventListener("DOMContentLoaded", router);
