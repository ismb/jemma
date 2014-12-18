var iFrameConfig = {
	MODULE : "Config",
	htmlContent: $(document.createElement('div')).attr('id', 'iframeConfig').height('100%')
}


iFrameConfig.Init=function(){
	var divElettro = $("#iframeConfig");

	if (divElettro.length == 0) {
		$("#ContentMain").append(iFrameConfig.htmlContent);
	} else {
		$("#iframeConfig").show();
	}
	
	$("#iframeConfig").append("<div style=\"width: 100%; height:100%; overflow: hidden;\"><iframe  style=\"width: 100%; height:100%; overflow: hidden; border:none;\" src=\"conf/index_embedded.html\"></iframe></div>");
}

iFrameConfig.Exit = function () {
	
	$("#iframeConfig iframe").empty();
	$("#iframeConfig").html(null);
	$("#iframeConfig").remove();
}
