var isIntegrationDmaOn;
var loadEventsOnFields = false;
var checkChangingFields = false;
var buttonsToEnable = new Array();



function reloadGridFromUrl(url, idGrid) {
	jQuery(idGrid).jqGrid('setGridParam', {url: url});
	jQuery(idGrid).trigger('reloadGrid');
}

jQuery.io = jQuery.io || {};
jQuery.extend(jQuery.io, {
	/*
	 * Una specializzazione del metodo jQuery.getJSON che permette
	 * di specificare se la richiesta debba essere sincrona o
	 * asincrona.
	 */
	getJson: function (url, data, callback, async) {
		// rendiamo data opzionale
		if (jQuery.isFunction(data)) {
			async = callback;
			callback = data;
			data = null;
		}

		async = (async != undefined) ? async : true;

		return jQuery.ajax({
			type: "POST",
			url: url,
			data: data,
			success: callback,
			dataType: 'json',
			async: async
		});
	},

	/*
	 * Invoca l'url richiesta per effettuare lo scaricamento del file.
	 */
	download: function (requestUrl, evt) {
		if (evt.preventDefault) {
			evt.preventDefault();
		} else {
			evt.returnValue = false;
		}
		window.location.href = requestUrl;
	},

	/*
	 * Invoca l'url richiesta e prende il sottoinsieme di properties racchiuse
	 * dall'attributo root.
	 */
	getJsonObject: function (url, data, root) {
		var object = new Object();
		jQuery.io.getJson(url, data, function (response) {
			object = (root && (root != null)) ? response[root] : response;
		}, false);

		return object;
	}
});

jQuery.fn.injectSelectOptions = function (/*Object*/ options) {

	if (this.filter('select').size() == 0) {
		return this;
	}

	var optionsMap = jQuery.extend(true, {
		url: null,
		data: null,
		root: null,
		value: /*String*/ function (/*Object*/ data) {
			return null;
		},
		text: /*String*/ function (/*Object*/ data) {
			return '';
		},
		cssStyle: /*String*/ function (/*Object*/ data) {
			return null;
		},
		cssClass: /*String*/ function (/*Object*/ data) {
			return null;
		},
		attachData: true,
		emptyOption: false
	}, options || {});

	var data = jQuery.io.getJsonObject(optionsMap.url, optionsMap.data, optionsMap.root);

	return this.filter('select').each(function () {
		var jqSelect = jQuery(this);
		jqSelect.empty();

		if (optionsMap.attachData) {
			jqSelect.data('attachedJsonOptions', data);
		}

		if (optionsMap.emptyOption) {
			_appendEmptyOption(jqSelect, optionsMap, optionsMap.emptyOption);
		}

		jQuery.each(data, function (index, singleOptionData) {
			_appendOptionData(jqSelect, optionsMap, singleOptionData);
		});
		//_appendEmptyOption(jqSelect, optionsMap, optionsMap.emptyOption);
	}).end();

	/*
	 * Appende alla select l'option coerentemente con i dati richiesti.
	 */
	function _appendOptionData(/*jQueryObject*/ jqSelect, /*Object*/ optionsMap, /*Object*/ singleOptionData) {
		var value = _invokeOptionMapHandler(optionsMap.value, singleOptionData);
		var text = _invokeOptionMapHandler(optionsMap.text, singleOptionData);
		var cssStyle = _invokeOptionMapHandler(optionsMap.cssStyle, singleOptionData);
		var cssClass = _invokeOptionMapHandler(optionsMap.cssClass, singleOptionData);

		if (value != 'undefined') {
			var htmlOption = '<option ';
			if (value != null) htmlOption += 'value=\'' + value + '\' ';
			if (cssStyle != null) htmlOption += 'style=\'' + cssStyle + '\' ';
			if (cssClass != null) htmlOption += 'class=\'' + cssClass + '\' ';
			htmlOption += '>' + text + '</option>';

			jqSelect.append(htmlOption);
		}
	}

	/*
	 * Invoca un handler che puÃ² essere:
	 *   una funzione, ed in tal caso ne restituisce l'esito del processamento
	 *   una stringa, ed in tal caso restituisce elemento dei dati designato da tale stringa.
	 */
	function _invokeOptionMapHandler(/*Object*/ handler, /*Object*/ singleOptionData) {
		if (jQuery.isFunction(handler)) {
			return handler(singleOptionData);
		} else if (jQuery.lang.isString(handler)) {
			return singleOptionData[handler];
		}
	}

	/*
	 * Restituisce l'OptionMap per l'empty value.
	 */
	function _appendEmptyOption(jqSelect, optionsMap, emptyOption) {
		jqSelect.append('<option value=\'\'> </option>');
	}
};
/**
 * Restituisce i dati ottenuti dalla richiesta json per popolare la select.
 *
 * @chainable No
 */
