/**
 * 
 */
var jGGButton = function(spec){ //spec, optionDB){

	var that = {};
	
	that.getWidth = function(){
		return spec.widthMenu;
	};
	
	that.getLangName = function(){
		return spec.langName;
	};
	
	that.superior = function(name){
		var method = that[name];
		return function(){
			return method.apply(that, arguments);
		};
	};
	
	return that;
};

var jGGSxMenu = function(spec){ //spec, optionDB){
	
	optionMenu = spec || {};
    optionMenu.widthMenu = optionMenu.widthMenu || 250;
    optionMenu.langName = optionMenu.langName || "LANG.menu.";

	//Constructor
	var that = jGGButton(optionMenu);
	
	that.newButton = function(divBtn, callbackFnc, args){
		
		var divId = "#" + divBtn;
		var divLang = eval(that.getLangName() + divBtn);
		
		var myArgs = args || null;
		
		var myCallBack = function(p, a){
			var idTpl = a;
			p(idTpl);
	    };

		$(divId).button().width(that.getWidth()).click(function(evnt){
			if (myArgs){
				return myCallBack(callbackFnc, args)
			} else {
				return null;
			}
		}).prop('value', divLang);
		
		$(divId + " span").text(divLang);
	};
	
	return that;
};




var jGGCenterPaneMenu = function(spec){ //spec, optionDB){
	
	optionMenu = spec || {};
    optionMenu.widthMenu = optionMenu.widthMenu || 300;
    optionMenu.langName = optionMenu.langName || "LANG.centerPane.";

	//Constructor
	var that = jGGSxMenu(optionMenu),
		superNewButton = that.superior('newButton');
	
	that.newButton = function(divBtn, callbackFnc, args){
		superNewButton(divBtn, callbackFnc, args);
	};

	
	return that;
};


var jGGCenterPaneMenuChannel = function(spec){ //spec, optionDB){
	
	optionMenu = spec || {};
    optionMenu.widthMenu = 100;
    optionMenu.langName = optionMenu.langName || "LANG.centerPane.";

	//Constructor
	var that = jGGSxMenu(optionMenu),
		superNewButton = that.superior('newButton');
	
	that.newButton = function(divBtn, callbackFnc, args){
		superNewButton(divBtn, callbackFnc, args);
	};

	
	return that;
};



var jGGNetworkManageMenu = function(spec){ //spec, optionDB){
	
	optionMenu = spec || {};
    optionMenu.widthMenu = optionMenu.widthMenu || 250;
    optionMenu.langName = optionMenu.langName || "LANG.networkManage.";

	//Constructor
	var that = jGGSxMenu(optionMenu),
		superNewButton = that.superior('newButton');
	
	that.newButton = function(divBtn, callbackFnc, args){
		superNewButton(divBtn, callbackFnc, args);
	};
	
	return that;
};
