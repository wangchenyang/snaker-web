package org.snaker.modules.flow.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.snaker.framework.security.shiro.ShiroUtils;
import org.snaker.modules.base.service.SnakerEngineFacets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 请假流程Controller
 * @author yuqs
 * @since 0.1
 */
@Controller
@RequestMapping(value = "/flow/leave")
public class LeaveController {
	@Autowired
	private SnakerEngineFacets facets;
	
	/**
	 * 申请保存
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "apply/save" ,method=RequestMethod.POST)
	public String applySave(Model model, String processId, String orderId, float day, String reason) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("day", day);
		args.put("reason", reason);
		args.put("apply.operator", ShiroUtils.getUsername());
		args.put("approveDept.operator", ShiroUtils.getUsername());
		args.put("approveBoss.operator", ShiroUtils.getUsername());
		facets.startAndExecute(processId, ShiroUtils.getUsername(), args);
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 部门经理审批保存
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "approveDept/save" ,method=RequestMethod.POST)
	public String approveDeptSave(Model model, String taskId, HttpServletRequest request) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("approveDept.suggest", request.getParameter("approveDept.suggest"));
		args.put("departmentResult", request.getParameter("departmentResult"));
		String departmentResult = request.getParameter("departmentResult");
		if(departmentResult.equals("-1")) {
			facets.executeAndJump(taskId, ShiroUtils.getUsername(), args, null);
		} else if(departmentResult.equals("2")) {
			facets.transfer(taskId, ShiroUtils.getUsername(), request.getParameter("nextOperator"));
		} else {
			facets.execute(request.getParameter("taskId"), ShiroUtils.getUsername(), args);
		}
		
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 总经理审批保存
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "approveBoss/save" ,method=RequestMethod.POST)
	public String approveBossSave(Model model, HttpServletRequest request) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("approveBoss.suggest", request.getParameter("approveBoss.suggest"));
		facets.execute(request.getParameter("taskId"), ShiroUtils.getUsername(), args);
		return "redirect:/snaker/task/active";
	}
}