jQuery.fn.getInjectedData = function () {
	return this.filter('select').data('attachedJsonOptions');
};

var hideFieldsInMask = function (jspNameParam) {
	$.getJSON('../sicurezza/getFieldMask.action', {jspName: jspNameParam}, function (data) {
		//alert(data);
		if(data == null || data.elementi == null)
			return;
		for (var i = 0; i < data.elementi.length; i++) {
			$("#" + data.elementi[i].elementName).hide();
			$('label[for="' + data.elementi[i].elementName + '"]').hide();
		}
	});
}

var clearForm = function (dialogName) {
	$("#" + dialogName).find(':input').each(function () {
		switch (this.type) {
		case 'password':
		case 'select-multiple':
		case 'select-one':
		case 'text':
		case 'textarea':
			$(this).val('');
			break;
		case 'checkbox':
		case 'radio':
			this.checked = false;
		}

	});
}


var checkUncheck = function (localRow, toCheck, gridId) {
	if (toCheck) {
		$("#" + gridId).jqGrid('expandNode', localRow);
		$("#" + gridId).jqGrid('expandRow', localRow);
	}
	else {
		$("#" + gridId).jqGrid('collapseNode', localRow);
		$("#" + gridId).jqGrid('collapseRow', localRow);
	}
	var children = $('#' + gridId).jqGrid('getNodeChildren', localRow);
	for (var i = 0; i < children.length; i++) {
		if (toCheck) {
			if ($.inArray(children[i].row_id, $("#" + gridId).jqGrid('getGridParam', 'selarrrow')) < 0)
				$("#" + gridId).jqGrid('setSelection', children[i].row_id);
		}
		else
			$("#" + gridId).jqGrid('resetSelection', children[i].row_id);
		$("input#jqg_" + gridId + "_" + children[i].row_id).prop('checked', toCheck);
		checkUncheck(children[i], toCheck, gridId);
	}

}

