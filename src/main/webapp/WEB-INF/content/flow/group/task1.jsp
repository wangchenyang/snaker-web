<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>

<html>
	<head>
		<title>组参与演示</title>
		<%@ include file="/common/meta.jsp"%>
		<link rel="stylesheet" href="${ctx}/styles/css/style.css" type="text/css" media="all" />
		<script src="${ctx}/styles/js/jquery-1.8.3.min.js" type="text/javascript"></script>
		<script src="${ctx}/styles/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
	</head>
	<body>
		<form id="inputForm" action="${ctx }/flow/group/task1" method="post">
			<input type="hidden" name="processName" value="${processName }"/>
			<input type="hidden" name="orderId" value="${orderId }"/>
			<input type="hidden" name="taskId" value="${taskId }"/>
			<table width="100%" border="0" align="center" cellpadding="0"
					class="table_all_border" cellspacing="0" style="margin-bottom: 0px;border-bottom: 0px">
				<tr>
					<td class="td_table_top" align="center">
						组参与演示
					</td>
				</tr>
			</table>
			<table class="table_all" align="center" border="0" cellpadding="0"
				cellspacing="0" style="margin-top: 0px">
				<tr>
					<td class="td_table_1">
						<span>选择task2参与者：</span>
					</td>
					<td class="td_table_2" colspan="3">
						<input type="radio" class="input_radio" name="group" value="ceshi1" />测试1(个人)
						<input type="radio" class="input_radio" name="group" value="6" />需求分析组
						<input type="radio" class="input_radio" name="group" value="7" />测试组
					</td>
				</tr>
			</table>
			<table align="center" border="0" cellpadding="0"
				cellspacing="0">
				<tr align="left">
					<td colspan="1">
						<input type="submit" class="button_70px" name="submit" value="提交">
						&nbsp;&nbsp;
						<input type="button" class="button_70px" name="reback" value="返回"
							onclick="history.back()">
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
