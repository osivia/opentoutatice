var lang = 'fr';

	  tinyMCE.init({
	    editor_selector : "mceEditor",
		mode : "specific_textareas",
		theme : "advanced",
		plugins : "fullscreen,nuxeoimageupload,nuxeolink",
		language : lang,
		theme_advanced_resizing : true,
	
		// Img insertion fixes
		relative_urls : false,
		remove_script_host : false,
		skin : "o2k7",
		skin_variant : "silver",
		theme_advanced_disable : "styleselect",
		theme_advanced_buttons3 : "hr,removeformat,visualaid,|,sub,sup,|,charmap,|",
		theme_advanced_buttons3_add : "fullscreen,nuxeoimageupload,nuxeolink",
		// extended_valid_elements :
		// "iframe[src|frameborder|style|scrolling|class|width|height|name|align]",
		});


		tinyMCE.init({
			editor_selector : "mceEditorMinimal",
			mode : "specific_textareas",
			theme : "advanced",
			plugins : "autoresize",
			language : lang,
			theme_advanced_resizing : true,
	
			// Img insertion fixes
			relative_urls : false,
			remove_script_host : false,
			skin : "o2k7",
			skin_variant : "silver",
			theme_advanced_buttons1 : "bold,italic,underline",
			theme_advanced_buttons2 : "",
			theme_advanced_buttons3 : "",
			
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