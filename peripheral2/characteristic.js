var util = require('util');

var bleno = require('../..');

var BlenoCharacteristic = bleno.Characteristic;

var counter = 0;
var getURL;
var url;

function wait(time){
return new Promise((resolve, reject) => {
setTimeout(() => {
resolve();
}, time);
});
}

var EchoCharacteristic = function() {
  EchoCharacteristic.super_.call(this, {
    uuid: '26e2b12b-85f0-4f3f-9fdd-91d114270e6e',
    properties: ['read'],
    value: null
  });

  this._updateValueCallback = null;
};

util.inherits(EchoCharacteristic, BlenoCharacteristic);

EchoCharacteristic.prototype.onReadRequest = function(offset, callback) {
  counter++;
  console.log('EchoCharacteristic - onReadRequest: value = ' + counter);

wait(0).then(() => {
var {Client} = require('pg');

var client = new Client({
user: 'm210786',
host: 'localhost', 
database: 'list-db',
password: 'password', 
port: 5432
})
query = null;

if(counter < 5){
query = {
name: 'fecth-user',
text: 'select url1 from listdb where uuid = $1', 
values: ['e625601e-9e55-4597-a598-76018a0d293d'],
}
}else if(counter < 10){
query = {
name: 'fecth-user',
text: 'select url2 from listdb where uuid = $1', 
values: ['e625601e-9e55-4597-a598-76018a0d293d'],
}
}else{
query = {
name: 'fecth-user',
text: 'select url3 from listdb where uuid = $1', 
values: ['e625601e-9e55-4597-a598-76018a0d293d'],
}
}

client.connect()

client.query(query, (err, res) => {
if (err) {
console.log(err.stack)
} else {
getURL = res.rows[0];
client.end()
}
})

return wait(100);
}).then(() => {
  console.log(getURL);
  url = Object.values(getURL).toString();
  this._value = new Buffer(url);
  callback(this.RESULT_SUCCESS, this._value);
});
};

EchoCharacteristic.prototype.onSubscribe = function(maxValueSize, updateValueCallback) {
  console.log('EchoCharacteristic - onSubscribe');

  this._updateValueCallback = updateValueCallback;
};

EchoCharacteristic.prototype.onUnsubscribe = function() {
  console.log('EchoCharacteristic - onUnsubscribe');

  this._updateValueCallback = null;
};

module.exports = EchoCharacteristic;
