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
var this_farm_id = -1;
var this_data = [];
var data_index = 0;
var total_data = 0;

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
					farm_placeholder.innerHTML = farm_name;
					
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
						option.appendChild(document.createTextNode(farm_name));
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
		var this_farm_date = this_plot[9];
		var this_farm_size = this_plot[10];
		this_farm_id = this_plot[11];
		
		var div = document.createElement("div");
		div.className="grid-item";
		
		var crops = this_plot_crops.split(",");
		var crop_names="";
		for(var j=0;j<crops.length;j++){
			crop_names = (crop_names == "")? crops[j] : crop_names+"<br>"+crops[j];
		}
		crop_names = (crop_names=="")? "Empty" : crop_names;
		crop_names = '<a href="#" onclick="getPlotData('+this_plot_id+','+data_index+')">'+crop_names+'</a>';
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
	
	var navigation = document.getElementById("navigation");
	
	if(farm_versions.length>1){
		navigation.style.display="block";
		
		var prev = document.getElementById("prev");
		var next = document.getElementById("next");
		
		prev.style.display = (current_version>0)? "inline" : "none";
		next.style.display = (current_version<(farm_versions.length-1))? "inline" : "none";
		
	} else {
		navigation.style.display="none";
	}
	
	var farm_date = document.getElementById("farm_date");
	farm_date.style.display = "inline";
	farm_date.innerHTML = '<strong>Farm created on:</strong> '+this_farm_date+', <strong>Size:</strong> '+this_farm_size+' acres. <a href="#" onclick="getFarmData('+this_farm_id+','+data_index+');">See all farm data</a>';
	
}

function goToPrevFarm(){
	current_version--;
	drawCurrentFarm();
}

function goToNextFarm(){
	current_version++;
	drawCurrentFarm();
}

function getFarmData(id,index){
	if(id>0){
		
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_farm_data.php?id=" + id + "&from=" + index);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				var response_data = this.responseText;
				var data_container = document.getElementById("data_container");
				if(response_data==""){
					data_container.innerHTML="No data";
				} else {
					this_data = response_data.split("*");
					total_data = parseInt(this_data[0]);
					var msg="<strong>Entire farm</strong><br><hr>";
					displayData(msg);
					
				}
			}
		};
	}
}

function getPlotData(id,index){
	if(id>0){
		
		var xmlhttp = new XMLHttpRequest();

		xmlhttp.open("GET", "includes/get_plot_data.php?id=" + id + "&from=" + index);
		xmlhttp.send();
		xmlhttp.onreadystatechange = function() {
			if (this.readyState === 4 && this.status === 200) {
				var response_data = this.responseText;
				if(response_data==""){
					var data_container = document.getElementById("data_container");
					data_container.innerHTML="No data";
				} else {
					this_data = response_data.split("*");
					total_data = parseInt(this_data[0]);
					//TODO: display plot info
					displayData("");
					
				}
			}
		};
	}
}

function displayData(msg){
	var data_container = document.getElementById("data_container");
	
	data_html="";
	
	for(var i=1;i<this_data.length;i++){
		this_data_parts=this_data[i].split(";");
		if(this_data_parts.length==4){
			this_data_html="Date: "+this_data_parts[0]+"<br>"+this_data_parts[1]+"<br><br>"+this_data_parts[2]+"<br>";
			this_data_html=(this_data_parts[3]=="")? this_data_html : this_data_html+"Comments: "+data_parts[3]+"<br>";
			data_html=(data_html=="")? this_data_html : data_html+"<hr>"+this_data_html;
		} else {
			//TODO: image + snd
		}
	}
	data_container.innerHTML=msg+data_html;
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
 <select class="w3-select w3-text-black w3-border" id="u" onchange="getUserFarms();">
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
	<select class="w3-select w3-text-black w3-border" id="farm" onchange="getFarmPlots(-1);">
	</select>
  </div>
  <span id="farm_name"></span><br><br>
  <div class="grid-container" id="farm_grid" style="display:none; align:center;">
  </div><br>
  <div id="navigation" style="display:none;">
  <span id="prev" style="display:none;"><a href="#" onclick="goToPrevFarm();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button"><<</a></span>
  &nbsp;
  <span id="next" style="display:none;"><a href="#" onclick="goToNextFarm();" style="text-decoration:none;" class="w3-theme-d2 w3-hover-theme w3-button">>></a></span>
  </div>
  <span id="farm_date" style="display:none; align:right;"></span>
</div> 
</p></div>
</div>
<div class="w3-container w3-card-4 w3-white w3-padding-medium w3-text-black" style="width:48%; max-width:800px; display:inline; position:fixed; left:50%; top:32px; margin-right:10px; height:700px; overflow:auto;" id="data_container">Choose a plot or entire farm to see data</div>
</div>
</body>
</html>

<?php
	}
}
?>