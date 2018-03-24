var CronJob = require('cron').CronJob;
var worker = require('./worker.js');

var job = new CronJob({
	name: "Notification Scheduler",
	cronTime: "*/5 * * * * *", // every 5 minutes
	onTick: worker.start(),
	start: true,
	id: "notificationscheduler",
	timeZone: "Europe/Berlin"
});

job.start();