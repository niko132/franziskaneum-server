var CronJob = require('cron').CronJob;
var worker = require('./worker.js');

var job = new CronJob({
	cronTime: "*/1 * * * *", // every 5 minutes
	onTick: worker.start(),
	start: true,
	timeZone: "Europe/Berlin"
});

job.start();