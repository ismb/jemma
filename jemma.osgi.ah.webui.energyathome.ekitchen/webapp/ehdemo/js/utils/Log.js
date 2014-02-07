var Log = {
	level : 0,
    useAlert: true
};

Log.setLevel = function(val) {
	this.level = val;
};

Log.alert = function(lev, modulo, testo) {
	if (lev <= this.level) {
        log_txt = "[" + modulo + "]: " + testo;  
  	  if (window.console)
  		console.log(log_txt);
      }
};
