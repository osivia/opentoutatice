$JQry = jQuery.noConflict();

function pvFieldFocus(){
	var $requiredField = $JQry('div.form-group.required');
	
	if($requiredField !== null && typeof $requiredField !== "undefined"){
		$requiredField.first().find("input").first().focus();
	}
	
}

window.addEventListener("load", pvFieldFocus);