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
<title>Ugunduzi - Study</title>
<script>

var farm_versions = [];
var current_version = 0;

function getUserFarms(){
	var u = document.getElementById("u");
	var id = u.options[u.selectedIndex].value; 
	
	if(id!=""){
		
		var plot_grid = document.getElementById("farm_grid");
		plot_grid.style.display="none";
	
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
					
					getFarmPlots(farm_id);
				} else {
					var farm_placeholder = document.getElementById("farm_name");
					farm_placeholder.innerHTML = "";
					
					var farm_chooser = document.getElementById("farm_chooser");
					farm_chooser.style.display="block";
					
					var farm_select = document.getElementById("farm");
					
					while(farm_select.hasChildNodes()){
						farm_select.removeChild(farm_select.firstChild);
					}
					
					var option = document.createElement("option");
					option.setAttribute("value","");
					option.setAttribute("selected",true);
					option.setAttribute("disabled",true);
					option.appendChild(document.createTextNode("Select farm"));
					farm_select.appendChild(option);
					
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

function getFarmPlots(id){
	
	if(id==-1){
		var farm = document.getElementById("farm");
		var id = farm.options[farm.selectedIndex].value;
	}
	
	if(id!=""){
		
	
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_farm_plots.php?id=" + id);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				plots = this.responseText;
				if(plots==""){
					var plot_grid = document.getElementById("farm_grid");
					plot_grid.style.display="none";
				} else {
					farm_versions = plots.split("*");
					current_version = farm_versions.length-1;
					
					drawCurrentFarm();
					
				}
			}
		};
		
	}
	
}

function drawCurrentFarm(){
	
	var this_farm = farm_versions[current_version];
	
	var plot_grid = document.getElementById("farm_grid");
	plot_grid.style.display="grid";
	
	while(plot_grid.hasChildNodes()){
		plot_grid.removeChild(plot_grid.firstChild);
	}
	
	var plots = this_farm.split("|");
	
	for(var i=0;i<plots.length;i++){
		
		var this_plot = plots[i].split(";");
		var this_plot_id = this_plot[0];
		var this_plot_x = this_plot[1];
		var this_plot_y = this_plot[2];
		var this_plot_w = this_plot[3];
		var this_plot_h = this_plot[4];
		var this_plot_size = this_plot[5];
		var this_plot_crops = this_plot[6];
		var this_plot_pest_control = this_plot[7];
		var this_plot_soil_management = this_plot[8];
		
		var div = document.createElement("div");
		div.className="grid-item";
		
		var crops = this_plot_crops.split(",");
		var crop_names="";
		for(var j=0;j<crops.length;j++){
			crop_names = (crop_names == "")? crops[j] : crop_names+"<br>"+crops[j];
		}
		crop_names = '<a href="#" onclick="displayPlotData('+this_plot_id+')">'+crop_names+'</a>';
		div.innerHTML = crop_names;
		
		var bgcolor;
		if(this_plot_pest_control=="" && this_plot_soil_management==""){
			bgcolor="#BBBBBB";
		} else if(this_plot_pest_control!="" && this_plot_soil_management==""){
			bgcolor="#2196F3";
		} else if(this_plot_pest_control=="" && this_plot_soil_management!=""){
			bgcolor="#FFC107";
		} else if(this_plot_pest_control!="" && this_plot_soil_management!=""){
			bgcolor="#4CAF50";
		}
		
		var cssString="grid-area: "+(parseInt(this_plot_y)+1).toString()+" / "+(parseInt(this_plot_x)+1).toString()+" / span "+this_plot_h+" / span "+this_plot_w+"; background-color: "+bgcolor;
		
		div.style.cssText=cssString;
		
		plot_grid.appendChild(div);
	}
	
	
}

</script>
<style>
.main {
	padding: 16px;
	margin-top: 0px;
}

.grid-container {
  display: grid;
  grid-template-columns: 170px 170px 170px 170px;
  grid-template-rows: 100px 100px 100px 100px;
  background-color: #ffffff;
  padding: 5px;
  grid-gap: 2px;
}

.grid-item {
  border: 0px;
  padding: 1px;
  font-size: 14px;
  text-align: center;
}
</style>
</head>
<body class="w3-theme-l4">
<div style="width:48%; max-width:800px; display:block; margin-left:10px; margin-right:auto;">
<div class="main"><p>
<div class="w3-container w3-card-4 w3-white w3-padding-medium w3-text-black">
<strong>Ugunduzi - Study</strong><br><br>
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
	<select class="w3-select w3-text-black w3-border" id="farm" onclick="getFarmPlots(-1);">
	</select>
  </div>
  <span id="farm_name"></span><br><br>
  <div class="grid-container" id="farm_grid" style="display:none; align:center;">
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