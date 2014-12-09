
var queueDivName="hueResetDiv"
var resetButtonId="resetPhilipsHue"
var stopResetButtonId="stopResetPhilipsHue"
	
function resetPhilips()
{
	var GAL_IEEE;
	var GAL_PANID;
	
	$.fn.getGalPanId=function()
	{
		var that=this;
		/*0x80:Short PanId*/
		this.queue(function()
			{
				setTimeout(function()
					{
						$.ajax(
								{
									url : DEFINEPATH.attributeInfoPath
											+ "?id=0x80",
									type : 'GET'
								})
						.done(
								function(data) {
									var dataBig = json_parse(data);
									
									if (dataBig.status.code == 0) {
										GAL_PANID=dataBig.detail.value;
										console.log("GAL_PANID:"+GAL_PANID);
										that.dequeue();
									} else {
										log.error("Error getting PAN ID");
			
									}
								});
					},2000);
			}
		);
		return this;
	}
	
	$.fn.getGalIeee=function()
	{
		var that=this;
		/*0x9A:Extended PanId*/
		this.queue( function()
			{
				$.ajax(
						{
							url : DEFINEPATH.attributeInfoPath
									+ "?id=0x9A",
							type : 'GET'
						})
				.done(
						function(data) {
							var dataBig = json_parse(data);
							
							if (dataBig.status.code == 0) {
								GAL_IEEE=dataBig.detail.value;
								console.log("GAL_IEEE:"+dataBig.detail.value)
								that.dequeue();
							} else {
								log.error("Error getting PAN ID");
	
							}
						});
			});
		return this;
	}
	
	$.fn.resetPhilipsOnChannel=function(channel)
	{
		var ccrTimeOut = '00000014';
		
		var opChannelRadio = '0xFE';
		
	    var that = this; 
	    this.queue(function() {
	    	$.ajax(
					{
						dataType : "json",
						url : DEFINEPATH.changeChannelPath
								+ 'timeout='
								+ ccrTimeOut
								+ '&scanChannel='
								+ channel
								+ '&scanDuration='
								+ opChannelRadio
					})
			.done(
					function(data) {

						if (data.status.code == 0) {
							
							that.dequeue();
						}

					});
	    });
	    // action the next method in the chain
	    return this;
	    
	}

	$.fn.sendPostRequest=function(afterMsecs,asduLength,asdu)
	{
	    var that = this; 
	    this.queue(function() {
	    	setTimeout(function()
	    	{
		    	$.ajax(
		    			{
						    contentType: "text/xml",
							url : DEFINEURL.interpanUrl,
							method: "POST",
							data: '<?xml version="1.0" encoding="utf-8"?> \
						        <tns:InterPANMessage xmlns:gal="http://www.zigbee.org/GWGSchema" xmlns:tns="http://www.zigbee.org/GWGRESTSchema">\
						        <gal:SrcAddressMode>3</gal:SrcAddressMode>\
						        <gal:SrcAddress>\
						        <gal:IeeeAddress>'+GAL_IEEE+'</gal:IeeeAddress>\
						        </gal:SrcAddress>\
						        <gal:SrcPANID>'+GAL_PANID+'</gal:SrcPANID>\
						        <gal:DstAddressMode>2</gal:DstAddressMode>\
						        <gal:DestinationAddress>\
						        <gal:NetworkAddress>65535</gal:NetworkAddress>\
						        </gal:DestinationAddress>\
						        <gal:DestPANID>65535</gal:DestPANID>\
						        <gal:ProfileID>49246</gal:ProfileID>\
						        <gal:ClusterID>4096</gal:ClusterID>\
						        <gal:ASDULength>'+asduLength+'</gal:ASDULength>\
						        <gal:ASDU>'+asdu+'</gal:ASDU>\
						        </tns:InterPANMessage>',
							success: function(data) {
								console.log(data);
								that.dequeue();
	
							}	
		    			}
		    	);
	    	},afterMsecs);
	    });
	    // action the next method in the chain
	    return this;
	    
	}
	
	$.fn.updateResetDivStatus=function(message)
	{
		var that=this;
		this.queue(function()
				{
					$("#"+queueDivName).html(message);
					that.dequeue();
				}
		);
		return this;
	}

	$.fn.updateGui=function(resetting)
	{
		var that=this;
		this.queue(function()
				{
					if(!resetting)
					{
						$("#"+resetButtonId).hide();
						$("#"+stopResetButtonId).show();
						$("#"+queueDivName).html("");
					}else{
						$("#"+resetButtonId).show();
						$("#"+stopResetButtonId).hide();
						$("#"+queueDivName).html("");
					}
						
					that.dequeue();
				}
		);
		return this;
	}

	
	$("#"+queueDivName).updateGui(false);
	$.each([11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26],
		
		function(i,num){
			
			$("#"+queueDivName)
				.getGalPanId()
				.getGalIeee()
				.updateResetDivStatus("Resetting on channel "+num)
				.resetPhilipsOnChannel(num)
				//ZCL ScanRequest
				.sendPostRequest(10000,9,'110100CAFECAFE0233')
				//ZCL ResetToFactoryNewRequest
				.sendPostRequest(5000,7,'110307CAFECAFE')			
		}
	);
	$("#"+queueDivName).updateGui(true);
	
}

function stopResetPhilips()
{
	$("#"+queueDivName).clearQueue();
	$("#"+resetButtonId).show();
	$("#"+stopResetButtonId).hide();
	$("#"+queueDivName).html("");
	
}


