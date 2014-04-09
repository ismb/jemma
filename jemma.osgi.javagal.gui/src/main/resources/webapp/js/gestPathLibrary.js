/**
 * 
 */

var DEFINEPATH = {
	channelPath : "/json/net/default/channel",
	infoForRestartGal : "/json/startup?", // timeout=00002710&index=00"
	startGalPath : "/json/startup?", // timeout={0,08x}&start=true",
	reStartGalPath : "/json/reset?", // timeout={0,08x}&startMode={1,01x}
	versionPath : "/json/version",
	viewDevicePath : "/json/net/default/wsnnodes?mode=cache",
	detailDevicePath : "/json/net/default/wsnnodes/nodedescriptorservicelist", // timeout={0:x8}&address={1:x2/8}",
	changeChannelPath : "/json/net/default/localnode/frequencyagility?",
	viewLQI : "/json/net/default/allwsnnodes/lqi",
	leavePath : "/json/net/default/wsnnodes/",
	permitJoinPath : "/json/net/default/allwsnnodes/permitjoin"
}