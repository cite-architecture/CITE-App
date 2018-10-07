

function initiateMarked(markdownString, elementId) {
	setTimeout(function(){
		var parsedMarkdown = marked(markdownString).replace("<p>","").replace("</p>","")
		document.getElementById(elementId).innerHTML = parsedMarkdown;
	},1000);
}

function initiateLeafletWithLatLong(lat, long, elementId) {
	setTimeout(function(){
		console.log("Will draw map in " + elementId + " for " + lat + ", " + long + ".")
		var latitude = parseFloat(lat);
		var longitude = parseFloat(long);
		var mymap = L.map(elementId).setView([latitude, longitude], 13);
		L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
			attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
			maxZoom: 18,
			id: 'mapbox.satellite',
			accessToken: 'pk.eyJ1IjoiY2JsYWNrMDEiLCJhIjoiN0duY2dHRSJ9.O4qgM_Pn3mftGyZk8fIhoQ'
		}).addTo(mymap);
		var marker = L.marker([latitude, longitude]).addTo(mymap);
	},1000);
}

// mapbox token pk.eyJ1IjoiY2JsYWNrMDEiLCJhIjoiN0duY2dHRSJ9.O4qgM_Pn3mftGyZk8fIhoQ