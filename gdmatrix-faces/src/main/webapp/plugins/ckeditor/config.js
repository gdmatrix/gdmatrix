/**
 * @license Copyright (c) 2003-2018, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here.
	// For complete reference see:
	// http://docs.ckeditor.com/#!/api/CKEDITOR.config

	// The toolbar groups arrangement, optimized for two toolbar rows.
	config.toolbarGroups = [
		{ name: 'document', groups: [ 'mode', 'document', 'doctools' ] },
    '/',
		{ name: 'clipboard', groups: [ 'clipboard', 'undo' ] },
		{ name: 'editing', groups: [ 'find', 'selection', 'editing' ] },
		{ name: 'links', groups: [ 'links' ] },
		{ name: 'insert', groups: [ 'insert' ] },
		{ name: 'forms', groups: [ 'forms' ] },
		{ name: 'tools', groups: [ 'tools' ] },
		{ name: 'others', groups: [ 'others' ] },
		'/',
		{ name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
		{ name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ] },
		{ name: 'styles', groups: [ 'styles' ] },
		{ name: 'colors', groups: [ 'colors' ] },
		{ name: 'about', groups: [ 'about' ] }
	];

	// Remove some buttons provided by the standard plugins, which are
	// not needed in the Standard(s) toolbar.
	config.removeButtons = 'Scayt,About,JustifyBlock';

	// Set the most common block elements.
	config.format_tags = 'p;h1;h2;h3;h4;h5';

	// Simplify the dialog windows.
	//config.removeDialogTabs = 'image:advanced;link:advanced';
        
  // Extra plug-ins
  config.extraPlugins = 'dialogadvtab,balloonpanel,selectall,iframe';
  config.removePlugins = 'scayt';

  // Turn off ACF (Advanced content filter)
  //config.allowedContent = true;
  config.allowedContent = {
      $1: {
          // Use the ability to specify elements as an object.
          elements: CKEDITOR.dtd,
          attributes: true,
          styles: true,
          classes: true
      }
  };        
  config.disallowedContent = 'table[cellspacing,cellpadding,border]; iframe[scrolling,frameborder,longdesc,align]';
  config.disableNativeSpellChecker = false;
  
  config.filebrowserBrowseUrl = 'go.faces?xmid=31723';
  config.filebrowserWindowWidth = '900';
  config.filebrowserWindowHeight = '85%';
};

CKEDITOR.timestamp = '20190208';
