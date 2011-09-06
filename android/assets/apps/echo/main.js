	
db.ensureCollection("contacts");
db.ensureCollection("labs");
db.labs.ensureIntIndex("lab_id");

function echo(req, resp){
    if (req.method == 'POST'){
        console.log("POST: " + JSON.stringify(req.params));
        if (req.params.first && req.params.last){
            rec = req.params;
            db.contacts.save(rec);
        }
    }

    resp.set('url', resp.url);
    resp.set('method', resp.method);
    resp.set('contacts', db.contacts.find({}).all());
}

function parseAddMessage(msg){
    var result = { status: 'E' };
    var tokens = msg.split(' ');

    // next token is the id
    var id = tokens[1];
    result.lab_id = id;
        
    // does this token already exist?  if so, fail
    var existing = db.labs.find({ lab_id: id });
    if (existing.count() > 0){
        result.response = "This is a duplicate entry for this lab.  That is not allowed";
        return result;
    }
        
    // the rest from here are the name until we reach the first .
    var name = "";
    var i=2;
    for(i=2; i<tokens.length; i++){
        if (tokens[i].indexOf(".") === 0){
            break;
        }
        name += tokens[i] + " ";
    }
    result.name = name.trim();
        
    // now parse which tests need to be undertaken
    var tests = [];
    for(;i<tokens.length; i++){
        if (tokens[i].indexOf(".") === 0){
            var test = tokens[i].substring(1);
            tests[tests.length] = test;
        }
    }
    result.tests = tests;
    result.status = 'N';
    result.response = "Your lab request has been recorded";
    
    return result;
}

function parseResultMessage(msg){
    var result = { status: 'E' };
    var tokens = msg.split(' ');
    
    var id = tokens[1];
    var existing = db.labs.find({ lab_id: id });
    if (existing.count() === 0){
        result.response = "Unable to find lab request with id: " + id + "  Please verify id.";
        return result;
    }
    result = existing.next();
    
    // parse the results
    results = {};
    for (var i=2; i<tokens.length; i++){
        if (tokens[i].indexOf(".") === 0){
            var test_name = tokens[i].substring(1).toLowerCase();
            
            if (i+1 >= tokens.length){
                result.response = "Incorrect format, each test identifier must be followed by P or F";
                return result;
            }
            
            var test_result = tokens[i+1].toUpperCase();
            if (test_result != "P" && test_result != "F"){
                result.response = "Incorrect format, each test result must be P or F, was: " + test_result;
                return result;
            }
            
            results[test_name] = test_result;
        }
    }
    result.results = results;
    db.labs.save(result);

    result.response = "Your lab results have been saved, the original patient has been notified";
    return result;
}

function parseMessage(msg){
    var result = { status: 'E' };
    var tokens = msg.split(' ');
    
    if (tokens[0].toLowerCase() == 'add'){
        return parseAddMessage(msg);
    }
    else if (tokens[0].toLowerCase() == 'res'){
        return parseResultMessage(msg);
    }
    else {
        result.response = "Unknown keyword, must be 'add' or 'res'.";
        return result;
    }
}

function handle_msg(req, resp){
    var result = {};
    if (req.method == "POST" && req.params.msg){
        var msg = req.params.msg;
        resp.set('msg', msg);
        
        result = parseMessage(msg);
        
        if (result.status == 'N'){
            // save away this result
            db.labs.save(result);
        }
    }
    resp.set('result', result);
}

router.addHttpHandler('echo', echo);
router.addHttpHandler('msg', handle_msg);
