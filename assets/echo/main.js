
// main is called with controller always in the global scope.  Controller supports the the following:
//    addRoute('regex', method) - adds a URL route
//    addHandler(method) - adds an SMS handler
//    sendSMS(recipient, message) - sends an SMS
//    ??

// user can define their functions inline in main.js or do includes for their views ie: include('echo.js')


//===============
// HTTP STUFF
//===============

function echo(request, response){
	response.put("uri", request.get('uri'));
	response.put("method", request.get('method'));
	console.log("log log log");

	var params = request.get('params').keys();
	while (params.hasMoreElements()){
	    var key = params.nextElement();
		console.log(key + ": " + request.get('params').get(key));
	}
	
	// by default the template will be named after the view, but can be overridden
	response.setTemplate("echo.ejs")
	
	// or alternatively
	// response.setTemplate("/echo/echo.ejs")
}

// users tie URLs to handlers via addRoute()
controller.addRoute("./echo", echo);

// same thing
controller.addRoute("echo", echo);

// alternatively, though not as flexible to renames
// controller.addRoute("/echo/echo", echo)

// support regexes
// controller.addRoute("./echo/(\w+)/", echo_word)

//=====================
// SMS STUFF
//=====================
function smsEcho(sms){
	if (sms.is_keyword("echo")){
		controller.sendSMS(sms.number, "echo " + sms.text);
	}
}

// users can add SMS support via addHandler()
controller.addHandler(echoHandler);