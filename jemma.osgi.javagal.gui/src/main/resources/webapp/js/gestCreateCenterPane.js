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
		
    	var arrTplToImport = {tplChannelNetwork: './advancedControl.html', 
    	                      tplViewDevice: './networkDevices.html', 
    	                      tplViewGraphNodes: './networkTopology.html', 
    	                      tplManageNetwork: './zigbeeNetworkInfo.html'};
    	
    	var tmpFileToLoad = eval("arrTplToImport."+idTpl);
    	
    	$.ajax({
			dataType:"html",
			url: tmpFileToLoad //arrTplToImport[iCounter]
		}).done(function(data){
			/*$('#ghostDiv').empty();
    		$("#ghostDiv").append(data);
    		var childClones = $('#'+idTpl).children().clone(true,true);*/
    		$('#center').empty();
    		$('#center').append(data);
    		
    		return that;
    	});
	};
	
	return that;
}