			

db.ensureCollection("contacts");

function echo(req, resp){
    if (req.method == 'POST'){
        console.log("POST: " + JSON.stringify(req.params));
        if (req.params.first && req.params.last){
            console.log("SAVING");
            db.contacts.save(req.params);
        }
    }

    resp.set('url', resp.url);
    resp.set('contacts', db.contacts.find({}).all());
}

router.addHttpHandler('echo', echo);


