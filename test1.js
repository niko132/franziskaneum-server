var request = require('request');
var xml2js = require('xml2js');
const { Client } = require('pg');

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
					var row = res.rows[i];
					
					var userNot = '';
					
					for (int j = 0; j < haupt.aktion.length; j++) {
						var aktion = haupt.aktion[j];
						
						if (aktion.klasse[0].indexOf(row.school_class + '/' + row.school_class_index)) {
							userNot += aktion.stunde[0] + '. St. ' + aktion.fach[0] + ' ' + aktion.lehrer[0] + ' ' + aktion.raum[0] + ' ' + aktion.info[0];
							userNot += '\n';
						}
					}
					
					console.log('USER TOKEN: ' + row.token);
					console.log(userNot);
					console.log('\n');
				}
			});
		});
	}
);