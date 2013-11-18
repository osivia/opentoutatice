var ie = /MSIE/.test(navigator.userAgent);
var moz = !ie && navigator.product == "Gecko";

if (moz) {
     HTMLElement.prototype.__defineGetter__("children", function() {
           var arr = new Array(), i = 0, l = this.childNodes.length;
           for ( i = 0; i < l; i++ ) {
               if ( this.childNodes[ i ].nodeType == 1 ) {
                    arr.push( this.childNodes[ i ] );
               }
           }
      return arr;
      });
} 

function findParentNode(className, obj) {
    var testObj = obj.parentNode;
    while (!Element.hasClassName(testObj, className)) {
        testObj = testObj.parentNode;
    }
    return testObj;
}

function findChildNode(className, obj) {
    var testObj = obj.children;
    var len = testObj.length;
    for ( i = 0; i < len; i++ ) {
       if (Element.hasClassName(testObj[i], className)) {
          return testObj[i]
       }
    }
    return false;
}

function toggleVoc(toggleButton) {
	  var box = findParentNode('vocBox', toggleButton);
	  var title = findChildNode('vocTitle', box);
	  var body = findChildNode('vocBody', box);

		if (Element.hasClassName(title, 'vocTitle')) {
			title.className = 'vocTitleUnFolded';
		} else {
			title.className = 'vocTitle';
		}

		Effect.toggle(body, 'blind', {duration:0.2});

	return false;
}