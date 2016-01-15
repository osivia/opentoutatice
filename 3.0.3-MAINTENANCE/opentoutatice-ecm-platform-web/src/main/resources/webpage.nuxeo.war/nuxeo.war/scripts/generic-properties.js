var $collapsed = null;
var $hideTitle = null;
var $panel = null;

function manageCollapse(){
	if(!$hideTitle.prop("checked")){
		if($collapsed.prop("disabled")){
			$collapsed.removeAttr("disabled");
		}
	} else {
		$collapsed.attr("disabled", "disabled");
	}
}

function setUp(){
	// FIXME: why get(0) is not direct jQuery variable??
	$collapsed = jQuery(jQuery("input[type='checkbox'][id*='collapsed']").get(0));
	$hideTitle = jQuery(jQuery("input[type='checkbox'][id*='hideTitle']").get(0));
	$panel = jQuery(jQuery("input[type='checkbox'][id*='panel']").get(0));
	
	$hideTitle.on("click", manageCollapse);
	$panel.on("click", manageCollapse);
	
	var collapseDisable = $hideTitle.prop("checked") || (!$hideTitle.prop("checked") && !$panel.prop("checked"));
	if(collapseDisable){
		$collapsed.attr("disabled", "disabled"); 
		if($collapsed.prop("checked")){
			$collapsed.removeAttr("checked");
		}
	}
}

window.addEventListener("load", setUp);