var fieldCharCheck = function () {

	$(".onlycharsautocomplete").on('paste',function(event){
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		var element = this;
		setTimeout(function () {
			var stringa = $(element).val();
			for(var i =0;i< stringa.length - 1;i++){
				var lettera =stringa[i]+"";
				if(badChar.indexOf(lettera) != (-1)){
					$(element).val("");
					return false;
				}
			}
			
		}, 100);				
	});

	$(".onlycharsautocomplete").unbind("keypress").on("keypress", function (event) {
		var lettera = String.fromCharCode(event.keyCode);
		if (event.keyCode === $.ui.keyCode.TAB &&
				$(this).data("autocomplete").menu.active) {
			event.preventDefault();
		}
		//alert("collaudo lettera "+lettera+" del keycode "+event.keyCode+" .");
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		if ((badChar.indexOf(lettera) != (-1))){
			event.preventDefault();
		}
	});


	$(".onlycharsautocompleteuppercase").on('paste',function(event){
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		var element = this;
		setTimeout(function () {
			var stringa = $(element).val();
			for(var i =0;i< stringa.length - 1;i++){
				var lettera =stringa[i]+"";
				if(badChar.indexOf(lettera) != (-1)){
					$(element).val("");
					return false;
				}
			}
			$(element).val($(element).val().toUpperCase());
		}, 100);				
	});

	$(".onlycharsautocompleteuppercase").unbind("keypress").on("keypress", function (event) {
		$(this).val($(this).val().toUpperCase());
		var lettera = String.fromCharCode(event.which);

		if (event.which === $.ui.keyCode.TAB &&
				$(this).data("autocomplete").menu.active) {
			event.preventDefault();           
		}

		//alert("collaudo lettera "+lettera+" del keycode "+event.keyCode+" .");
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		if ((badChar.indexOf(lettera) != (-1))){
			event.preventDefault();
		} else {

			if($(this)[0].selectionStart != $(this)[0].selectionEnd)
			{
				var keyPressed = event.which;
				if(keyPressed!=8 && event.ctrlKey==false){
					var valore = $(this).val().substring(0,$(this)[0].selectionStart) + String.fromCharCode(keyPressed).toUpperCase()+ $(this).val().substring($(this)[0].selectionEnd );;
					$(this).val(valore);
					return false;
				}

			}
			var keyPressed = event.which;
			//controllo per backspace e ctrl key , fix firefox
			if(keyPressed!=8 && event.ctrlKey==false){
				$(this).val($(this).val() + String.fromCharCode(keyPressed).toUpperCase());
				event.preventDefault();
			}
		}
	});

	$(".onlycharsuppercase").on('paste',function(event){
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		var element = this;
		setTimeout(function () {
			var stringa = $(element).val();
			for(var i =0;i< stringa.length - 1;i++){
				var lettera =stringa[i]+"";
				if(badChar.indexOf(lettera) != (-1)){
					$(element).val("");
					return false;
				}
			}
			$(element).val($(element).val().toUpperCase());
		}, 100);				
	});

	$(".onlycharsuppercase").unbind("keypress").on("keypress", function (event) {
		$(this).val($(this).val().toUpperCase());	
		//lancio l'evento change (se presente) prima del preventDefault
		if ($._data(this, "events")["change"]!=null)
			$(this).change();

		var lettera = String.fromCharCode(event.keyCode);
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		if ((badChar.indexOf(lettera) != (-1)))
			event.preventDefault();
		else {
			if($(this)[0].selectionStart != $(this)[0].selectionEnd)
			{
				var keyPressed = event.which;
				if(keyPressed!=8 && event.ctrlKey==false){
					var valore = $(this).val().substring(0,$(this)[0].selectionStart) + String.fromCharCode(keyPressed).toUpperCase()+ $(this).val().substring($(this)[0].selectionEnd );;
					$(this).val(valore);
					return false;
				}

			}
			var keyPressed = event.which;
			if(keyPressed!=8 && event.ctrlKey==false){
				$(this).val($(this).val() + String.fromCharCode(keyPressed).toUpperCase());
				event.preventDefault();
			}
		}
	});
	
	$(".onlychars").on('paste',function(event){
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");

		var element = this;
		setTimeout(function () {
			var stringa = $(element).val();
			for(var i =0;i< stringa.length - 1;i++){
				var lettera =stringa[i]+"";
				if(badChar.indexOf(lettera) != (-1)){
					$(element).val("");
					return false;
				}
			}
		}, 100);				
	});

	$(".onlychars").unbind("keypress").on("keypress", function (event) {
		//lancio l'evento change (se presente) prima del preventDefault
		if ($._data(this, "events")["change"]!=null)
			$(this).change();

		var lettera = String.fromCharCode(event.keyCode);
		var badChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ",", "?", "!", "\"", "�", "$", "%", "&", "/", "=", "^");
		if ((badChar.indexOf(lettera) != (-1)))
			event.preventDefault();

	});

	$(".uppercasefield").on('paste',function(event){
		var element = this;
		setTimeout(function () {
			$(element).val($(element).val().toUpperCase());
		}, 100);				
	});

	$(".uppercasefield").unbind("keypress").on("keypress", function (event) {
		if($(this)[0].selectionStart != $(this)[0].selectionEnd)
		{

			var keyPressed = event.which;
			if(keyPressed!=8 && event.ctrlKey==false){
				var valore = $(this).val().substring(0,$(this)[0].selectionStart) + String.fromCharCode(keyPressed).toUpperCase()+ $(this).val().substring($(this)[0].selectionEnd );
				$(this).val(valore);
				return false;
			}

		}
		var keyPressed = event.which;
		if(keyPressed!=8 && event.ctrlKey==false){
			$(this).val($(this).val() + String.fromCharCode(keyPressed).toUpperCase());
		}
		//lancio l'evento change (se presente) prima del preventDefault
		if ($._data(this, "events")["change"]!=null)
			$(this).change();

		if(keyPressed!=8 && event.ctrlKey==false){
			event.preventDefault();
		}
		//lo script soprastante riesce a bypassare il maxlength dell'html, quindi faccio un controllo manuale
		var maxL = $(this).prop("maxlength");
		if($(this).val()!= null && $(this).val()!="" && $(this).val().length>maxL && maxL!=-1)
			$(this).val($(this).val().substring(0,maxL));

	});
	
	
	$(".onlynumbers").on('paste',function(event){
		var GoodChar = new Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "0");
		var element = this;
		setTimeout(function () {
			var stringa = $(element).val();
			for(var i =0;i< stringa.length - 1;i++){
				var lettera =stringa[i]+"";				
				if(GoodChar.indexOf(lettera) == (-1)){
					$(element).val("");
					return false;
				}
			}
		}, 100);				
	});

	

	$(".onlynumbers").unbind("keydown").on("keydown", function (event) {

		//lancio l'evento change (se presente) prima del preventDefault
		if ($._data(this, "events")["change"]!=null)
			$(this).change();

		if (event.ctrlKey) {
			if (event.keyCode != 65 && event.keyCode != 97 && event.keyCode != 67 && event.keyCode != 99 && event.keyCode != 86 && event.keyCode != 118)
				event.preventDefault();
		}
		else if (!event.shiftKey) {
			if (((event.keyCode < 48 || event.keyCode > 57) && (event.keyCode < 96 || event.keyCode > 105)) && event.keyCode != 8 && event.keyCode != 46)
				event.preventDefault();
		}
		else {
			event.preventDefault();
		}
	});
};

