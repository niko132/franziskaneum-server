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
var password = "Franz2017";
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
					
					var userNot = '';
					
					if (user.is_teacher) {
						console.log('teacher user');
						
						var shortcut = user.teacher_shortcut;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
							var searchString = aktion.lehrer[0] + ' ' + aktion.info[0];
						
							if (searchString.indexOf(shortcut) > -1) {
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.klasse[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
							}
						}
					} else if (user.school_class >= 11) {
						console.log('sek2 user');
						
						var schoolClass = user.school_class;
						var courses = user.courses;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
						
							if (aktion.klasse[0].indexOf(schoolClass) > -1 && hasCourse(aktion.klasse[0], courses)) {							
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.lehrer[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
							}
						}
					} else {
						console.log('sek1 user');
						
						var schoolClass = user.school_class + '/' + user.school_class_index;
						
						for (var j = 0; j < haupt.aktion.length; j++) {
							var aktion = haupt.aktion[j];
						
							if (aktion.klasse[0].indexOf(schoolClass) > -1) {							
								userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.lehrer[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
								userNot += '\n';
							}
						}
					}
					
					console.log('USER TOKEN: ' + user.token);
					console.log(userNot);
					console.log('\n');
				}
			});
		});
	}
);