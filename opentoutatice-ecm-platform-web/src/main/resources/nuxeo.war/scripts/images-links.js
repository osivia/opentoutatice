// FIXME: use Loïc's method (it certainly doesn't work here becaus of bad jquery js import)
var $JQry = jQuery.noConflict();

// EditableWindow or TinyMCE
var origin_ = "none";

var $picturePath;
var $targetPath;
var $targetTitle;

$JQry(window).load(function() {
	var $inputPath = $JQry("input[type=hidden][id*='picturePath']")[0];
	$picturePath = $JQry($inputPath);

	if ($picturePath.val()) {
		previewImg();
	}

	var $inputLink = $JQry("input[type=hidden][id*='targetPath']")[0];
	$targetPath = $JQry($inputLink);

	var $inputTitle = $JQry("[id*='targetTitle']")[0];
	$inputTitle.addEventListener("blur", setManualPath);

	$targetTitle = $JQry($inputTitle);
	
	// It is not an external URL
	if(!$targetTitle.val().contains("/") && $targetTitle.val() != ""){
		$targetTitle.attr("readonly","readonly");
	}
});

function previewImg() {
	$JQry("#imagePreview").attr("src", $picturePath.val());
}

function removeTarget() {
	$targetPath.val("");
	$targetTitle.val("");

	if($targetTitle.prop("readonly")){
		$targetTitle.removeAttr("readonly");
	}
}

function setManualPath() {
	if($targetTitle.val() != "" && $targetTitle.val().contains("/")){
		$targetPath.val($targetTitle.val());
	}
}