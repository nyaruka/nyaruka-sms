
// main is called with controller always in the global scope.  Controller supports the the following:
//    addRoute('regex', method) - adds a URL route
//    addHandler(method) - adds an SMS handler
//    sendSMS(recipient, message) - sends an SMS
//    ??

// user can define their functions inline in main.js or do includes for their views ie: include('echo.js')


db.ensureCollection("posts");

//===============
// HTTP STUFF
//===============

function echo(request, response){
	if (request.method == "POST"){
		console.log("POST: " + request.params['post']);
		db.posts.save({ post: request.params['post'],
						date: new Date().getTime() });	
	} 
	var cursor = db.posts.find({});
	var records = [];	
	while (cursor.hasNext()){
		records[records.length] = cursor.next();
	}
	response.set("records", records);

	console.log("URI: " + request.url);
	response.set("url", request.url);
	response.set("method", request.method);
	console.log("log log log");

	for (var key in request.params){
		console.log(key + ": " + request.params[key]);
	}
	
	// by default the template will be named after the view, but can be overridden
	response.setTemplate("echo.ejs")
	
	// or alternatively
	// response.setTemplate("/echo/echo.ejs")
}

// users tie URLs to handlers via addRoute()
// controller.addRoute("./echo", echo);

// same thing
router.addHttpHandler("echo", echo);

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
// controller.addHandler(echoHandler);