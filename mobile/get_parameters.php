<?php
include_once "./../includes/vars.php";

$df = fopen("php://output", 'w');
$row=array($ugunduzi_email,$ugunduzi_pass,$data_subject,$multimedia_subject,$smtp_server,$smtp_server_port);
fputcsv($df, $row);
fclose($df);

?>