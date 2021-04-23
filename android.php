<?php
// バッファリング開始
ob_start();
?>
 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
</head>
<body>
<br><br>
<br><br>
<br><br>
<font size="6">
Word = 
<?php
echo $_POST['keyuuid'];

?>
</font>
<br><br><br><br>
</body>
</html>
 
<?php
// 同階層の pass_check.html にphp実行結果を出力
file_put_contents( 'android.html', ob_get_contents() );
 
// 出力用バッファをクリアしてオフ
ob_end_clean();

?>