var Mailgun = require('mailgun');
var mailgunInitialized = false;

Parse.Cloud.afterSave('Devices', function(request) {
  Parse.Config.get({ success: function(config) {
    var config = Parse.Config.current();
    if(!mailgunInitialized) {
      var mailgunApikey = config.get('mailgun_apikey');
      var mailgunDomain = config.get('mailgun_domain');
      if(mailgunApikey && mailgunDomain) {
        Mailgun.initialize(mailgunDomain, mailgunApikey);
        console.log('Mailgun initialized for domain ' + mailgunDomain);
        mailgunInitialized = true;
      } else {
        console.error('Missing mailgun apikey and/or domain in config');
      }
    }

    if(mailgunInitialized) { 
      var notificationsTo = config.get('notifications_to');
      var notificationsFrom = config.get('notifications_from');

      var deviceModel = request.object.get('model');
      var deviceSerial = request.object.get('serial');
      var checkedOutTo = request.object.get('checkedOutTo');

      var message = 'Model: ' + deviceModel + '\n' +
          'Serial: ' + deviceSerial + '\n';
      if(checkedOutTo) {
        message += 'To: ' + checkedOutTo;
      }

      Mailgun.sendEmail({
        to: notificationsTo,
        from: notificationsFrom,
        subject: '[DeviceTracker] ' + deviceModel + ' has been checked ' + (checkedOutTo ? 'out' : 'in'),
        text: message
      }, {
        success: function(httpResponse) {
          console.log(httpResponse);
        },
        error: function(httpResponse) {
          console.error('[Mailgun.sendEmail error] ' + httpResponse);
        }
      });
    }
  }});
});