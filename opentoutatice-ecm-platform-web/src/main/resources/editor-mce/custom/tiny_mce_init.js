//Language configuration
//TODO: check si nécessaire 
//if(lang!= 'en' && lang !='fr'){
//	lang = 'en';
//}

tinyMCE.init({mode : "specific_textareas",
        language : "fr",
        theme : "advanced",
        editor_selector : "mceEditor",
		editor_deselector : "disableMCEInit",
        plugins : "table,print,paste,searchreplace,contextmenu,xhtmlxtras,fullscreen,nuxeoimageupload,nuxeolink",

        // Theme options
        theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,|,fullscreen,nuxeoimageupload,nuxeolink",
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,code",
		theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,|,print,|,abbr,acronym,del,ins",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_statusbar_location : "bottom",
        gecko_spellcheck : true,
        extended_valid_elements : "img[usemap|class|src|border=0|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],map[id|name],area[shape|alt|coords|href|target]",
        // Skin options
        skin : "o2k7",
        skin_variant : "silver",
        relative_urls : false,
	    remove_script_host : false,
	    
	    content_css : nxContextPath + "/css/note_content_block.css",
		body_class : "note_content_block"

});

tinyMCE.init({mode : "specific_textareas",
    language : "fr",
    theme : "advanced",
    editor_selector : "mceExtrait",
	editor_deselector : "disableMCEInit",
    plugins : "lists,style,advlink,directionality,nonbreaking,xhtmlxtras,nuxeolink",

    // Theme options
    theme_advanced_buttons1 : "bold,italic,underline,strikethrough,sub,sup|,justifyleft,justifycenter,justifyright,justifyfull,|,bullist,numlist,|,outdent,indent,blockquote,|,forecolor,backcolor,|,nuxeolink,link,unlink",

    gecko_spellcheck : true,

    // Skin options
    skin : "o2k7",
    skin_variant : "silver",
    relative_urls : false,
    remove_script_host : false,
});

tinyMCE.init({
    mode : "textareas",
    language : "fr",
    theme : "simple",
    editor_selector : "mceSimple"
	});

    function toggleTinyMCE(id) {
      if (!tinyMCE.getInstanceById(id))
        addTinyMCE(id);
       else
        removeTinyMCE(id);
      }

    function removeTinyMCE(id) {
     tinyMCE.execCommand('mceRemoveControl', false, id);
    }

    function addTinyMCE(id) {
     tinyMCE.execCommand('mceAddControl', false, id);
    }