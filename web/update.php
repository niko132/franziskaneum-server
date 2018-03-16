<?php
	if (isset($_POST['token']) && !empty($_POST['token'])) {
		$token = $_POST['token'];
		echo $token;
		echo '\n';
		
		$db_connection = pg_connect("host=ec2-54-75-227-92.eu-west-1.compute.amazonaws.com dbname=d7i5315uupgr2m user=mydxnmvauddyee password=493c42d4734c867c3fda62972ff79b1df2994deea5af051b3b1a4052e89f0381");
		
		$old_token = $_POST['old_token'];
		$is_teacher = $_POST['is_teacher'];
		$school_class = $POST['school_class'];
		$school_class_index = $POST['school_class_index'];
		$teacher_shortcut = $POST['teacher_shortcut'];
		$courses = $POST['courses'];
		
		if (!empty($old_token)) {
			echo ' OLD TOKEN ';
			
			$result = pg_query($db_connection, "SELECT token FROM users WHERE token = '" . $old_token . "'";
			echo 'rows: ' . pg_num_rows($result);
			
			// wenn old_token in datenbank -> updaten
			// sonst neue Zeile einfügen
			
			// update existing row
		} else {
			$result = pg_query($db_connection, "INSERT INTO users (token) VALUES ('" . $token . "')");
			echo ' NEW ONE ';
			echo pg_last_error($db_connection);
			// create new row
		}
	}
?>