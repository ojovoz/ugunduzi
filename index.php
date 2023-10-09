<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
$dbh = initDB();

$success=false;
$user_alias="";

$_SESSION['user_filter']=array();
$_SESSION['farm_filter']=array();
$_SESSION['crop_filter']=array();
$_SESSION['ingredient_filter']=array();

if($_SERVER["REQUEST_METHOD"] == "POST") {
	if(isset($_POST['login'])){
		$user_alias=$_POST['user_alias'];
		$user_password=$_POST['user_password'];
		$id=getUserIDFromAliasPass($dbh,$user_alias,$user_password);
		if($id!=-1){
			$success=true;
			$_SESSION['user_alias']=$user_alias;
			$_SESSION['user_id']=$id;
			$_SESSION['mode']=1; //mode: 0=everybody's images, 1=my data
			$_SESSION['admin']=userIsAdmin($dbh,$id);
			header("Location: feed.php");
		}
	}
}

if(!$success){
?>

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/w3.css">
<link rel="stylesheet" href="css/green_theme.css">
<title>Ugunduzi</title>
</head>
<body>
<div class="w3-container w3-display-top" style="width:80%; max-width:500px; dislay:block; margin-left:auto; margin-right:auto;">
<h2 class="w3-theme-d4">Ugunduzi - Login</h2>
<form method="post" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]);?>">
<p>      
<label class="w3-text-theme">Your name:</label>
<input class="w3-input w3-theme-l1 w3-border-0" name="user_alias" type="text" maxlength="20"></p>
<p>      
<label class="w3-text-theme">Your password:</label>
<input class="w3-input w3-theme-l1 w3-border-0" name="user_password" type="password" maxlength="30"></p><br>
<div align="center"><button class="w3-button w3-padding-large w3-theme-d4 w3-round w3-border-0" id="login" name="login">Log in</button><br><br>
<a class="w3-text-theme" href="feed.php?guest=1">Guest login</a><br><br>
<a class="w3-text-theme" href="download/index.html">Download app & manuals</a><br><br>
</div></form><br>
<?php
if(!$success && ($user_alias!="")){
?>
<label class="w3-text-deep-orange"><strong>Incorrect login</strong></label>
<?php
}
?>
</div>
</body>
</html>
<?php
}
?>