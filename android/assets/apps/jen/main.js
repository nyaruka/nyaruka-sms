/** 
 * jen main.js
 *
 * This file defines all the handlers for the jen app.
 */
  
ensureCollection('jen')
db.jen.ensureIntIndex('jen_id');

/* The root of our application */
function index(req, resp){
    resp.set('url', resp.url);
    resp.set('method', resp.method);
    resp.set('records', db.jen.find({}).all());
}

router.addHttpHandler('/', index);
