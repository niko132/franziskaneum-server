<?php
	if (isset($_POST['token']) && !empty($_POST['token'])) {
		$token = $_POST['token'];
		echo $token;
		echo '\n';
	} else {
		$db_connection = pg_connect("host=ec2-54-75-227-92.eu-west-1.compute.amazonaws.com dbname=d7i5315uupgr2m user=mydxnmvauddyee password=493c42d4734c867c3fda62972ff79b1df2994deea5af051b3b1a4052e89f0381");
		
		if (isset($_POST['old_token']) && !empty($_POST['old_token'])) {
			$old_token = $_POST['old_token'];
			echo $old_token;
			echo '\n';
			
			// update existing row
		} else {
			$result = pg_query($db_connection, "INSERT INTO users (token) VALUES ('" . $token . "')");
			// create new row
		}
	}
?>