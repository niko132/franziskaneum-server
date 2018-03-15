var {google} = require('googleapis');

function getAccessToken() {
	return new Promise(function(resolve, reject) {
		var key = require('./service-account.json');
		var jwtClient = new google.auth.JWT(
			key.client_email,
			null,
			key.private_key,
			SCOPES,
			null
		);
		jwtClient.authorize(function(err, tokens) {
			if (err) {
				reject(err);
				return;
			}
			resolve(tokens.access_token);
		});
	});
}

console.log('test');
console.log('\n');

getAccessToken().then(function(result) {
	console.log(result);
}, function(err) {
	console.log(err);
});