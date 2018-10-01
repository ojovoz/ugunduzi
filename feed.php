<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
include_once "includes/vars.php";
$dbh = initDB();

if(isset($_SESSION['user_id']) && isset($_SESSION['user_alias']) && isset($_SESSION['mode'])){
	checkRecords($dbh,$ugunduzi_email,$ugunduzi_pass,$data_subject,$multimedia_subject,$mail_server,$servpath,$root_folder,$ffmpeg_path,$sample_rate);
	if(isset($_GET['from'])){
		$from=$_GET['from'];
	} else {
		$from=0;
	}
?>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/w3.css">
<link rel="stylesheet" href="css/green_theme.css">
<script src="includes/audio.min.js"></script>
<script>
  audiojs.events.ready(function() {
    var as = audiojs.createAll();
  });
</script>
<style>
.navbar {
  overflow: hidden;
  position: fixed;
  top: 0;
}
  
.navbar a {
  float: left;
  display: block;
  text-align: center;
  padding: 14px 16px;
  text-decoration: none;
  font-size: 17px;
}

.main {
	padding: 16px;
	margin-top: 30px;
}

.audiojs .scrubber {
    background: none repeat scroll 0 0 #5A5A5A;
    border-bottom: 0 none;
    border-left: 0 none;
    border-top: 1px solid #3F3F3F;
    float: left;
    height: 14px;
    margin: 10px;
    overflow: hidden;
    position: relative;
    width: 50%; /* smaller width */
}

.audiojs .time {
    border-left: 1px solid #000000;
    color: #DDDDDD;
    float: left;
    height: 36px;
    line-height: 36px;
    margin: 0; /* no margin */
    padding: 0 6px 0 9px; /* 2px smaller left padding */
    text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.5);
}

.audiojs {
    font-family: monospace;
    font-size: 10px; /* reduced font size */
	width:100%; 
	max-width:<?php echo($audio_width); ?>px; 
	background: #15420b; 
}


</style>
<title>Ugunduzi</title>
</head>
<body>
<div style="width:100%; max-width:800px; dislay:block; margin-left:auto; margin-right:auto;">
<div class="navbar w3-theme-d4" style="width:100%; max-width:800px;">
  <a class="w3-theme-d4 w3-hover-theme" href="#"><?php echo(ucfirst($_SESSION['user_alias'])); ?></a>
  <a class="w3-theme-d4 w3-hover-theme" href="#">Everybody</a>
</div>
<div class="main"><p>
<?php
	switch($_SESSION['mode']){
		case 0:
			$query="SELECT log.plot_id, log.log_date, farm.user_id, log.log_picture, log.log_sound FROM log, plot, farm WHERE log_picture<>'' AND log_sound<>'' AND plot.plot_id = log.plot_id AND farm.farm_id = plot.farm_id ORDER BY log_date DESC LIMIT $from, $max_images_per_page";
			$result = mysqli_query($dbh,$query);
			while($row=mysqli_fetch_array($result,MYSQL_NUM)){
				$user_id=$row[2];
				$user_alias=getUserAliasFromID($dbh,$user_id);
				if($user_alias!=""){
					$message_data=ucfirst($user_alias).": ".$row[1];
				} else {
					$message_data=$row[1];
				}
				$plot_data=getPlotData($dbh,$row[0],$none_word,$plot_word,$pest_control_word,$soil_management_word);
				if($plot_data!=""){
					$message_header=$message_data."<br>".$plot_data."<br>";
				} else {
					$message_header=$message_data."<br>";
				}
				$image_source="./content".$row[3];
				$audio_source="./content".$row[4];
				?>
				<p><div class="w3-container w3-card-4">
				<div class="w3-text-theme"><?php echo($message_header); ?></div>
				<img style="width:100%; max-width:700px;" src="<?php echo($image_source); ?>"><br>
				<audio src="<? echo($audio_source); ?>" preload="none"></audio><br>
				</div></p>
				<?php
			}
			break;
		case 1:
			break;
	}
?>
</p>
</div>
</div>
</body>
<?php
} else {
	header("Location: index.php");
}
?>