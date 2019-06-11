<?php
header("Cache-Control: no-cache, must-revalidate");
session_start();
include_once "includes/init_database.php";
include_once "includes/functions.php";
include_once "includes/vars.php";
$dbh = initDB();

if(isset($_SESSION['admin'])){
	if($_SESSION['admin']){

	

?>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="css/w3.css">
<link rel="stylesheet" href="css/green_theme.css">
<title>Ugunduzi - Admin</title>
<script>

function getUserFarms(){
	var u = document.getElementById("u");
	var id = u.options[u.selectedIndex].value; 
	
	if(id!=""){
	
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_user_farms.php?id=" + id);
		xmlhttp.send();
	
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				var farms = this.responseText;
				var farms_array = farms.split(";");
				if(farms_array.length==0 || farms==""){
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "User has no farms";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="none";
				} else if(farms_array.length==1){
					var this_farm = farms_array[0];
					var this_farm_parts = this_farm.split(",");
					var farm_id = this_farm_parts[0];
					var farm_name = this_farm_parts[1];
					var farm_size = this_farm_parts[2];
				
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "<strong>Farm name:</strong> " + farm_name + ", <strong>Size:</strong> " + farm_size + " acres";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="none";
				} else {
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="block";
					
					var farm_select = document.getElementById("farm");
					
					while(farm_select.hasChildNodes()){
						farm_select.removeChild(farm_select.firstChild);
					}
					
					for(var i=0;i<farms_array.length;i++){
						var this_farm_parts = farms_array[i].split(",");
						var farm_id = this_farm_parts[0];
						var farm_name = this_farm_parts[1];
						var farm_size = this_farm_parts[2];
						
						var option = document.createElement("option");
						option.setAttribute("value",farm_id);
						option.appendChild(document.createTextNode("Farm name: " + farm_name + ", Size: " + farm_size + " acres"));
						farm_select.appendChild(option);
					}
				}
			}
		};
	}
}

</script>
<style>
.main {
	padding: 16px;
	margin-top: 30px;
}

.grid-container {
  display: grid;
  grid-template-columns: auto auto auto auto;
  background-color: #ffffff;
  padding: 1px;
}

.grid-item {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 1px;
  font-size: 14px;
  text-align: center;
  height: 100px;
}
</style>
</head>
<body class="w3-theme-l4">
<div style="width:48%; max-width:800px; display:block; margin-left:10px; margin-right:auto;">
<div class="main"><p>
<div class="w3-container w3-card-4 w3-white w3-padding-medium w3-text-black">
 <select class="w3-select w3-text-black w3-border" id="u" onclick="getUserFarms();">
    <option value="" disabled selected>Choose a user</option>
	<?php
	$query="SELECT user_name, user_id FROM user WHERE user_name<>'' ORDER BY user_name";
	$result = mysqli_query($dbh,$query);
	while($row=mysqli_fetch_array($result,MYSQL_NUM)){
		echo('<option value="'.$row[1].'">'.$row[0].'</option>');
	}
	?>
  </select>
  <br><br>
  <div style="display:none;" id="farm_chooser">
	<select class="w3-select w3-text-black w3-border" id="farm">
	</select>
  </div>
  <span id="farm_name"></span>
  <div class="grid-container" id="farm_grid" style="display:none;">
  <div class="grid-item">1</div>
  <div class="grid-item">2</div>
  <div class="grid-item">3</div>
  <div class="grid-item">4</div>
  <div class="grid-item">1</div>
  <div class="grid-item">2</div>
  <div class="grid-item">3</div>
  <div class="grid-item">4</div>
  <div class="grid-item">1</div>
  <div class="grid-item">2</div>
  <div class="grid-item">3</div>
  <div class="grid-item">4</div>
  <div class="grid-item">1</div>
  <div class="grid-item">2</div>
  <div class="grid-item">3</div>
  <div class="grid-item">4</div>
  </div><br>
</div> 
</p></div>
</div>
</body>
</html>

<?php
	}
}
?>