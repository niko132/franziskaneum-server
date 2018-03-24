var CronJob = require('cron').CronJob;
var worker = require('./worker.js');

var fifteenSeconsAfterMinute = function() {
  console.log("Another minute is gone forever. Hopefully, you made the most of it...");
}

var job = new CronJob({
  cronTime: "15 * * * * *",//15 seconds after every minute
  onTick: fifteenSeconsAfterMinute,
  start: true,
  timeZone: "America/Los_Angeles"
});

job.start();