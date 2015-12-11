function showErrors() {
		var $ul = jQuery("#labels");
		var $errors = jQuery("span.errorMessage");
		
		if($errors !== null && typeof $errors !== "undefined"){
			$errors.each(function() {
					jQuery(this).attr("style", "display: inline-block; margin-top: 5px;");
					var $firstParent = jQuery(this).closest("div[class*='form-group']");
					$firstParent.attr("style", "border: 1px solid #a94442; border-radius: 5px; padding-top: 5px; margin-left: 0px; margin-right: -1px;");
					var $label = $firstParent.children("label");
					$ul.append("<li>" + $label.text() + "</li>");
			});
	}
}

window.addEventListener("load", showErrors);