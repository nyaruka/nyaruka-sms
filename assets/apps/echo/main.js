
db.ensureCollection("contacts");

function echo(req, resp){
    if (req.method == 'POST'){
        console.log("POST: " + JSON.stringify(req.params));
        if (req.params.first && req.params.last){
            rec = req.params;
            rec.submitted = {
                time: new Date().getTime(),
                ip: '127.0.0.1'
            };

            db.contacts.save(rec);
        }
    }

    resp.set('url', resp.url);
    resp.set('method', resp.method);
    resp.set('contacts', db.contacts.find({}).all());
}

router.addHttpHandler('echo', echo);
