/**
* function of the icon selector wiget
* @param fieldname  the hidden field
* @param listvalue the value of the current element clicked
* @param multiSelect the mode for multiple selections, true or false
*/
function toggleIcon(element, fieldname, listvalue, iconsGroup, multiSelect) {
	
	// get the input hidden field, for persistance
	hiddenField = document.getElementById(fieldname);
	
	// multiple values can be setted
	if (multiSelect == true) {
		jQuery(element).toggleClass('ui-state-active'); // set this current element active or inactive
		
		// add or remove the value in hidden field
		if(hiddenField.value.indexOf(listvalue) == -1) {
			hiddenField.value = hiddenField.value + listvalue + " ";
		}
		else {
			hiddenField.value = hiddenField.value.replace(listvalue+" ", "");
		}

	}
	// one value can be setted
	else {
		if (typeof (iconsGroup) != "undefined" && iconsGroup != "") { // get the group of buttons
			
			// remove all active
			jQuery('#group-' + iconsGroup + ' > .ui-state-default').removeClass(
					'ui-state-active');
			// set this current element active
			jQuery(element).addClass('ui-state-active');
			// persist the value clicked
			hiddenField.value = listvalue;
		}
	}

}