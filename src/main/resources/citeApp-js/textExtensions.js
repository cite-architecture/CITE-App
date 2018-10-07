

function initiateMarked(markdownString, elementId) {
	setTimeout(function(){
		var parsedMarkdown = marked(markdownString).replace("<p>","").replace("</p>","")
		document.getElementById(elementId).innerHTML = parsedMarkdown;
	},1000);
}