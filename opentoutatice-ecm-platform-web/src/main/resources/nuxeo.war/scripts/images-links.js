var $JQry = jQuery.noConflict();

// EditableWindow or TinyMCE
var origin_ = "none";

var $picturePath;
var $targetPath = new Array();
var $targetTitle = new Array();
var index_ = 1;

$JQry(window).load(function() {

	var $inputPath = $JQry("input[type=hidden][id*='picturePath']")[0];
	$picturePath = $JQry($inputPath);

	if ($picturePath.val()) {
		previewImg();
	}

	var $inputLinks = $JQry("input[type=hidden][id*='targetPath']");
	var $inputTitles = $JQry("[id*='targetTitle']");

	for(var incr = 0; incr < $inputLinks.length; incr++){

		$targetPath[incr] = $JQry($inputLinks)[incr];	
		//$inputTitles[incr].addEventListener("blur", setManualPath);

		$targetTitle[incr] = $JQry($inputTitles[incr]);
	
		// It is not an external URL
		if(!$targetTitle[incr].val().contains("/") && $targetTitle[incr].val() != ""){
			$targetTitle[incr].attr("readonly","readonly");
		}
	}
	
});

function previewImg() {
	$JQry("#imagePreview").attr("src", $picturePath.val());
}

function removeTarget(index) {
	var indx = index - 1;
	$JQry($targetPath[indx]).val("");
	$JQry($targetTitle[indx]).val("");

	if($JQry($targetTitle[indx]).prop("readonly")){
		$JQry($targetTitle[indx]).removeAttr("readonly");
	}
}

function setManualPath(index) {
	var indx = index - 1;
	if($JQry($targetTitle[indx]).val() != "" && $JQry($targetTitle[indx]).val().contains("/")){
		$JQry($targetPath[indx]).val($JQry($targetTitle[indx]).val());
	}
}