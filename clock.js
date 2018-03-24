var http = require('http');
var CronJob = require('cron').CronJob;
var worker = require('./notification.js');

new CronJob({
	cronTime: "0 */5 * * * *", // every 5 minutes
	onTick: worker.start,
	start: true,
	timeZone: "Europe/Berlin"
});

new CronJob({
	cronTime: "0 */20 * * * *", // every 20 minutes
	onTick: function() {
		console.log("Keeping the dyno alive...");
		http.get("http://franziskaneum.herokuapp.com/"); // use this to prevent the dyno from sleeping ;)
	},
	start: true,
	timeZone: "Europe/Berlin",
	runOnInit: true
});