var lang = 'fr';

function createTinyMceBar(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddEditor', false, textareas[index].id);
	}
}

function createSimpleTinyMCEResponsive() {
tinyMCE
	.init({
		  editor_selector : "mceSimpleEditorResponsive",
		  mode : "specific_textareas",
		  width : "100%",
		  height : "350",
		  theme : "modern",
		  plugins : ["link ottc_image code charmap fullscreen nuxeoimageupload nuxeolink"],
		  language : lang,
		  block_formats: "Paragraphe=p;Adresse=address;Pre=pre;Titre 1=h1;Titre 2=h2;Titre 3=h3;Titre 4=h4;Titre 5=h5; Titre 6=h6",
		  content_css: "/nuxeo/css/toutatice-popup.min.css?" + new Date().getTime(),
		
		  // Img insertion fixes
		  relative_urls : false,
		  remove_script_host : false,
		
		  toolbar1 : "formatselect | bold italic underline | alignleft aligncenter alignright alignjustify | bullist numlist",
		  toolbar2 : "fullscreen | undo redo | code | link unlink nuxeolink | nuxeoimageupload ottc_image",
		  menubar: false,
		  statusbar: false
	  
	});

    createTinyMceBar("mceSimpleEditorResponsive,disableMCEInit");

}

window.addEventListener("load", createSimpleTinyMCEResponsive);