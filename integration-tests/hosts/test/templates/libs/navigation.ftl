[#macro main]
[#assign nodes = navigationFunction("/", 0)]

<nav class="navbar navbar-expand-lg bg-body-tertiary">
  <div class="container-fluid">
    <a class="navbar-brand" href="/">Navbar scroll</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarScroll" aria-controls="navbarScroll" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarScroll">
      <ul class="navbar-nav me-auto my-2 my-lg-0 navbar-nav-scroll" style="--bs-scroll-height: 100px;">
		[#list nodes as node]
			[#if node.path != "/"]
				<li class="nav-item">
					<a class="nav-link ${node.current?string('active', '')}" aria-current="${node.current?string('page', '')}" href="${node.path}">${node.name} </a>
				</li>
			[/#if]
		[/#list]
      </ul>
    </div>
  </div>
</nav>
[/#macro]
