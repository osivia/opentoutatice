/**
 * plugin.js
 *
 * Copyright, Moxiecode Systems AB
 * Released under LGPL License.
 *
 * License: http://www.tinymce.com/license
 * Contributing: http://www.tinymce.com/contributing
 */

/*global tinymce:true */

tinymce.PluginManager.add('ottc_image', function(editor) {
	function getImageSize(url, callback) {
		var img = document.createElement('img');

		function done(width, height) {
			if (img.parentNode) {
				img.parentNode.removeChild(img);
			}

			callback({width: width, height: height});
		}

		img.onload = function() {
			done(img.clientWidth, img.clientHeight);
		};

		img.onerror = function() {
			done();
		};

		var style = img.style;
		style.visibility = 'hidden';
		style.position = 'fixed';
		style.bottom = style.left = 0;
		style.width = style.height = 'auto';

		document.body.appendChild(img);
		img.src = url;
	}

	function createImageList(callback) {
		return function() {
			var imageList = editor.settings.image_list;

			if (typeof(imageList) == "string") {
				tinymce.util.XHR.send({
					url: imageList,
					success: function(text) {
						callback(tinymce.util.JSON.parse(text));
					}
				});
			} else {
				callback(imageList);
			}
		};
	}

	function showDialog(imageList) {
		var win, data = {}, dom = editor.dom, imgElm = editor.selection.getNode();
		var width, height, imageListCtrl, classCtrl;

		function buildImageList() {
			var imageListItems = [{text: 'None', value: ''}];

			tinymce.each(imageList, function(image) {
				imageListItems.push({
					text: image.text || image.title,
					value: editor.convertURL(image.value || image.url, 'src'),
					menu: image.menu
				});
			});

			return imageListItems;
		}

		function isEnlargeable(){
			var clazz = imgElm.getAttribute('class');
			return clazz.indexOf('enlargeable') != -1;
		}

		function setClass(){
			if(win.find('#class')[0].checked()){
				data['class'] = "enlargeable";
			} 
			else {
				data['class'] = " ";
			}
		}

		function onSubmitForm() {
			function waitLoad(imgElm) {
				function selectImage() {
					imgElm.onload = imgElm.onerror = null;
					editor.selection.select(imgElm);
					editor.nodeChanged();
				}

				imgElm.onload = function() {
					if (!data.width && !data.height) {
						dom.setAttribs(imgElm, {
							width: imgElm.clientWidth,
							height: imgElm.clientHeight
						});
					}

					selectImage();
				};

				imgElm.onerror = selectImage;
			}

			data = tinymce.extend(data, win.toJSON());

			setClass();

			if (!data.alt) {
				data.alt = '';
			}

			if (data.width === '') {
				data.width = null;
			}

			if (data.height === '') {
				data.height = null;
			}

			if (data.style === '') {
				data.style = null;
			}

			data = {
				src: data.src,
				alt: data.alt,
				width: data.width,
				height: data.height,
				style: data.style,
				"class": data['class']
			};

			if (!data["class"]) {
				delete data["class"];
			}

			editor.undoManager.transact(function() {
				if (!data.src) {
					if (imgElm) {
						dom.remove(imgElm);
						editor.focus();
						editor.nodeChanged();
					}

					return;
				}

				if (!imgElm) {
					data.id = '__mcenew';
					editor.focus();
					editor.selection.setContent(dom.createHTML('img', data));
					imgElm = dom.get('__mcenew');
					dom.setAttrib(imgElm, 'id', null);
				} else {
					dom.setAttribs(imgElm, data);
				}

				waitLoad(imgElm);
			});
		}

		function srcChange() {
			if (imageListCtrl) {
				imageListCtrl.value(editor.convertURL(this.value(), 'src'));
			}

			getImageSize(this.value(), function(data) {
				if (data.width && data.height) {
					width = data.width;
					height = data.height;

					win.find('#width').value(width);
					win.find('#height').value(height);
				}
			});
		}

		width = dom.getAttrib(imgElm, 'width');
		height = dom.getAttrib(imgElm, 'height');

		if (imgElm.nodeName == 'IMG' && !imgElm.getAttribute('data-mce-object') && !imgElm.getAttribute('data-mce-placeholder')) {
			data = {
				src: dom.getAttrib(imgElm, 'src'),
				alt: dom.getAttrib(imgElm, 'alt'),
				"class": dom.getAttrib(imgElm, 'class'),
				width: width,
				height: height
			};
		} else {
			imgElm = null;
		}

		classCtrl = {
			name: 'class',
			type: 'checkbox',
			label: '',
			text: 'Image agrandissable'
		};

		var generalFormItems = [];

		generalFormItems.push(classCtrl);

		// Simple default dialog
		win = editor.windowManager.open({
			title: 'Propriétés de l\'image',
			data: data,
			width: 300,
			body: generalFormItems,
			onSubmit: onSubmitForm
		});

		//Set checkbox class status
		win.find('#class')[0].checked(isEnlargeable());
	}

	editor.addButton('ottc_image', {
		icon: 'image',
		tooltip: 'Propriétés de l\'image',
		onclick: createImageList(showDialog),
		stateSelector: 'img:not([data-mce-object],[data-mce-placeholder])'
	});

	editor.addMenuItem('ottc_image', {
		icon: 'image',
		text: 'Propriétés de l\'image',
		onclick: createImageList(showDialog),
		context: 'insert',
		prependToContext: true
	});
});