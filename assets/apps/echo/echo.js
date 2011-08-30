
// just output all our request parameters 
response.put("uri", request.get('uri'));
response.put("method", request.get('method'));
console.log("log log log");

var params = request.get('params').keys();
while (params.hasMoreElements()){
    var key = params.nextElement();
	console.log(key + ": " + request.get('params').get(key));
}