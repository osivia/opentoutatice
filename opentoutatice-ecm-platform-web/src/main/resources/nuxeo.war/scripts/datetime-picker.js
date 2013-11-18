var $JQry = jQuery.noConflict();

$JQry(function() {
	$JQry.timepicker.regional['fr'] = {
			timeText: 'Horaire',
			hourText: 'Heure',
			minuteText: 'Minute',
			currentText: 'Maintenant',
			closeText: 'Ok'
	};

	$JQry.timepicker.setDefaults($JQry.timepicker.regional['fr']);

	$JQry(".inputTimePickerStart").timepicker({
		timeOnlyTitle: 'Heure de début',
		hourMin: 7,
		hourMax: 20,
		hour: 8,
		minute: 0,
		stepMinute: 15,
		onSelect: function (selectedDateTime) {
			var start = $JQry(this).datetimepicker('getDate');
			var end = $JQry(".inputTimePickerEnd").datetimepicker('getDate');

			if(start > end) {
				$JQry(".inputTimePickerEnd").addClass("acaren_input_error");
			} else {
				$JQry(".inputTimePickerEnd").removeClass("acaren_input_error");
			}
		}				
	});

	$JQry(".inputTimePickerEnd").timepicker({
		timeOnlyTitle: 'Heure de fin',
		hourMin: 7,
		hourMax: 20,
		hour: 8,
		minute: 0,
		stepMinute: 15,
		onSelect: function (selectedDateTime) {
			var start = $JQry(".inputTimePickerStart").datetimepicker('getDate');
			var end = $JQry(this).datetimepicker('getDate');
			
			if(start > end) {
				$JQry(this).addClass("acaren_input_error");
			} else {
				$JQry(this).removeClass("acaren_input_error");
			}
		}				
	});

	$JQry.datepicker.regional['fr'] = {
		closeText: 'Ok',
		prevText: '&lt;précédent',
		nextText: 'suivant&gt;',
		currentText: 'Aujourd hui',
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Jan.','Fév.','Mar.','Avr.','Маi.','Juin',
		'Juil.','Aoû;.','Sept.','Oct.','Nov.','Déc.'],
		dayNames: ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'],
		dayNamesShort: ['lun.','mar.','mer.','jeu.','ven.','sam.','dim.'],
		dayNamesMin: ['lun.','mar.','mer.','jeu.','ven.','sam.','dim.'],
		dateFormat: 'dd/mm/yy',
		firstDay: 0,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''
	};

	$JQry.datepicker.setDefaults($JQry.datepicker.regional['fr']);

//	$JQry(".inputDatePickerStart").datepicker({});

});

