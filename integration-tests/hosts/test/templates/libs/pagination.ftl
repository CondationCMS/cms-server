[#macro pagination page path]
	<nav aria-label="Page navigation">
		<ul class="pagination justify-content-center">
			<li class="page-item [#if page.page lte 1 ]disabled[/#if] ">
				[#assign prevPage = ""]
				[#if page.page > 1]
					[#assign pageNumber = page.page-1]
					[#if pageNumber gt 1]
						[#assign prevPage = "${path}?page=${page.page-1}"]
					[#else]
						[#assign prevPage = "${path}"]
					[/#if]
				[/#if]
				<a class="page-link" href="${prevPage}" aria-label="Previous"
					[#if page.page lte 1] aria-disabled="true"[/#if]
					>
					<span aria-hidden="true">&laquo;</span>
				</a>
			</li>
			[#list [1..page.total] as i]
				[#assign index = i?counter]
				<li class="page-item  [#if index == page.page ] active [/#if]  "
					[#if index == page.page ] aria-current="page" [/#if]
					>
					<button class="page-link">${page.page}</button>
				</li>
			[/#list]
			[#assign nextPage = "#"]
			[#if page.page < page.total]
				[#assign nextPage = "${path}?page=${page.page+1}"]
			[/#if]
			<li class="page-item [#if page.page gte page.total ]disabled[/#if]">
				<a class="page-link" href="${nextPage}" aria-label="Next"
					[#if page.page gte page.total]  aria-disabled="true"[/#if]
					>
					<span aria-hidden="true">&raquo;</span>
				</a>
			</li>
		</ul>
	</nav>
[/#macro]
