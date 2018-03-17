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
				console.log(res.rows.length + ' users');
			});
			
			console.log('Hello World!');
		});
	}
);