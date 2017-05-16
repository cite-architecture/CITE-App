// Variables to hold the current image URN and an array of ROIs


var viewer = null
var roiArray = [
{roi: "0.064,0.2267,0.466,0.0293", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.1", group: "1"},
{roi: "0.159,0.2538,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.2", group: "2"},
{roi: "0.161,0.2748,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.3", group: "3"},
{roi: "0.166,0.2928,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.4", group: "4"}
]



	          // className: 'image_imageROI image_roiGroup1'



updateImageJS = function( collection, imageObject ){
    console.log("Collection: " + collection)
		console.log("Image: " + imageObject)
		var collDirectory = collection.replace(new RegExp(':', 'g'), '_');
		var imagePath = "image_archive/" + collDirectory + "/" + imageObject + ".dzi"
		console.log("Will look for images in: " + imagePath)
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
				homeFillsViewer: true,
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
						elt.id = "image_imageRoi" + ol
		 			  elt.className = "image_imageROI" + " image_roiGroup" + roiArray[ol].group
						elt.dataset.urn = roiArray[ol].mappedUrn

						viewer.addOverlay(elt,osdRect)
					}
				}

					// Go through and attach events to each overlay
					for (n = 0; n < roiArray.length; n++){
						var thisId = "image_imageRoi" + n
						var thisElement = document.getElementById(thisId)
						thisElement.addEventListener("click", function() {
							var TheTextBox = document.getElementById("image_mappedUrn");
					    TheTextBox.value = thisElement.dataset.urn;
							if (TheTextBox.onchange) TheTextBox.onchange();
						}, false);
					}
				//})

			},500);




}
