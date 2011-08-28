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