var sendRequest = function (url, data, onSuccess, onError) {
	jQuery.ajax({
		async: false,
		dataType: 'json',
		type: 'GET',
		url: url,
		data: data,
		success: onSuccess,
		error: onError
	});
}

var addNameProperty = function (dialogName) {
	$("#" + dialogName).find(':input').each(function () {
		if ($(this).prop("name") == null || $(this).prop("name") == undefined || $(this).prop("name") == "")
			$(this).prop("name", $(this).prop("id"))
	});

}

var disableForm = function (dialogName) {
	$("#" + dialogName).find(':input').each(function () {
		switch (this.type) {
		case 'password':
		case 'select-multiple':
		case 'select-one':
		case 'text':
			$(this).prop("disabled",true);
			break;
		case 'checkbox':
		case 'radio':
			this.checked = false;
			$(this).prop("disabled",true);
		}

	});

	$("#"+dialogName+" .hasDatepicker").each(function() {
		$(this).datepicker('disable');
	});

	$("#"+dialogName+" textarea").each( function() {
		$(this).prop("disabled",true);   
	});

}

var enableForm = function (dialogName) {
	$("#" + dialogName).find(':input').each(function () {
		switch (this.type) {
		case 'password':
		case 'select-multiple':
		case 'select-one':
		case 'text':
			$(this).prop("disabled",false);
			break;
		case 'checkbox':
		case 'radio':
			this.checked = false;
			//$(this).removeProp("disabled");
			$(this).prop("disabled",false);
		}

	});

	$("#"+dialogName+" .hasDatepicker").each(function() {
		$(this).datepicker('enable');
	});

	$("#"+dialogName+" textarea").each( function() {
		$(this).prop("disabled",false);
	});

}

var reloadGridFromLocalArray = function(idGrid, arrayData)
{
	$("#" + idGrid).jqGrid("clearGridData", true);
	for(var i=0;i<=arrayData.length;i++)
		jQuery("#" + idGrid).jqGrid('addRowData',i+1,arrayData[i]);
}




var sortGridFromLocalArray = function(idGrid, columnName, orderType)
{
	$("#" + idGrid).jqGrid('setGridParam',{sortname: columnName, sortorder: orderType});
}

