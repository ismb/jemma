var Tools = {}
/**
 * Gestisce il resize di un'imamgine in modo che sia nel contenitore senza cambiare le proporzioni
 */
Tools.addCSSinDocument = function(url){
	var headID = document.getElementsByTagName("head")[0];         
	var cssNode = document.createElement('link');
	cssNode.type = 'text/css';
	cssNode.rel = 'stylesheet';
	cssNode.href = url+"?"+new Date().getTime();
	cssNode.media = 'screen';
	headID.appendChild(cssNode);
}

Tools.addJavaScriptinDocument = function(url){
	var headID = document.getElementsByTagName("head")[0];         
	var newScript = document.createElement('script');
	newScript.type = 'text/javascript';
	newScript.src = url;
	headID.appendChild(newScript);
}

function Querystring(qs) {
	this.params = {};

	if (qs == null)
		qs = location.search.substring(1, location.search.length);
	if (qs.length == 0)
		return;

	// Turn <plus> back to <space>
	// See: http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1

	qs = qs.replace(/\+/g, ' ');
	var args = qs.split('&'); // parse out name/value pairs separated via &
	// split out each name=value pair
	for ( var i = 0; i < args.length; i++) {
		var pair = args[i].split('=');
		var name = decodeURIComponent(pair[0]);

		var value = (pair.length == 2) ? decodeURIComponent(pair[1]) : name;

		this.params[name] = value;
	}
}

Querystring.prototype.get = function(key, default_) {
	var value = this.params[key];
	return (value != null) ? value : default_;
}

Querystring.prototype.contains = function(key) {
	var value = this.params[key];
	return (value != null);
}

var LazyScript = {
	foglio : []
};

LazyScript.load = function(urlscr, callback) {
	try {
		if ($.inArray(urlscr, LazyScript.foglio) == -1) {
			LazyScript.foglio.push(urlscr);
			var script = document.createElement("script");
			script.src = urlscr+"?"+new Date().getTime();
			script.type = "text/javascript";
			$("head")[0].appendChild(script);

			if (callback) {
				script.onreadystatechange = function() {
					if (script.readyState == 'loaded' || script.readyState == 'complete') {
						callback();
					}
				}
				script.onload = function() {
					callback();
					return;
				}
			}
		} else {
			if (callback) {
				callback();
			}
		}
	} catch (e) {
		alert(e);
	}
}

