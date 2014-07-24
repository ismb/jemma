var myProfile = [];
var myClusters = [];
var myManufacturers = null;
var iXMLCounter = 0;

/*
 * Restituisce l'array per il Profile-ID
 */
var getProfiles = function() {
	var tmpString = '{\'\':null,';
	var rtnrString;

	$.each(myProfile, function(iCounter, vProfile) {
		tmpString += "'" + vProfile.zigbeeprofileid + "':'"
				+ vProfile.description + "',";
	});
	tmpString = tmpString.substring(0, tmpString.length - 1) + "}";
	eval('rtnrString=' + tmpString);
	return rtnrString;
};

/*
 * Restituisce il singolo Profile
 */
var getSingleProfileFromID = function(prflID) {
	var rtrnPrfl = null;
	$.each(myProfile, function(iCounter, vProfile) {
		if (prflID == vProfile.zigbeeprofileid) {
			rtrnPrfl = vProfile;
		}
	});
	return rtrnPrfl;
}

/*
 * Restituisce il singolo Manufacturer
 */
var getManufacturerFromID = function(manufaturerID) {
	var rtrnPrfl = null;
	$.each(myManufacturers, function(iCounter, vManufacturer) {
		if (manufaturerID == vManufacturer.manufacturerid) {
			rtrnPrfl = vManufacturer.description;
		}
	});
	return rtrnPrfl;

}

var getClusterDescription = function(profileID, clusterID) {

}

/*
 * Restituisce il singolo Device-Name
 */
var getSingleDeviceName = function(dvcID, selectProfileID) {
	var rtrnDvcN = null;
	var myPrfl = getSingleProfileFromID(selectProfileID);
	if (myPrfl && myPrfl.device) {
		$.each(myPrfl.device, function(iCounter, vDev) {
			if (dvcID == vDev.deviceidentifier) {
				rtrnDvcN = vDev.description;
			}
		});
	}
	return rtrnDvcN;
}

/*
 * Restituisce l'array per il Device-ID
 */
var getDevice = function(prflID) {
	var tmpString;
	var rtnrString;
	var myPrfl = getSingleProfileFromID(prflID);

	if (prflID) {
		tmpString = '{';
		$.each(myPrfl.device, function(iCounter, vDev) {
			if (vDev.devProfilesid == myPrfl.profilesid) {
				tmpString += "'" + vDev.deviceidentifier + "':'"
						+ vDev.description + "',";
			}
		});
		tmpString = tmpString.substring(0, tmpString.length - 1) + "}";
	} else {
		tmpString = '{}';
	}
	eval('rtnrString=' + tmpString);
	return rtnrString;
};

/*
 * Restituisce l'oggetto per completare i cluster
 */
var getClusters = function(devId) {
	var tmpObj = {
		inputCM : '',
		outputCM : '',
		inputCO : '',
		outputCO : ''
	};
	$.each(myProfile, function(iCounter, vProf) {
		$.each(vProf.device, function(iCounter, vDev) {
			if (vDev.deviceidentifier == devId) {
				$.each(vDev.servermandatorylist, function(i, v) {
					if (tmpObj.inputCM.indexOf(v.toUpperCase()) == -1) {
						tmpObj.inputCM += '0x' + v.toUpperCase() + ' ';
					}
				});
				$.each(vDev.clientmandatorylist, function(i, v) {
					if (tmpObj.outputCM.indexOf(v.toUpperCase()) == -1) {
						tmpObj.outputCM += '0x' + v.toUpperCase() + ' ';
					}
				});
				$.each(vDev.serveroptionallist, function(i, v) {
					if (tmpObj.inputCO.indexOf(v.toUpperCase()) == -1) {
						tmpObj.inputCO += '0x' + v.toUpperCase() + ' ';
						if ((i % 4) == 0) {
							tmpObj.inputCO += '<br />';
						}
					}
				});
				$.each(vDev.clientoptionallist, function(i, v) {
					if (tmpObj.outputCO.indexOf(v.toUpperCase()) == -1) {
						tmpObj.outputCO += '0x' + v.toUpperCase() + ' ';
						if ((i % 4) == 0) {
							tmpObj.outputCO += '<br />';
						}
					}
				});
			}
		});
	});
	return tmpObj;
}

/*
 * Restituisce l'oggetto per completare i cluster
 */
var getClusterDescription = function(devId) {
	var tmpObj = {
		inputCM : '',
		outputCM : '',
		inputCO : '',
		outputCO : ''
	};
	$.each(myProfile, function(iCounter, vProf) {
		$.each(vProf.device, function(iCounter, vDev) {
			if (vDev.deviceidentifier == devId) {
				$.each(vDev.servermandatorylist, function(i, v) {
					tmpObj.inputCM += v;
				});
				$.each(vDev.clientmandatorylist, function(i, v) {
					tmpObj.outputCM += v;
				});
				$.each(vDev.serveroptionallist, function(i, v) {
					tmpObj.inputCO += v;
				});
				$.each(vDev.clientoptionallist, function(i, v) {
					tmpObj.outputCO += v;
				});
			}
		});
	});
	return tmpObj;
}

var idClstrIsInMyClusters = function(idClstr) {
	var rtrnBool = false;
	$.each(myClusters, function(iClstr, valClstr) {
		if ((idClstr == valClstr.clustersidentifier) && (!rtrnBool)) {
			rtrnBool = true;
		}
	});
	return rtrnBool;
}

