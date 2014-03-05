<%--
  Created by IntelliJ IDEA.
  User: csmi686
  Date: 04/03/14
  Time: 15.14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="jsp/inc/cssandjs.jsp"/>
<script type="text/javascript">
    $(document).ready(function () {
        $('body').layout({resizeWhileDragging: true, sizable: false, animatePaneSizing: true, fxSpeed: 'slow', north__size: "20%", center__size: "80%", west__size: "20%", spacing_open: 0, spacing_closed: 0});
        $("#btnDlg").button().click(function(){
            $('#dlg').dialog({autoOpen:false, modal:true});
            $('#dlg').dialog('open');
        });

        var gridDataArray = new Array();
        for(var i = 0; i < 10; i++)
        {
            gridDataArray.push({'row_id':i,
                'affidatario':'Campo' + i +'_a',
                'destinatario':'Campo' + i + '_b'});
        }


        $("#testGrid").jqGrid({
            datatype: "json",
            rowNum: 10000,
            colModel: [
                {name: 'row_id', index: 'row_id', hidden: true},
                {name: 'affidatario', index: 'affidatario', width: 400, label: 'Campo 1'},
                {name: 'destinatario', index: 'destinatario', width: 300, label: 'Campo 2'}
            ],
            sortname:'row_id',
            sortorder: "desc",
            viewrecords:true,
            autowidth:false,
            subGrid:false,
            pager:'#testPager'
        });


        jQuery("#testGrid").jqGrid('navGrid', '#testPager', {edit: false, add: false, del: false, search: false})
                .navButtonAdd('#testPager', {    caption: "Cerca",
                    onClickButton: function () {
                        alert("Ricerca");
                    },
                    buttonicon: "ui-icon-search",
                    title: "Filtro",
                    id:'eventiNBFiltro'
                }).navButtonAdd("#testPager", {
                    caption: "Nuovo",
                    id:'eventiNBNuovo',
                    onClickButton: function () {
                        alert("Nuovo");

                    },
                    buttonicon: 'ui-icon-plus',
                    title: 'Nuovo'
                })
        reloadGridFromLocalArray("testGrid", gridDataArray);


    });
</script>
<body>
<div id="north" class="ui-layout-north" style="overflow: hidden;">
    <table border="0">
        <tr>
            <td valign="middle" align="left" width="20%">&nbsp;</td>
            <td valign="middle" width="60%" style="background-color: red"><label class="title">Java GAL - Console di gestione</label></td>
            <td valign="middle" width="20%"><img src="images/2000px-logo_telecom_italia-svg.png" height="100px"/></td>
        </tr>
    </table>
    </div>
<div id="west" class="ui-layout-west">Menu</div>
<div id="center" class="ui-layout-center">
    <div id="dlg" title="testo">Ciao</div>
    <button id="btnDlg">Clicca</button>
    <table id="testGrid"/>
    <div id="testPager"/>
</div>
</body>
</html>