function stringEndsWith(str, suffix) {
	return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

var saveChangedCheckbox = function(divId)
{
	if(checkChangingFields)
	{
		//per le checkbox
		$("#" + divId + " .gridcheckbox").each( function() {
			$(this).change( function() {
				var fieldName = $(this).prop("id");         			
				if ($("#container_changedFields").val().indexOf(fieldName + ",") == -1)
				{
					$(this).closest("#evt_container").data('changed', true);
					$("#container_changedFields").val($("#container_changedFields").val() + fieldName + ",");
				}

			});
		});
	}
}

var saveChangedAutocomplete = function(autocompleteId)
{
	if(checkChangingFields)
	{    	
		//controllo l'esistenza del campo _hidden, eventualmente lo creo
		if($("#"+autocompleteId+"_hidden").length == 0)
			$("#"+autocompleteId).after("<input type='hidden' id='"+autocompleteId+"_hidden' name='"+autocompleteId+"_hidden'/>");     			

		//riempo il campo _hidden
		if ($("#"+autocompleteId).val() == "")
			$("#" + autocompleteId + "_hidden").val("");
		else
			$("#" + autocompleteId + "_hidden").val($("#"+autocompleteId).val());

		$("#" + autocompleteId + "_hidden").trigger("change");

		if ($("#container_changedFields").val().indexOf(autocompleteId + "_hidden" + ",") == -1)
		{
			$("#"+autocompleteId).closest("#evt_container").data('changed', true);
			$("#container_changedFields").val($("#container_changedFields").val() + $("#"+autocompleteId).prop("id") + "_hidden"  + ",");
		}
	}
}
var reloadGridFromJsonArray = function(idGrid, arrayData)
{
	var myarray = new Array()
	for(var i=0;i<arrayData.length;i++){
		var item = {
				desc: arrayData[i]['desc'],	
				id: arrayData[i]['id'],		    		
				parent: arrayData[i]['parent'],
				isLeaf: arrayData[i]['isLeaf'],
				level: arrayData[i]['level'],
				loaded: arrayData[i]['loaded'],
				expanded: arrayData[i]['expanded']
		};	
		myarray.push(item);   
	}	
	$("#"+idGrid).setGridParam({
		datastr: myarray,
		datatype: "jsonstring"
	}).trigger("reloadGrid");	
}

//sostituisce tutte le occorrenze in una stringa
var replaceAll = function (find, replace, str) {
	return str.replace(new RegExp(find, 'g'), replace);
}

//scrive un valore dentro un campo indipendentemente dal tipo di campo
var writeField = function (field, value, type) {

	//controllo se il tipo � dichiarato o devo calcolarlo
	if(type==null)
		type=$("#"+field).prop('tagName');

	type=(type!=null ? type.toLowerCase() : null);

	if(type=="input")
	{
		type=$("#"+field).prop('type');

		if(type=="checkbox")
		{
			$("#"+field).prop("checked",true);
		}
		else if(type=="radio")
		{
			$("#"+field).prop("checked",true);
		}
		else
		{
			$("#"+field).val(value);
		}

	}
	else if(type=="textarea")
	{
		$("#"+field).val(value);
	}

}

function gotoUrl(url)
{
    $.blockUI("<h3>Page loading...</h3>");
    //$("#center").nextAll().remove();
    $("#center").empty();
    $.ajax({
        async: true,
        type: 'GET',
        url: url,
        success: function (data) {
        	//$(data).appendTo('#center');
        	$('#center').append(data);
        	$.unblockUI();
        	
        }
    });
}

function split(val) {
    return val.split(/,\s*/);
}

function extractLast(term) {
    return split(term).pop();
}

function setUrlAndGrid(actionUrl, currSearchParams){
	page.push("${pageContext.request.contextPath}"+actionUrl);
	
	var keys = new Array();
	
	if (currSearchParams != null && currSearchParams != "" && currSearchParams.indexOf(".action") < 0){

		currSearchParams.replace(/([^=&]+)=([^&]*)/g, function (str, key, value) {
		   
		    keys[key] = value;
		   
		});
	}
	grid.push(currSearchParams);
	return keys;
};

function callBack(currSearchParams){
	gotoUrl(page.pop());
}

function fillForms(filler){
	
	for(var arr in filler){
        if (filler[arr] != null && filler[arr] != " " && filler[arr] != "") {
            $("#"+arr).val(filler[arr]);
        }   
	 }
	
}
function enableButtonsForUsecases()
{
    for(var i = 0; i<buttonsToEnable.length;i++)
    {
        $("#" + buttonsToEnable[i]).button("enable",true);
    }
    $("#")
}

function padLeft(pad, tmpStringToHex){

	var pad2 = "00";
	var pad4 = "0000";
	var pad16 = "0000000000000000";
	
	var myPad = eval("pad"+pad);
	var str = tmpStringToHex;
	str = myPad.substring(0, myPad.length - str.length) + str;
	return str;
}

function padLeftInverse(hexToDec){

	var str = parseInt(hexToDec, 16);
	return str;
}

function padVector(arrOfByte){

	var pad2 = "00";
	var tmpStr = "";
	var tmpChar = "";
	
	for(var i = 0; i<arrOfByte.length; ){
		tmpChar1 = arrOfByte[i].toString(16);
		tmpChar1 = pad2.substring(0, pad2.length - tmpChar1.length) + tmpChar1;
		tmpChar2 = arrOfByte[i+1].toString(16);
		tmpChar2 = pad2.substring(0, pad2.length - tmpChar2.length) + tmpChar2;
		tmpStr = tmpStr + '0x' + tmpChar1.toUpperCase() + " " + '0x' + tmpChar2.toUpperCase() + " ";
		i = i+2;
    }
	return tmpStr;
}

function padVectorInverse(arrOfByte){

	var tmpStr = "[";
	var tmpChar = "";
	if ((typeof arrOfByte == 'string') && (arrOfByte.length == 79)){
		var arrOfByte = arrOfByte.split(" ");
	}
	
	for(var i = 0; i<arrOfByte.length; i++){
		tmpChar1 = parseInt(arrOfByte[i], 16);
		tmpStr = tmpStr + tmpChar1 + ",";
    }
	tmpStr = tmpStr.substring(0, tmpStr.length - 1) + "]";
	return tmpStr;
}

function verBigAddr(cp, fieldToVerify){
	var arrCP = cp.split(' ');
	var err = 0;
	if (arrCP.length != 16){
		alert('Errato formato indirizzo! Verificare il campo ' + fieldToVerify);
		err = 1;
	} else {
		for (var iCounter = 0; iCounter < arrCP.length; iCounter++){
			var espressione = /^0x[0-9|a-f|A-F]{2}$/;
			if (!espressione.test(arrCP[iCounter])){
				alert('Errato formato indirizzo! Verificare il campo ' + fieldToVerify);
				err = 1;
				break;
			}
		}
	}
	if (err == 0){
		var strRtn = arrCP.join(' ');
		return strRtn;
	} else {
		controlAllOk = false;
		return null;
	}
}

function verShortAddr(cp, fieldToVerify){
	var espressione = /^0x[0-9|a-f|A-F]{4}$/;
	cp = cp.trim();
	if (!espressione.test(cp)){
		alert('Errato formato indirizzo! Verificare il campo ' + fieldToVerify);
		controlAllOk = false;
		return null;
	} else {
		return cp;
	}
}



//At the top of your script:
if (!window.console)
	console = {
		log : function() {
		}
	};
// If you use other console methods, add them to the object literal above

// Then, anywhere in your script:
console
		.log('This message will be logged, but will not cause an error in IE7');





window.alert = function(msg, title, callback) {
	apprise(msg, {}, callback);
}

window.confirm = function(msg, title, callback) {
	apprise(msg, {
		'verify' : true,
		'textYes' : 'Yes',
		'textNo' : 'No'
	}, callback);
}

window.prompt = function(message, value, title, callback) {
	apprise(msg, {
		input : true
	}, callback);
}

var confirmV2 = function(msg, callback) {
	apprise(msg, {
		'newVerify' : true,
		'textYes' : 'Yes',
		'textNo' : 'No',
		'textCancel' : 'Cancel'
	}, callback);
}