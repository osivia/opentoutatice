jQuery.noConflict();

window.onload = function(e) {
	/* Nx vocabularies widgets */
	jQuery('.select2-container').removeAttr("style");
	jQuery('.select2-container > ul.select2-choices > li > input').removeAttr("style");
	/* Nx Calendar */
	//jQuery(".rich-calendar-button").parent().append("<i class='glyphicons halflings uni-calendar'></i>");
}