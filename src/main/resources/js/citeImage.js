// Variables to hold the current image URN and an array of ROIs


var viewer = null
var roiArray =[]

/*
classes:

	image_mappedUrn
	image_mappedROI
	image_roiGroup_1
	image_roi_selected

*/

/*
function imageInNewWindow(c) {
		console.log("Got here… opening " )
		var w = c.width;
		console.log(w);
		var h = c.height;
		console.log(h);
		var u = c.toDataURL();
		window.open(u, "Image", "width=200, height=200");
}
*/

function idForMappedUrn(i) {
	var s = "image_mappedUrn_" + (i)
	return s
}

function idForMappedROI(i) {
	var s = "image_mappedROI_" + (i)
	return s
}

function roiToUrnId(id) {
	var s = id.replace("image_mappedROI_","image_mappedUrn_")
	return s
}


function clearJsRoiArray(r) {
	roiArray = []
}

function addToJsRoiArray(i,r,u,g){
	tempMap = {index: i, roi: r, mappedUrn: u, group: g}
	roiArray.push(tempMap)
}

function clearSelectedROIs(){
	for (n = 0; n < roiArray.length; n++){
			var roiId = idForMappedROI(n+1)
			var urnId = idForMappedUrn(n+1)
			var thisROI = document.getElementById(roiId)
			var thisURN = document.getElementById(urnId)
			thisROI.classList.remove("image_roi_selected")
			thisURN.classList.remove("image_roi_selected")
	}
}

function updateImageJS( collection, imageObject, path ){
		//var collDirectory = collection.replace(new RegExp(':', 'g'), '_');
		//var imagePath = "../../../image_archive/" + collDirectory + "/" + imageObject + ".dzi"
		console.log("In js: " + collection + ", " + imageObject + ", " + path);
		initOpenSeadragon(path)
	}

	/**
 * Normalizes the index of the ROI to a number within the given amount of
 * colors. E.g. If there were only two colors, an index of 4 would return group
 * number 0 => (index % colorLength)
 * @param  {int} i index number
 * @return {int} normalized group number
 */
function getGroup(i){
	var colorArray = ["#f23568", "#6d38ff", "#38ffd7", "#fff238", "#661641", "#275fb3", "#24a669", "#a67b24", "#ff38a2", "#194973", "#35f268", "#7f441c", "#801c79", "#2a8ebf", "#216616", "#d97330", "#da32e6", "#196d73", "#bdff38", "#bf3e2a", "#3d1973", "#30cdd9", "#858c1f", "#661616"	];
	var limit = colorArray.length
   var rv = i % limit;
	return rv;
}

function initOpenSeadragon(imagePath) {

		if (viewer != null){
				viewer.destroy();
				viewer = null
		}

		viewer = OpenSeadragon({
        id: "image_imageContainer",
        prefixUrl: "js/images/",
			  crossOriginPolicy: "Anonymous",
        tileSources: imagePath,
				springStiffness: 20,
				animationTime: 8,
				homeFillsViewer: false,
				gestureSettingsMouse: {
					clickToZoom: false
				}
		});

		viewer.guides({
		  allowRotation: false,        // Make it possible to rotate the guidelines (by double clicking them)
		  horizontalGuideButton: null, // Element for horizontal guideline button
		  verticalGuideButton: null,   // Element for vertical guideline button
		  prefixUrl: "js/images/",             // Images folder
		  removeOnClose: false,        // Remove guidelines when viewer closes
		  useSessionStorage: false,    // Save guidelines in sessionStorage
		  navImages: {
		    guideHorizontal: {
		      REST: 'guidehorizontal_rest.png',
		      GROUP: 'guidehorizontal_grouphover.png',
		      HOVER: 'guidehorizontal_hover.png',
		      DOWN: 'guidehorizontal_pressed.png'
		    },
		    guideVertical: {
		      REST: 'guidevertical_rest.png',
		      GROUP: 'guidevertical_grouphover.png',
		      HOVER: 'guidevertical_hover.png',
		      DOWN: 'guidevertical_pressed.png'
		    }
		  }
		});


    // Add overlays
			setTimeout(function(){
				var normH = viewer.world.getItemAt(0).getBounds().height;
				var normW = viewer.world.getItemAt(0).getBounds().width;
				if (roiArray.length > 0){
					for (ol = 0; ol < roiArray.length; ol++){
						var roi = roiArray[ol].roi
						var rl = +roi.split(",")[0]
						var rt = +roi.split(",")[1]
						var rw = +roi.split(",")[2]
						var rh = +roi.split(",")[3]
						var tl = rl * normW
					  var tt = rt * normH
					  var tw = rw * normW
					  var th = rh * normH
						var osdRect = new OpenSeadragon.Rect(tl,tt,tw,th)
						var elt = document.createElement("a")
						elt.id = idForMappedROI(roiArray[ol].index)
	 			    elt.className = "image_mappedROI" + " image_roiGroup_" + roiArray[ol].group + " " + idForMappedUrn(roiArray[ol].index)
						elt.dataset.urn = roiArray[ol].mappedUrn

						viewer.addOverlay(elt,osdRect)
					}
				}

					// Go through and attach events to each overlay
					for (n = 0; n < roiArray.length; n++){
						var captureN = n // because N keeps moving
						var thisId = idForMappedROI(roiArray[n].index)
						var thisElement = document.getElementById(thisId)
						thisElement.addEventListener("click", function(e) {
								clearSelectedROIs()
								var roiId = e.target.id
								var targetId = roiToUrnId(roiId)
								var targetSpan = document.getElementById(targetId)
								targetSpan.classList.add("image_roi_selected")
								e.target.classList.add("image_roi_selected")
						}, false);
					}
			},3000);

}
