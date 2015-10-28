//var nxthemesPath = "/nuxeo";
//var nxthemesBasePath = "/nuxeo/site";
//var nxContextPath = "/nuxeo";
//
//var currentConversationId = '0NXMAIN';
//
//function setHideTitle(component) {
//
//	if (component.value.length == 0) {
//		hide = true;
//	} else {
//		hide = false;
//	}
//
//	fieldName = component.getAttribute("name");
//
//	if(fieldName.indexOf("nxw_text") != -1) { // Test présence du champ texte
//		fieldName = fieldName.replace("nxw_text", "hideTitle"); // Déduction du nom du champ caché
//		hideTitle = document.getElementById(fieldName);
//		hideTitle.value = hide;
//	}
//
//}
//
///**
//* function of the icon selector wiget
//* @param fieldname  the hidden field
//* @param listvalue the value of the current element clicked
//* @param multiSelect the mode for multiple selections, true or false
//*/
//function toggleIcon(fieldname, listvalue, multiSelect) {
//	
//	hiddenField = document.getElementById(fieldname);
//	
//	if (multiSelect == true) {
//		jQuery(this).toggleClass('ui-state-active');
//		
//		if(hiddenField.value.indexOf(listvalue) == -1) {
//			hiddenField.value = hiddenField.value + listvalue + " ";
//		}
//		else {
//			hiddenField.value = hiddenField.value.replace(listvalue+" ", "");
//		}
//
//	}
//	else {
//		jQuery('.ui-state-default').removeClass('ui-state-active');
//		jQuery(this).addClass('ui-state-active');
//
//		hiddenField.value = listvalue;
//
//	}
//
//}
//
//jQuery(function() {
//	jQuery('.ui-state-default').hover(function() {
//		jQuery(this).addClass('ui-state-hover');
//
//	}, function() {
//		jQuery(this).removeClass('ui-state-hover');
//	});
//
//	jQuery('.ui-state-default').click(function() {
//		jQuery(this).toggleClass('ui-state-active');
//
//	});
//
//});