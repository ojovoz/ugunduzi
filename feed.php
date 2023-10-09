<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
include_once "includes/vars.php";
$dbh = initDB();

if(isset($_GET['guest'])){
	$_SESSION['user_id']=-1;
	$_SESSION['user_alias']="";
	$_SESSION['mode']=0;
	$_SESSION['admin']=false;
}

if(isset($_SESSION['user_id']) && isset($_SESSION['user_alias']) && isset($_SESSION['mode'])){
	checkRecords($dbh,$ugunduzi_email,$ugunduzi_pass,$data_subject,$multimedia_subject,$mail_server,$servpath,$root_folder,$ffmpeg_path,$sample_rate);
	if(isset($_GET['from'])){
		$from=$_GET['from'];
	} else {
		$from=0;
	}
	
	if(isset($_GET['mode'])){
		$_SESSION['mode']=$_GET['mode'];
		$_SESSION['user_filter']=array();
		$_SESSION['farm_filter']=array();
		
		$_SESSION['crop_filter']=array();
		$_SESSION['ingredient_filter']=array();
		
	} else {
		if(isset($_GET['user'])){
			$user_filter_id=$_GET['user'];
			if($user_filter_id>=0){
				if(!in_array($user_filter_id,$_SESSION['user_filter'])){
					array_push($_SESSION['user_filter'],$user_filter_id);
				}
			} else {
				$user_filter_id*=-1;
				$index=array_search($user_filter_id,$_SESSION['user_filter']);
				if($index>=0){
					array_splice($_SESSION['user_filter'],$index,1);
				}
			}
		}

		if(isset($_GET['farm'])){
			$farm_filter_id=$_GET['farm'];
			if($farm_filter_id>=0){
				if(!in_array($farm_filter_id,$_SESSION['farm_filter'])){
					array_push($_SESSION['farm_filter'],$farm_filter_id);
				}
			} else {
				$farm_filter_id*=-1;
				$index=array_search($farm_filter_id,$_SESSION['farm_filter']);
				if($index>=0){
					array_splice($_SESSION['farm_filter'],$index,1);
				}
			}
		}

		if(isset($_GET['crop'])){
			$crop_filter_id=$_GET['crop'];
			if($crop_filter_id>=0){
				if(!in_array($crop_filter_id,$_SESSION['crop_filter'])){
					array_push($_SESSION['crop_filter'],$crop_filter_id);
				}
			} else {
				$crop_filter_id*=-1;
				$index=array_search($crop_filter_id,$_SESSION['crop_filter']);
				if($index>=0){
					array_splice($_SESSION['crop_filter'],$index,1);
				}
			}
		}

		if(isset($_GET['ingredient'])){
			$ingredient_filter_id=$_GET['ingredient'];
			if($ingredient_filter_id>=0){
				if(!in_array($ingredient_filter_id,$_SESSION['ingredient_filter'])){
					array_push($_SESSION['ingredient_filter'],$ingredient_filter_id);
				}
			} else {
				$ingredient_filter_id*=-1;
				$index=array_search($ingredient_filter_id,$_SESSION['ingredient_filter']);
				if($index>=0){
					array_splice($_SESSION['ingredient_filter'],$index,1);
				}
			}
		}
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
  
  function deleteContent(id){
	  if(confirm("Je kweli unataka kufuta kitu ulichokichagua?")){
		  location.href="delete.php?id=" + id;
	  }
  }
</script>
<style>
.navbar {
  overflow: hidden;
  position: fixed;
  top: 0;
  z-index: 100;
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
	max-width:768px; 
	background: #15420b; 
}


</style>
<title>Ugunduzi</title>
</head>
<body class="w3-theme-l4">
<div style="width:100%; max-width:800px; dislay:block; margin-left:auto; margin-right:auto;">
<div class="navbar w3-theme-d4" style="width:100%; max-width:800px;">
  <?php if($_SESSION['user_id']>=0) { ?><a class="<?php echo($_SESSION['mode']==0 ? "w3-theme-d4" : "w3-theme-d2");?> w3-hover-theme" href="feed.php?mode=1"><?php echo(ucfirst($_SESSION['user_alias'])); ?></a><?php } ?>
  <a class="<?php echo($_SESSION['mode']==1 ? "w3-theme-d4" : "w3-theme-d2");?> w3-hover-theme" href="feed.php?mode=0">Kila mtu</a>
  <?php if($_SESSION['admin']) { ?><a class="w3-theme-d4 w3-hover-theme" href="study.php" target="_blank">Study</a> <?php } ?>
</div>
<div class="main"><p>
<?php	
if(!empty($_SESSION['user_filter']) || !empty($_SESSION['farm_filter']) || !empty($_SESSION['crop_filter']) || !empty($_SESSION['ingredient_filter'])){
	$user_filter_names = (!empty($_SESSION['user_filter']) ? getUserNames($dbh,$_SESSION['user_filter'],"feed.php") : "");
	$farm_filter_names = (!empty($_SESSION['farm_filter']) ? getFarmNames($dbh,$_SESSION['farm_filter'],"feed.php") : "");
	$crop_filter_names = (!empty($_SESSION['crop_filter']) ? getCropNames($dbh,$_SESSION['crop_filter'],"feed.php") : "");
	$ingredient_filter_names = (!empty($_SESSION['ingredient_filter']) ? getIngredientNames($dbh,$_SESSION['ingredient_filter'],"feed.php") : "");
	?>
	<p><div class="w3-container w3-card-4 w3-white w3-padding-small w3-text-black">
	Tafuta: <?php echo($user_filter_names." ".$farm_filter_names." ".$crop_filter_names." ".$ingredient_filter_names); ?>
	</div></p>
	<?php
}
$user_filter = ($_SESSION['mode']==0 ? (!empty($_SESSION['user_filter']) ? " AND farm.user_id IN(".implode(",",$_SESSION['user_filter']).") " : " ") : " AND farm.user_id = ".$_SESSION['user_id']." ");
$farm_filter = ($_SESSION['mode']==0 ? " " : (!empty($_SESSION['farm_filter']) ? " AND farm.farm_id IN(".getAllFarmIDS($dbh,$_SESSION['farm_filter'],$_SESSION['user_id']).") " : " "));
$plot_filter = ((!empty($_SESSION['crop_filter']) || !empty($_SESSION['ingredient_filter'])) ? " AND plot.plot_id IN(".getPlotsWithCropIngredient($dbh,$_SESSION['crop_filter'],$_SESSION['ingredient_filter']).") " : " ");
$image_filter = ($_SESSION['mode']==0 ? " AND log.log_picture<>'' AND log.log_sound<>'' " : " ");
$query_limit = " LIMIT $from, $max_items_per_page";
if($plot_filter==" "){
	$query_all="(SELECT log.plot_id, log.log_date, farm.user_id, log.log_data_item_id, log.log_quantity, log.log_value, log.log_units_id, log.log_crop_id, log.log_treatment_id, log.log_comments, log.log_picture, log.log_sound, farm.farm_id, log.log_id FROM log, plot, farm WHERE log.plot_id>=0 AND plot.plot_id = log.plot_id AND farm.farm_id = plot.farm_id".$plot_filter.$farm_filter.$user_filter.$image_filter.") UNION (SELECT log.plot_id, log.log_date, farm.user_id, log.log_data_item_id, log.log_quantity, log.log_value, log.log_units_id, log.log_crop_id, log.log_treatment_id, log.log_comments, log.log_picture, log.log_sound, farm.farm_id, log.log_id FROM log, plot, farm WHERE log.plot_id<0 AND farm.farm_id = (log.plot_id*-1)".$plot_filter.$farm_filter.$user_filter.$image_filter.") ORDER BY log_date DESC";
} else {
	$query_all="SELECT log.plot_id, log.log_date, farm.user_id, log.log_data_item_id, log.log_quantity, log.log_value, log.log_units_id, log.log_crop_id, log.log_treatment_id, log.log_comments, log.log_picture, log.log_sound, farm.farm_id, log.log_id FROM log, plot, farm WHERE log.plot_id>=0 AND plot.plot_id = log.plot_id AND farm.farm_id = plot.farm_id".$plot_filter.$farm_filter.$user_filter.$image_filter." ORDER BY log_date DESC"; 
}
$query=$query_all.$query_limit;
$result = mysqli_query($dbh,$query);
while($row=mysqli_fetch_array($result,MYSQL_NUM)){
	$user_id=$row[2];
	$user_alias='<a href="feed.php?user='.$user_id.'">'.getUserNameFromID($dbh,$user_id).'</a>';
	if($row[0]>=0){
		$plot_data=getPlotData($dbh,$row[0],$none_word,$plot_word,$pest_control_word,$soil_management_word,"feed.php");
	} else {
		$plot_data=getFarmNameFromID($dbh,$row[12]);
	}
	switch($_SESSION['mode']){
		case 0:
			$message_data = $user_alias.": ".$row[1];
			$message_header = ($plot_data!="" ? $message_data."<br>".$plot_data."<br>" : $message_data."<br>");
			$image_source="./content".$row[10];
			$audio_source="./content".$row[11];
			?>
			<p><div class="w3-container w3-card-4 w3-white"><br>
			<div class="w3-text-black"><?php echo($message_header); ?></div>
			<img style="width:100%; max-width:768px;" src="<?php echo($image_source); ?>"><br>
			<audio src="<? echo($audio_source); ?>" preload="none"></audio><br>
			</div></p>
			<?php
			break;
		case 1:
			$farm_name='<a href="feed.php?farm='.$row[12].'">'.ucfirst(getFarmNameFromID($dbh,$row[12])).'</a>';
			$message_data = $farm_name.": ".$row[1];
			$message_header = ($plot_data!="" ? $message_data."<br>".$plot_data."<br>" : $message_data."<br>");
			?>
			<p><div class="w3-container w3-card-4 w3-white"><br>
			<div class="w3-text-black"><?php echo($message_header); ?></div>
			<?php
			if($row[10]!="" && $row[11]!=""){
				$image_source="./content".$row[10];
				$audio_source="./content".$row[11];
				?>
				<img style="width:100%; max-width:768px;" src="<?php echo($image_source); ?>"><br>
				<audio src="<? echo($audio_source); ?>" preload="none"></audio>
				<a class="w3-red" href="#" onclick="deleteContent(<?php echo($row[13]); ?>);">Delete</a><br><br>
				</div></p>
				<?php
			} else {
				$message_content = getLogDataItemText($dbh,$row[3],$row[4],$row[5],$row[6],$row[7],$row[8],$row[9]);
				?>
				<br><div class="w3-text-black"><?php echo($message_content); ?></div><br>
				</div></p>
				<?php
			}
			break;
	}		
}
$total_items=getTotalItems($dbh,$query_all);
if($from>0 || $total_items>($from+$max_items_per_page)){
	?>
	<p>
	<div class="w3-bar w3-xlarge"> 
	<?php
	if($from>0){
		?>
		<a href="feed.php?from=<?php echo($from-$max_items_per_page) ?>" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button"><< Uliopita</a>
		<?php
	}
	if($total_items>($from+$max_items_per_page)){
		?>
		<a href="feed.php?from=<?php echo($from+$max_items_per_page) ?>" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button">Ijayo >></a>
		<?php
	}
	?>
	</div></p>
	<?php
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