var getClusterDescription = function(idClstr) {
	var res = null;
	$.each(myClusters, function(iClstr, valClstr) {
		if ((idClstr == valClstr.clustersidentifier)) {
			res = valClstr;
			return;
		}
	});
	return res;
}

var setMyXML = function(xmlObj) {

	myProfile.push(xmlObj.profile);

	$.each(xmlObj.clusters, function(iClstr, valClstr) {
		if (!idClstrIsInMyClusters(valClstr.clustersidentifier)) {
			myClusters.push(valClstr);
		}
	});

	iXMLCounter++;
	if (iXMLCounter < XMLPATH.length) {
		launchXmlReader(iXMLCounter);
	}
}

var setMyXMLManufacturer = function(xmlObj) {
	myManufacturers=xmlObj.manufacturers;
}

var launchXmlReader = function(iCounter) {
	readProfileAndClusterFromXML(XMLPATH[iCounter], setMyXML);

}

var launchManufaturerXmlReader = function() {
	readManufacturer(XMLPATHMANUFACTURER, setMyXMLManufacturer);

}

function readProfileAndClusterFromXML(xmlpath, callback) {
	var returnObj = {
		profile : {
			profilesid : null,
			zigbeeprofileid : null,
			description : null
		},
		clusters : new Array()
	};
	$.ajax({
		type : "GET",
		dataType : "xml",
		url : xmlpath
	}).done(
			function(xml) {
				$(xml).find('profileinformation').each(
						function() {
							returnObj.profile.profilesid = $(this).find(
									'profilesid').text();
							returnObj.profile.zigbeeprofileid = $(this).find(
									'zigbeeprofileid').text();
							returnObj.profile.description = $(this).find(
									'description').text();
						});
				var device = new Array();
				$(xml).find('devices').each(
						function() {
							device.push({
								profilesid : $(this).find('profilesid').text(),
								deviceidentifier : $(this).find(
										'deviceidentifier').text(),
								description : $(this).find('description')
										.text()
							});
						});
				var servermandatorylist = new Array();
				$(xml).find('servermandatorylist').each(function() {
					servermandatorylist.push({
						deviceid : $(this).find('deviceid').text(),
						clusterid : $(this).find('clusterid').text()
					});
				});
				var clientmandatorylist = new Array();
				$(xml).find('clientmandatorylist').each(function() {
					clientmandatorylist.push({
						deviceid : $(this).find('deviceid').text(),
						clusterid : $(this).find('clusterid').text()
					});
				});
				var serveroptionallist = new Array();
				$(xml).find('serveroptionallist').each(function() {
					serveroptionallist.push({
						deviceid : $(this).find('deviceid').text(),
						clusterid : $(this).find('clusterid').text()
					});
				});
				var clientoptionallist = new Array();
				$(xml).find('clientoptionallist').each(function() {
					clientoptionallist.push({
						deviceid : $(this).find('deviceid').text(),
						clusterid : $(this).find('clusterid').text()
					});
				});
				$(xml).find('clusters').each(
						function() {
							var tmpClstr = {
								clustersidentifier : $(this).find(
										'clustersidentifier').text(),
								description : $(this).find('description')
										.text()
							};
							returnObj.clusters.push(tmpClstr);
						});

				returnObj.profile.device = new Array();
				$.each(device, function(idx, val) {
					var myDevice = {};
					myDevice.devProfilesid = val.profilesid;
					myDevice.deviceidentifier = val.deviceidentifier;
					myDevice.description = val.description;
					myDevice.servermandatorylist = new Array();
					$.each(servermandatorylist, function(idx, val) {
						if (val.deviceid == myDevice.deviceidentifier) {
							myDevice.servermandatorylist.push(val.clusterid);
						}
					});
					myDevice.clientmandatorylist = new Array();
					$.each(clientmandatorylist, function(idx, val) {
						if (val.deviceid == myDevice.deviceidentifier) {
							myDevice.clientmandatorylist.push(val.clusterid);
						}
					});
					myDevice.serveroptionallist = new Array();
					$.each(serveroptionallist, function(idx, val) {
						if (val.deviceid == myDevice.deviceidentifier) {
							myDevice.serveroptionallist.push(val.clusterid);
						}
					});
					myDevice.clientoptionallist = new Array();
					$.each(clientoptionallist, function(idx, val) {
						if (val.deviceid == myDevice.deviceidentifier) {
							myDevice.clientoptionallist.push(val.clusterid);
						}
					});
					returnObj.profile.device.push(myDevice);
				});
				callback(returnObj);
			});
}

function readManufacturer(xmlpath, callback) {
	var returnObj = {
		manufacturers : new Array()
	};
	$.ajax({
		type : "GET",
		dataType : "xml",
		url : xmlpath
	}).done(function(xml) {

		$(xml).find('root').each(function() {
			$(this).find('manufacturer').each(function() {
				var tmpManufacturer = {
					manufacturerid : $(this).find('manufacturerid').text(),
					description : $(this).find('description').text()
				};
				returnObj.manufacturers.push(tmpManufacturer);
			});
		});
		callback(returnObj);
	});
}

launchXmlReader(iXMLCounter);
launchManufaturerXmlReader();