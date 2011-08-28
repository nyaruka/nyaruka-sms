
function Request(){
	this.url = _request.url;
	this.method = _request.method;

	this.params = {};
	
	this.has = function(key){
		return (key in this.params);
	};
	
	this.get = function(key){
		if (key in this.params){
			return this.params[key];
		} else {
			return null;
		}
	};
	
	var iterator = _request.params().keys();
	while(iterator.hasMoreElements()){
		var key = '' + iterator.nextElement();
		this.params[key] = '' + _request.params().getProperty(key);
	}
	
	return this;
};

__request = Request();