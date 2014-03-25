/**
 * 
 */

var jGGGestCenterPane = function(){ //spec, optionDB){
	
	optionCenterPane = {};
	optionCenterPane.campo1 = 0;

	//Constructor
	var that;
	that = {};
	
	that.templateActivator = function(idTpl){

		var t = window.document.querySelector("#"+idTpl);
		
		$("#center").empty();
		$("#center").append(t.content.cloneNode(true));
	};
	
	return that;
}