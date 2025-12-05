<%@ page language="java" contentType="text/html; charset=utf-8"%>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>NXNS云匹配认别系统</title>
<script src="http://www.jq22.com/jquery/1.11.1/jquery.min.js"></script>
<style type="text/css">
*{
    margin:0;
    padding:0;
}
body{
    background:#000;
}
.content{
    width:1920px;
    height:1080px;
    margin:0 auto;
    position:relative;
}
video{
    margin:0 auto;
    width:1200px;
    height:900px;
    position:absolute;
    left:30px;
    right:0;
    top:50px;
    
}
.ditu{
    position:absolute;
    left:0;
    z-index:2;
}
a.boy{
    width:431px;
    height:272px;
    position:absolute;
    top:541px;
    left:304px;
    z-index:200;
}
a:hover.boy{
    background:url(images/boy.png) no-repeat;
}
    
a.girl{
    width:431px;
    height:272px;
    position:absolute;
    top:552px;
    left:1193px;
    z-index:200;
}
a:hover.girl{
    background:url(images/girl.png) no-repeat;
}
.select-sex{
    width:330px;
    height:70px;
    position:absolute;
    background:#ccc;
    left:810px;
    top:895px;
}
.main-con{
    width:650px;
    height:316px;
    position:absolute;
    z-index:100;
    left:508px;
    top:335px;
    color: #0298B7;
}
.datas{
    position:absolute;
    left:1166px;
    top:357px;
    z-index:101;
}
.left-user img{
    border-radius:50%;
}
.right-user img{
    border-radius:50%;
}
.left-user{
    position:absolute;
    z-index:102;
    left:243px;
    top:420px;
}
.right-user{
    position:absolute;
    z-index:103;
    left:1485px;
    top:422px;
}
.back-btn a{
    position:absolute;
    left:336px;
    top:135px;
    z-index:106;
    width:107px;
    height:70px;
    background:url(images/back.png) no-repeat;
    display:block;
    
}
.back-btn a:hover{
    background:url(images/back_over.png) no-repeat;
}
.p1{
    display: none;
}
.p2{
    display: none;
}
.p3{
    display: none;
}
.p4{
    display: none;
}
.p5{
    display: none;
}
.p6{
    display: none;
}

</style>
<script type="text/javascript">
function changeText(cont1,cont2,speed){
    console.log(cont2);
    var Otext=cont1.text();
    var Ocontent=Otext.split("");
    var i=0;
    function show(){
        if(i<Ocontent.length)
        {        
            cont2.append(Ocontent[i]);
            i=i+1;
        };
    };
    var Otimer =setInterval(show,speed);    
};
$(document).ready(function(){
    changeText($(".p1"),$(".p11"),150);
     setTimeout(function(){
        changeText($(".p2"),$(".p22"),150);
    }, 500);
    
    setTimeout(function(){
        changeText($(".p3"),$(".p33"),150);
    }, 1600);
    
    setTimeout(function(){
        changeText($(".p4"),$(".p44"),150);
    }, 2000);
    
    setTimeout(function(){
        changeText($(".p5"),$(".p55"),100);
    }, 2500);
    
    var time = (("${pdesc}".length*0.1)+2.6)*1000;
    setTimeout(function(){
        $('#video').trigger('pause');
    }, time);
});



</script>
</head>

<body>
<div style="width: 1920px;height: 1000px;position: relative;margin: 0px auto;"></div>
<video src="2.wav"  id="video" autoplay loop>您所用的浏览器不支持改标签</video>
<div class="content">
<div class="back-btn"><a href="main.html"></a></div>
<div class="left-user"><img src="${img}" width="205" height="205" /></div>
<div class="right-user"><img width="205" height="205" src="${ppei}"/></div>
<!-- 使用el表达式获取request中的值 -->
<div class="main-con">
    <p style="font-size: 25px;font-family: Microsoft YaHei;">性别：<span class="p1">${gender}</span><span class="p11"></span></p>
    <p style="font-size: 25px;font-family: Microsoft YaHei;">眼镜：<span class="p2">${eyestatus}</span><span class="p22"></span></p>
    <p style="font-size: 25px;font-family: Microsoft YaHei;">笑容：<span class="p3">${smile}</span><span class="p33"></span></p>
    <p style="font-size: 25px;font-family: Microsoft YaHei;">年龄：<span class="p4">${age}</span><span class="p44"></span></p>
    <p style="font-size: 25px;font-family: Microsoft YaHei;">描述：<span class="p5">${pdesc}</span><span class="p55"></span></p>
</div>

<div class="ditu"><img src="images/third.png"/></div>

<div  style="top:883px;background-color:#000205;height: 80px;width: 500px;left: 870px;position: absolute;color: #02B2EA;font-size: 22px;margin: 0px;z-index: 999;">
    <h1 style="font-size: 70px;margin: 0px;color: #02BAEA;font-family: Microsoft YaHei;">匹配成功</h1>
</div>
</div>
<%
    response.setHeader("refresh", "60;url=main.html");
%>
</body>
</html>
