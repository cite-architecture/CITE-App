// Variables to hold the current image URN and an array of ROIs


var viewer = null
var roiArray = [
{roi: "0.064,0.2267,0.466,0.0293", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.1", group: "1"},
{roi: "0.159,0.2538,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.2", group: "2"},
{roi: "0.161,0.2748,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.3", group: "3"},
{roi: "0.166,0.2928,0.343,0.0218", mappedUrn: "urn.cts:greekLit:tlg0012.tlg001.msA:1.4", group: "4"}
]



	          // className: 'image_imageROI image_roiGroup1'

clearJsRoiArray = function() {
	console.log("Clearing roiArray")
	roiArray = []
}

addToJsRoiArray = function(r,u,g,clearFirst){
	if(clearFirst){ clearJsRoiArray() }
	console.log("updateJsRoiArray: " + r + ", " + u + ", " + g)
	tempMap = {roi: r, mappedUrn: u, group: g}
	console.log("tempMap: "  + tempMap)
	roiArray.push(tempMap)
	console.log("roiArray.size() = " + roiArray.size)
}

updateJsRoiArray = function( roiArrayFromScala ){
	console.log("updateJsRoiArray")
	console.log("from Scala…")
	console.log(roiArrayFromScala)
	roiArray = roiArrayFromScala
	console.log("now in JS…")
	console.log(roiArray)
}

updateImageJS = function( collection, imageObject ){
        console.log("Collection: " + collection)
    	console.log("Image: " + imageObject)
		var collDirectory = collection.replace(new RegExp(':', 'g'), '_');
		var imagePath = "../../../image_archive/" + collDirectory + "/" + imageObject + ".dzi"
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
						elt.id = "image_imageRoi" + ol
		 			    elt.className = "image_imageROI" + " image_roiGroup" + roiArray[ol].group
						elt.dataset.urn = roiArray[ol].mappedUrn

						viewer.addOverlay(elt,osdRect)
					}
				}

					// Go through and attach events to each overlay
					for (n = 0; n < roiArray.length; n++){
						var thisId = "image_imageRoi" + n
						var thisClass = "image_imageRoi" + n
						var thisElement = document.getElementById(thisId)
						thisElement.addEventListener("click", function() {
							//var TheTextBox = document.getElementById("image_mappedUrn");
						    //TheTextBox.value = thisElement.dataset.urn;
							///	if (TheTextBox.onchange) TheTextBox.onchange();
							var targetId = "image_mapped_" + thisElement.dataset.urn
							var targetSpan = document.getElementById(targetId)
							targetSpan.classList.add("image_roi_selected")
							targetSpan.classList.add(thisClass)
							console.log("Clicked on: " + targetId )
						}, false);
					}
			},2000);


function distinctColor(n){
		var colorDic = [
			"#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059", "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87", "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80", "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100", "#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F", "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09", "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66", "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C", "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81", "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00", "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700", "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329", "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C", "#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800", "#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51", "#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58", "#7A7BFF", "#D68E01", "#353339", "#78AFA1", "#FEB2C6", "#75797C", "#837393", "#943A4D", "#B5F4FF", "#D2DCD5", "#9556BD", "#6A714A", "#001325", "#02525F", "#0AA3F7", "#E98176", "#DBD5DD", "#5EBCD1", "#3D4F44", "#7E6405", "#02684E", "#962B75", "#8D8546", "#9695C5", "#E773CE", "#D86A78", "#3E89BE", "#CA834E", "#518A87", "#5B113C", "#55813B", "#E704C4", "#00005F", "#A97399", "#4B8160", "#59738A", "#FF5DA7", "#F7C9BF", "#643127", "#513A01", "#6B94AA", "#51A058", "#A45B02", "#1D1702", "#E20027", "#E7AB63", "#4C6001", "#9C6966", "#64547B", "#97979E", "#006A66", "#391406", "#F4D749", "#0045D2", "#006C31", "#DDB6D0", "#7C6571", "#9FB2A4", "#00D891", "#15A08A", "#BC65E9", "#FFFFFE", "#C6DC99", "#203B3C", "#671190", "#6B3A64", "#F5E1FF", "#FFA0F2", "#CCAA35", "#374527", "#8BB400", "#797868", "#C6005A", "#3B000A", "#C86240", "#29607C", "#402334", "#7D5A44", "#CCB87C", "#B88183", "#AA5199", "#B5D6C3", "#A38469", "#9F94F0", "#A74571", "#B894A6", "#71BB8C", "#00B433", "#789EC9", "#6D80BA", "#953F00", "#5EFF03", "#E4FFFC", "#1BE177", "#BCB1E5", "#76912F", "#003109", "#0060CD", "#D20096", "#895563", "#29201D", "#5B3213", "#A76F42", "#89412E", "#1A3A2A", "#494B5A", "#A88C85", "#F4ABAA", "#A3F3AB", "#00C6C8", "#EA8B66", "#958A9F", "#BDC9D2", "#9FA064", "#BE4700", "#658188", "#83A485", "#453C23", "#47675D", "#3A3F00", "#061203", "#DFFB71", "#868E7E", "#98D058", "#6C8F7D", "#D7BFC2", "#3C3E6E", "#D83D66", "#2F5D9B", "#6C5E46", "#D25B88", "#5B656C", "#00B57F", "#545C46", "#866097", "#365D25", "#252F99", "#00CCFF", "#674E60", "#FC009C", "#92896B"
		];
	}

}
