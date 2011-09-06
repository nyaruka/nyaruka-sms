function Cursor(cursor){
	this.cursor = cursor;

	this.next = function(){
		var record = JSON.parse(cursor.next().toString());
		return record;
	};	
	
	this.hasNext = function(){
		return this.cursor.hasNext();
	};
    
    this.count = function(){
        return this.cursor.count();
    };
	
	this.all = function(){
		var items = [];
		while(this.hasNext()){
			items[items.length] = this.next();
			console.log("item--");
		}
		console.log("num items: " + items.length);
		return items;
	};
	
	return this;
}


function Collection(collection){
	this.collection = collection;

	this.ensureIntIndex = function(name){
		this.collection.ensureIntIndex(name);
	};
	
	this.ensureStrIndex = function(name){
		this.collection.ensureStrIndex(name);
	};	

	this.save = function(json){
		this.collection.save(JSON.stringify(json));
	};
	
	this.find = function(json){
		return Cursor(this.collection.find(JSON.stringify(json)));
	};
	
	return this;
}

function DB(){
	this.ensureCollection = function(name){
		console.log("DB: " + _db);
		var collection = _db.ensureCollection(name);
		this[name] = Collection(collection);
	};
	
	return this;
}

var db = DB();
