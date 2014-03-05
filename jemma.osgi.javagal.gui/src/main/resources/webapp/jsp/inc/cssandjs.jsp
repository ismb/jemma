<html>
<head>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Java GAL - Console di Gestione</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui-1.10.4.custom.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/searchFilter.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ui.jqgrid.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ui.multiselect.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/layout-default-latest.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/apprise.css">
</head>
<body>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/grid.addons.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/grid.postext.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/grid.setcolumns.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.10.2.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-ui-1.10.4.custom.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/ui.multiselect.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.jqGrid.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.searchFilter.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.tablednd.js"></script>
<script>
    var arrowimages={down:['downarrowclass', '${pageContext.request.contextPath}/css/images/arrow-down.gif', 25], right:['rightarrowclass', '${pageContext.request.contextPath}//css/images/arrow-right.gif']}
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/i18n/grid.locale-it.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.layout-latest.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.blockUI.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/apprise-1.5.full.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/common.js"></script>
<script type="text/javascript">
    //$.datepicker.setDefaults($.datepicker.regional[ "it" ]);
    $.datepicker.setDefaults($.datepicker.regional[ "it" ]);
    $.datepicker.setDefaults({
        changeMonth: true,
        changeYear: true,
        dateFormat: 'dd/mm/yy',
        yearRange:"c-100:c+10"
    });
    window.alert = function(msg, title, callback)
    {
        //jAlert(msg, title, callback);
        apprise(msg,{},callback);

    }

    window.confirm = function(msg, title, callback)
    {
        //jConfirm(msg, title, callback);
        apprise(msg, {'verify':true, 'textYes':'Si', 'textNo':'No'},callback);
    }

    window.prompt = function(message, value, title, callback)
    {
        //jPrompt(message, value, title, callback);
        apprise(msg, {input:true}, callback);
    }

    var confirmV2 = function(msg, callback)
    {
        apprise(msg,{'newVerify':true, 'textYes':'Si', 'textNo':'No','textCancel':'Annulla'},callback);
    }

</script>