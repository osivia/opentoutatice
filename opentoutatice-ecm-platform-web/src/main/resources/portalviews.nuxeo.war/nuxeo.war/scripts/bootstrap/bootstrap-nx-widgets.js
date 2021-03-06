function customizeNxWidgets() {
	/* Nx vocabularies widgets */
	jQuery('.select2-container').removeAttr("style");
	jQuery('.select2-container > ul.select2-choices > li > input').removeAttr("style");
	jQuery('#select2-drop-mask').removeAttr("style");
	jQuery('.select2-search-field > input').removeAttr("style");
	jQuery('.select2-drop').removeAttr("style");
	jQuery('li.select2-search-field > input.select2-input').removeAttr("style");
}

window.addEventListener("load", customizeNxWidgets);
