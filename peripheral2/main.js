var bleno = require('../..');

var BlenoPrimaryService = bleno.PrimaryService;

var EchoCharacteristic = require('./characteristic');

console.log('bleno - echo');

bleno.on('stateChange', function(state) {
  console.log('on -> stateChange: ' + state);

  if (state === 'poweredOn') {
    bleno.startAdvertising('peripheral2', ['e625601e-9e55-4597-a598-76018a0d293d']);
  } else {
    bleno.stopAdvertising();
  }
});

bleno.on('advertisingStart', function(error) {
  console.log('on -> advertisingStart: ' + (error ? 'error ' + error : 'success'));

  if (!error) {
    bleno.setServices([
      new BlenoPrimaryService({
        uuid: 'e625601e-9e55-4597-a598-76018a0d293d',
        characteristics: [
          new EchoCharacteristic()
        ]
      })
    ]);
  }
});
