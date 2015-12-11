var lang = 'fr';

function createTinyMce(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddEditor', false, textareas[index].id);
	}
}

function createEditorMinimalInLine(){

tinyMCE.init({
	editor_selector : "mceEditorMinimalInLine",
	mode : "specific_textareas",
	theme : "modern",
	plugins : "",
	language : lang,
	theme_advanced_resizing : true,
	height : "25",

	inline_styles : false,
	formats : {
		underline : {
			inline : 'u',
			exact : false
		}
	},
	
	content_css: "/nuxeo/css/toutatice-popup.min.css?" + new Date().getTime(),

	// Disable <p> and <br> tag generation
	force_p_newlines : false,
	forced_root_block : false,
	invalid_elements : "br",
	valid_elements : "strong/b,em,u,span[style<text-decoration: underline;]",

	// Img insertion fixes
	relative_urls : false,
	remove_script_host : false,

	toolbar1 : "bold,italic,underline",
	menubar: false,
	statusbar: false

});

createTinyMce("mceEditorMinimalInLine,disableMCEInit");

}

function createEditorMinimal(){

tinyMCE
.init({
	editor_selector : "mceEditorMinimal",
	mode : "specific_textareas",
	theme : "modern",
	plugins : "",
	language : lang,
	theme_advanced_resizing : true,
	height : "150",

	inline_styles : false,
	formats : {
		underline : {
			inline : 'u',
			exact : false
		}
	},
	
	content_css: "/nuxeo/css/toutatice-popup.min.css?" + new Date().getTime(),

	valid_elements : "p,br,strong/b,em,u,span[style<text-decoration: underline;]",

	// Img insertion fixes
	relative_urls : false,
	remove_script_host : false,

	toolbar1 : "bold,italic,underline",
	menubar: false,
	statusbar: false

});

createTinyMce("mceEditorMinimal,disableMCEInit");

}

window.addEventListener("load", createEditorMinimalInLine);
window.addEventListener("load", createEditorMinimal);


