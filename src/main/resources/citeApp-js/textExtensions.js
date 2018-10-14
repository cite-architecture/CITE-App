

function initiateMarked(markdownString, elementId) {
	setTimeout(function(){
		var parsedMarkdown = marked(markdownString).replace("<p>","").replace("</p>","")
		document.getElementById(elementId).innerHTML = parsedMarkdown;
	},1000);
}

function initiateLeafletWithLatLong(lat, long, elementId) {
	setTimeout(function(){
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

function initiateLeafletWithGeoJson(geojson, elementId) {
	setTimeout(function(){
		var myFeature = JSON.parse(geojson);
		var mymap = L.map(elementId).setView([0, 0], 20);
		L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
			attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
			maxZoom: 18,
			id: 'mapbox.satellite',
			accessToken: 'pk.eyJ1IjoiY2JsYWNrMDEiLCJhIjoiN0duY2dHRSJ9.O4qgM_Pn3mftGyZk8fIhoQ'
		}).addTo(mymap);
		var gjFeature = L.geoJSON(myFeature)
		gjFeature.addTo(mymap);
		mymap.fitBounds(gjFeature.getBounds());
		mymap.zoomOut(3);

// create a fullscreen button and add it to the map
L.control.fullscreen({
  position: 'topleft', // change the position of the button can be topleft, topright, bottomright or bottomleft, defaut topleft
  title: 'Show me the fullscreen !', // change the title of the button, default Full Screen
  titleCancel: 'Exit fullscreen mode', // change the title of the button when fullscreen is on, default Exit Full Screen
  content: null, // change the content of the button, can be HTML, default null
  forceSeparateButton: true, // force seperate button to detach from zoom buttons, default false
  forcePseudoFullscreen: true, // force use of pseudo full screen even if full screen API is available, default false
  fullscreenElement: false // Dom element to render in full screen, false by default, fallback to map._container
}).addTo(mymap);

// events are fired when entering or exiting fullscreen.
mymap.on('enterFullscreen', function(){
  console.log('entered fullscreen');
});

mymap.on('exitFullscreen', function(){
  console.log('exited fullscreen');
});
		
	},1000);


}

// mapbox token pk.eyJ1IjoiY2JsYWNrMDEiLCJhIjoiN0duY2dHRSJ9.O4qgM_Pn3mftGyZk8fIhoQ