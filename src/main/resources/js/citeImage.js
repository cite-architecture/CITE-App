// Variables to hold the current image URN and an array of ROIs

var viewer = null
var overlayArray = []


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

		overlayArray = [
					{
            id: 'example-overlay1',
            x: 0.33,
            y: 0.75,
            width: 0.2,
            height: 0.25,
	          className: 'image_imageROI image_roiGroup1'
					},
					{
						id: 'example-overlay2',
						x: 0.66,
						y: 0.25,
						width: 0.2,
						height: 0.25,
						className: 'image_imageROI image_roiGroup2'
				},
					{
						id: 'example-overlay3',
						x: 0.50,
						y: 0.50,
						width: 0.2,
						height: 0.25,
						className: 'image_imageROI image_roiGroup3'
				},
				{
					id: 'example-overlay4',
					x: 0.10,
					y: 0.5,
					width: 0.2,
					height: 0.25,
					className: 'image_imageROI image_roiGroup4'
				}
		]

		viewer = OpenSeadragon({
        id: "image_imageContainer",
        prefixUrl: "js/images/",
        tileSources: imagePath,
				overlays: overlayArray,
				springStiffness: 20,
				animationTime: 8,
				homeFillsViewer: true

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

		viewer.addHandler('open', function(event) {
			// Go through and attach events to each overlay
			for (n = 0; n < overlayArray.length; n++){
				var thisId = overlayArray[n].id
				var thisElement = document.getElementById(thisId)
				thisElement.addEventListener("click", function() {
					var TheTextBox = document.getElementById("image_mappedUrn");
			    TheTextBox.value = this.id;
					if (TheTextBox.onchange) TheTextBox.onchange();
				}, false);
			}
		})


}
