var lang = 'fr';

function createTinyMceBar(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddEditor', false, textareas[index].id);
	}
}

function createTinyMCEResponsive() {
tinyMCE
	.init({
		  editor_selector : "mceEditorResponsive",
		  mode : "specific_textareas",
		  width : "100%",
		  height : "350",
		  theme : "modern",
		  plugins : ["link image code searchreplace paste visualchars charmap table fullscreen preview nuxeoimageupload nuxeolink"],
		  language : lang,
		
		  // Img insertion fixes
		  relative_urls : false,
		  remove_script_host : false,
		
		  toolbar1 : "bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | formatselect | fontselect | fontsizeselect",
		  toolbar2 : "paste pastetext searchreplace | bullist numlist | outdent indent | undo redo | link unlink anchor image code",
		  toolbar3 : "table | hr removeformat visualchars | subscript superscript | charmap preview | fullscreen nuxeoimageupload nuxeolink",
		  menubar: false
	  
	});

    createTinyMceBar("mceEditorResponsive,disableMCEInit");

}

window.addEventListener("load", createTinyMCEResponsive);