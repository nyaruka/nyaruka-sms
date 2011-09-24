/** 
 * {{name}} main.js
 *
 * This file defines all the handlers for the {{name}} app.
 */
  
ensureCollection('{{name}}')
db.{{name}}.ensureIntIndex('{{name}}_id');

/* The root of our application */
function index(req, resp){
    resp.set('url', resp.url);
    resp.set('method', resp.method);
    resp.set('records', db.{{name}}.find({}).all());
}

router.addHttpHandler('/', index);