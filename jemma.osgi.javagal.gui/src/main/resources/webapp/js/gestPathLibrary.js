var DEFINEPATH = {
	channelPath : "/json/net/default/channel",
	infoForRestartGal : "/json/startup?", 
	startGalPath : "/json/startup?", 
	reStartGalPath : "/json/reset?", 
	recoveryGalPath : "/json/recovery",
	versionPath : "/json/version",
	viewDevicePath : "/json/net/default/wsnnodes?mode=cache",
	detailDevicePath : "/json/net/default/wsnnodes/nodedescriptorservicelist", 
	changeChannelPath : "/json/net/default/localnode/frequencyagility?",
	viewLQI : "/json/net/default/allwsnnodes/lqi",
	leavePath : "/json/net/default/wsnnodes/",
	permitJoinPath : "/json/net/default/allwsnnodes/permitjoin",
	simpleDescriptorPath : "/json/net/default/wsnnodes/services",
	attributeInfoPath : "/json/net/default/ib",
	
}

var DEFINEURL = {
	interpanUrl: "http://"+window.location.hostname+":9000/net/default/wsnnodes/0000?timeout=100&urilistener='localhost:1500'"
		
}