<?php
	function createUpdateQuery($token, $old_token, $is_teacher, $school_class, $school_class_index, $teacher_shortcut, $courses, $show_notifications) {
		$query = "UPDATE users SET token = '" . $token . "'";
		
		if (!empty($is_teacher)) {
			$query .= ", is_teacher = ";
			$query .= $is_teacher;
		}
		
		if (!empty($school_class)) {
			$query .= ", school_class = ";
			$query .= $school_class;
		}
		
		if (!empty($school_class_index)) {
			$query .= ", school_class_index = ";
			$query .= $school_class_index;
		}
		
		if (!empty($teacher_shortcut)) {
			$query .= ", teacher_shortcut = ";
			$query .= "'" . $teacher_shortcut . "'";
		}
		
		if (!is_null($courses)) {
			$query .= ", courses = ";
			$query .= "'{" . $courses . "}'";
		}
		
		if (!empty($show_notifications)) {
			$query .= ", show_notifications = ";
			$query .= $show_notifications;
		}
		
		$query .= " WHERE token = '" . $old_token . "'";
			
		return $query;
	}
	
	function createInsertQuery($token, $is_teacher, $school_class, $school_class_index, $teacher_shortcut, $courses, $show_notifications) {
		$fields = "token";
		$values = "'" . $token . "'";
		
		if (!empty($is_teacher)) {
			$fields .= ", is_teacher";
			$values .= ", " . $is_teacher;
		}
		
		if (!empty($school_class)) {
			$fields .= ", school_class";
			$values .= ", " . $school_class;
		}
		
		if (!empty($school_class_index)) {
			$fields .= ", school_class_index";
			$values .= ", " . $school_class_index;
		}
		
		if (!empty($teacher_shortcut)) {
			$fields .= ", teacher_shortcut";
			$values .= ", '" . $teacher_shortcut . "'";
		}
		
		if (!is_null($courses)) {
			$fields .= ", courses";
			$values .= ", '{" . $courses . "}'";
		}
		
		if (!empty($show_notifications)) {
			$query .= ", show_notifications = ";
			$query .= $show_notifications;
		}
		
		$query = "INSERT INTO users (" . $fields . ") VALUES (" . $values . ")";
				
		return $query;
	}
	
	
	// MAIN
	if (isset($_POST['token']) && !empty($_POST['token'])) {		
		$db_connection = pg_connect("host=ec2-54-75-227-92.eu-west-1.compute.amazonaws.com dbname=d7i5315uupgr2m user=mydxnmvauddyee password=493c42d4734c867c3fda62972ff79b1df2994deea5af051b3b1a4052e89f0381");
		
		$token = $_POST['token'];
		$old_token = $_POST['old_token'];
		$is_teacher = $_POST['is_teacher'];
		$school_class = $_POST['school_class'];
		$school_class_index = $_POST['school_class_index'];
		$teacher_shortcut = $_POST['teacher_shortcut'];
		$show_notifications = $_POST['show_notifications'];
		
		if (isset($_POST['courses'])) {
			$courses = $_POST['courses'];
		} else {
			$courses = NULL;
		}
		
		$result = pg_query($db_connection, "SELECT token FROM users WHERE token = '" . $old_token . "'");
		$num_rows = pg_num_rows($result);
		
		if (!empty($old_token) && $num_rows >= 1 && !($token === $old_token)) {
			echo 'new token ';
			
			$query = "DELETE FROM users WHERE token = '" . $token . "'";
			$result = pg_query($db_connection, $query);
			
			$query = createUpdateQuery($token, $old_token, $is_teacher, $school_class, $school_class_index, $teacher_shortcut, $courses, $show_notifications);
			$result = pg_query($db_connection, $query);
			
			echo pg_last_error($db_connection);
		} else {
			$result = pg_query($db_connection, "SELECT token FROM users WHERE token = '" . $token . "'");
			$num_rows = pg_num_rows($result);
			
			if ($num_rows >= 1) {
				echo 'update values';
				
				$query = createUpdateQuery($token, $token, $is_teacher, $school_class, $school_class_index, $teacher_shortcut, $courses, $show_notifications);
				$result = pg_query($db_connection, $query);
				
				echo pg_last_error($db_connection);
			} else {
				echo 'insert token';
				
				$query = createInsertQuery($token, $is_teacher, $school_class, $school_class_index, $teacher_shortcut, $courses, $show_notifications);
				$result = pg_query($db_connection, $query);
				
				echo pg_last_error($db_connection);
			}
		}
	}
?>