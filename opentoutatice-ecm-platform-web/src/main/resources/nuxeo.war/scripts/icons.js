/**
 * function of the icon selector wiget
 * 
 * @param fieldname
 *            the hidden field
 * @param listvalue
 *            the value of the current element clicked
 * @param multiSelect
 *            the mode for multiple selections, true or false
 */
function toggleIcon(element, fieldname, listvalue, iconsGroup) {

	hiddenField = document.getElementById(fieldname);

	if (typeof (iconsGroup) != "undefined" && iconsGroup != "") {
		jQuery('#group-' + iconsGroup + ' > .ui-state-default').removeClass(
				'ui-state-active');
		jQuery(element).addClass('ui-state-active');
	} else {
		jQuery('.ui-state-default').removeClass('ui-state-active');
		jQuery(element).addClass('ui-state-active');
	}

	hiddenField.value = listvalue;

}

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

//}