var CRC32 = require('crc-32');
var request = require('request');
var xml2js = require('xml2js');
const { Client } = require('pg');

function hasCourse(searchString, courses) {
	for (var i = 0; i < courses.length; i++) {
		var course = courses[i];
		
		if (searchString.indexOf(course) > -1) {
			return true;
		}
	}
	
	return false;
}

var username = "FranzApp";
var password = "Franz2018";
var url = "http://franziskaneum.de/vplan/vplank.xml";
var auth = "Basic " + new Buffer(username + ":" + password).toString("base64");

request(
	{
		url: url,
		headers: {
			"Authorization": auth
		}
	},
	function(error, response, body) {
		var parser = new xml2js.Parser();
		parser.parseString(body, function(err, result) {			
			var haupt = result.vp.haupt[0];
			
			// TODO: gucken ob Vertretungsplan neu ist
			
			const pgClient = new Client({
				connectionString: process.env.DATABASE_URL,
			});
			pgClient.connect();
			
			pgClient.query("SELECT * FROM users", (err, res) => {
				for (var i = 0; i < res.rows.length; i++) {
					var user = res.rows[i];
					
					var userNotifications = [];
					
					var userNot = '';
					var userNotCount = 0;
					
					if (user.is_teacher) {
						console.log('teacher user');
						
						var shortcut = user.teacher_shortcut;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
							var searchString = aktion.lehrer[0] + ' ' + aktion.info[0];
						
							if (searchString.indexOf(shortcut) > -1) {
								userNotifications.push(aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.klasse[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0]);
								
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.klasse[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
								
								userNotCount += 1;
							}
						}
					} else if (user.school_class >= 11) {
						console.log('sek2 user');
						
						var schoolClass = user.school_class;
						var courses = user.courses;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
						
							if (aktion.klasse[0].indexOf(schoolClass) > -1 && hasCourse(aktion.klasse[0], courses)) {							
								userNotifications.push(aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.klasse[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0]);
								
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.lehrer[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
								
								userNotCount += 1;
							}
						}
					} else {
						console.log('sek1 user');
						
						var schoolClass = user.school_class + '/' + user.school_class_index;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
						
							if (aktion.klasse[0].indexOf(schoolClass) > -1) {							
								userNotifications.push(aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.klasse[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0]);
								
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.lehrer[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
								
								userNotCount += 1;
							}
						}
					}
					
					var userNotificationHashes = [];
					var newNotificationIndices = [];
					var hashesString = "";
					
					for (var j = 0; j < userNotifications.length; j++) {
						var hash = CRC32.str(userNotifications[j]);
						userNotificationHashes.push(hash);
						
						if (j == userNotifications.length - 1) {
							hashesString += hash;
						} else {
							hashesString += hash + ", ";
						}
						
						console.log(hash);
						
						if (user.notification_hashes.indexOf(hash) <= -1) { // this hash wasn't already presented
							newNotificationIndices.push(j);
						}
					}
					
					console.log("NEW NOTIFICATIONS: " + newNotificationIndices.length);
					
					// insert 'userNotificationHashes' in database
					console.log("query: " + hashesString);
					pgClient.query("UPDATE users SET notification_hashes = '{" + hashesString + "}' WHERE token = '" + user.token + "'", (err, res) => {
						
					});
					
					if (newNotificationIndices.length > 0) { // new notifications -> request fcm
						var notificationCount = newNotificationIndices.length;
					
						var body = {
							to: user.token,
							notification: {
								body: notificationCount + ' Änderungen '
							}
						}
						
						console.log(JSON.stringify(body));
						
						request(
							{
								url: 'https://fcm.googleapis.com/fcm/send',
								method: 'POST',
								body: JSON.stringify(body),
								headers: {
									"Content-Type": "application/json",
									"Authorization": "key=AIzaSyDCVVrr4nA3Pd6LmOWO7i0m95ASCTusw68"
								}
							},
							function(error, response, body) {
								console.log(body);
								
								// TODO: Handle "InvalidRegistration" -> Delete Row
							}
						);
					}
				
				/*
					if (userNot) {
						var body = {
							to: user.token,
							notification: {
								body: userNotCount + ' Änderungen'
							}
						}
						
						console.log(JSON.stringify(body));
						
						request(
							{
								url: 'https://fcm.googleapis.com/fcm/send',
								method: 'POST',
								body: JSON.stringify(body),
								headers: {
									"Content-Type": "application/json",
									"Authorization": "key=AIzaSyDCVVrr4nA3Pd6LmOWO7i0m95ASCTusw68"
								}
							},
							function(error, response, body) {
								console.log(body);
								
								// TODO: Handle "InvalidRegistration" -> Delete Row
							}
						);
					}
					*/
				}
			});
		});
	}
);