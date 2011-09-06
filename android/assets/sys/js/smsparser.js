
function ParseException(sms, error){
    this.sms = sms;
    this.error = error;
    return this;
}

function SmsParser(sms){
    // our complete SMS
    this.sms = sms;

    // current offset into our SMS
    this.cursor = 0;

    // seeks our cursor to the next non-delimeter character
    this.seek = function(start){
        for(var i=start; i<this.sms.length; i++){
            if (this.sms[i] != ' '){
                break;
            }
        }
        this.cursor = i;
    };

    // returns the next string in the sequence
    this.nextString = function(conf){
        // already at end of string?  then return defined
        if (this.cursor == this.sms.length){
            return undefined;
        }

        var nextSpace = this.sms.indexOf(' ', this.cursor);
        var result = "";
        if (nextSpace == -1){
            result = this.sms.substring(this.cursor);
            this.cursor = this.sms.length;
        } else {
            result = this.sms.substring(this.cursor, nextSpace);
            this.seek(nextSpace);
        }

        // apply matches
        if (result && conf && conf.matches){
            regex = new RegExp(conf.matches, "gi")
            if (!regex.test(result)){
                result = undefined;
            }
        }

        // should we throw if undefined?
        if (!result && conf && conf.error){
            throw new ParseException(this.sms, conf.error);
        }

        return result;
    };

    // returns the next int in the sequence
    this.nextInt = function(conf){
        // get the next string
        var str = this.nextString();

        // undefined?  return that
        if (str == undefined){
            return undefined;
        }

        // otherwise, attempt to cast to an int
        var value = parseInt(str);
        if (isNaN(value)){
            // should we throw since the value isn't an int?
            if (conf && conf.error){
                throw new ParseException(this.sms, conf.error);
            }

            return undefined;
        } else {
            return value;
        }
    };

    this.seek(0);

    return this;
}