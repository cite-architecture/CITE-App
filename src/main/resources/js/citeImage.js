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


function idForMappedUrn(i) {
	var s = "image_mappedUrn_" + i
	return s
}

function idForMappedROI(i) {
	var s = "image_mappedROI_" + i
	return s
}



function clearJsRoiArray() {
	roiArray = []
}

function addToJsRoiArray(i,r,u,g,clearFirst){
	if(clearFirst){ clearJsRoiArray() }
	tempMap = {index: i, roi: r, mappedUrn: u, group: g}
	roiArray.push(tempMap)
}

function clearSelectedROIs(){
	for (n = 0; n < roiArray.length; n++){
			var roiId = idForMappedROI(n)
			var urnId = idForMappedUrn(n)
			var thisROI = document.getElementById(roiId)
			var thisURN = document.getElementById(urnId)
			thisROI.classList.remove("image_roi_selected")
			thisURN.classList.remove("image_roi_selected")
	}
}

function updateImageJS( collection, imageObject ){
		var collDirectory = collection.replace(new RegExp(':', 'g'), '_');
		var imagePath = "../../../image_archive/" + collDirectory + "/" + imageObject + ".dzi"
		initOpenSeadragon(imagePath)
	}

function initOpenSeadragon(imagePath) {

		if (viewer != null){
				viewer.destroy();
				viewer = null
		}

		viewer = OpenSeadragon({
        id: "image_imageContainer",
        prefixUrl: "js/images/",
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
				var normH = viewer.world.getItemAt(0).getBounds().height
				var normW = viewer.world.getItemAt(0).getBounds().width
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
						var thisClass = "image_mappedROI"
						var thisElement = document.getElementById(thisId)
						thisElement.addEventListener("click", function() {
								clearSelectedROIs()
								var thisIndex = captureN
								var targetId = idForMappedUrn(roiArray[thisIndex].index)
								var targetSpan = document.getElementById(targetId)
								targetSpan.classList.add("image_roi_selected")
								targetSpan.classList.add(idForMappedUrn(captureN))
								targetSpan.classList.add(thisClass)
						}, false);
					}
			},2000);

}
