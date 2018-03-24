var CronJob = require('cron').CronJob;
var worker = require('./notification.js');

var job = new CronJob({
	cronTime: "0 */5 * * * *", // every 5 minutes
	onTick: worker.start,
	start: true,
	timeZone: "Europe/Berlin"
});

job.start();