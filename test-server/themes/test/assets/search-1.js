document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("#search-button").addEventListener("click", () => {
        performSearch();
    })
    document.querySelector("#search-input").addEventListener("keypress", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            performSearch();
          }
    })
})

const getSearchUrl = () => {
    if ("/" !== CONTEXT_PATH) {
        return CONTEXT_PATH + "/module/search-module/search"
    } else {
        return "/module/search-module/search"
    }
}

const performSearch =  async () => {
    let query = document.querySelector("#search-input").value
    console.log("search for ", query)
    if (query.length > 3) {
        let result = await fetch( getSearchUrl() + "?query=" + query);
        let jsonResult = await result.json()
        
        let searchResults = ""
        jsonResult.items.forEach(item => {
            searchResults += `
                <div class="clearfix search-result"><!-- item -->
		            <h4><a href="${item.uri}">${item.title}</a></h4>
		            <small class="text-success">${item.uri}</small>
		            <p>${item.content}</p>
	            </div>
            `
        })

        document.querySelector("#result_count").innerHTML = `About ${jsonResult.total} results`
        document.querySelector("#search_results").innerHTML = searchResults
        
    }
}