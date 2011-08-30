
function Request(){
	this.url = '' + _request.url();
	this.method = '' + _request.method();

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
		var key = '' + iterator.nextElement().toString();
		this.params[key] = '' + _request.params().getProperty(key).toString();
	}
	
	return this;
};

__request = Request();

function Response(){
	
	this.set = function(key, value){
		console.log("TYPE: " + typeof value);
		if(typeof value === 'string' ){
			_response.set(key, value);
		} 
		else if (typeof value === 'object') {
			if (value.length != null) {
				console.log("Setting JSON Array: " + value);
				_response.setJSONArray(key, JSON.stringify(value));
			} else {
				_response.setJSON(key, JSON.stringify(value));
			}
		}
			
	};

	this.setTemplate = function(template) {
		_response.setTemplate(template);
	};

	return this;
}

__response = Response();






var MAX_DUMP_DEPTH = 10;
      
function dumpObj(obj, name, indent, depth) {
      if (depth > MAX_DUMP_DEPTH) {
             return indent + name + ": <Maximum Depth Reached>\n";
      }
      if (typeof obj == "object") {
             var child = null;
             var output = indent + name + "\n";
             indent += "\t";
             for (var item in obj)
             {
                   try {
                          child = obj[item];
                   } catch (e) {
                          child = "<Unable to Evaluate>";
                   }
                   if (typeof child == "object") {
                          output += dumpObj(child, item, indent, depth + 1);
                   } else {
                          output += indent + item + ": " + child + "\n";
                   }
             }
             return output;
      } else {
             return obj;
      }
}

