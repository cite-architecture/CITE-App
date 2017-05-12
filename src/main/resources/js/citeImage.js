// Variables to hold the current image URN and an array of ROIs

var viewer = null


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
}
