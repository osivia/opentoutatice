
function sortableSetUp() {
	// Sortable
	var oldIndex;
	jQuery(".setItem-sortable").sortable({
		connectWith : ".setItem-sortable",
		cursor : "move",
		handle : ".sortable-handle",
		helper : "clone",
		tolerance : "pointer", 
		
		start: function(e, ui) {
		    oldIndex = ui.item.index();
		},
	    update: function(e, ui) {
            var newIndex = ui.item.index();
            if(oldIndex != newIndex){
            	moveItem(oldIndex, newIndex);
            }
	    }
	});
};

jQuery(sortableSetUp);