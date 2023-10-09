<?
//initialize dbConnection
function initDB() {
	$host="localhost";
	$db="ugunduzi";
	$db_user="xxx";
	$db_pass="xxx";
	$dbh=mysqli_connect($host, $db_user, $db_pass, $db);
	return $dbh;
}

//
?>