package org.snaker.modules.flow.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.TaskModel.TaskType;
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
	 * 申请
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "all" ,method=RequestMethod.GET)
	public String all(Model model, String processName, String orderId, String taskId) {
		model.addAttribute("processName", processName);
		if(StringUtils.isNotEmpty(orderId) && StringUtils.isNotEmpty(taskId)) {
			model.addAttribute("orderId", orderId);
			model.addAttribute("taskId", taskId);
			Task task = facets.getEngine().query().getTask(taskId);
			if(task != null && StringUtils.isNotEmpty(task.getActionUrl())) {
				return "redirect:" + task.getActionUrl();
			}
		}
		return "flow/leave/apply";
	}
	/**
	 * 申请
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "apply" ,method=RequestMethod.GET)
	public String apply(Model model, String processName, String orderId) {
		model.addAttribute("processName", processName);
		model.addAttribute("orderId", orderId);
		return "flow/leave/apply";
	}
	
	/**
	 * 申请保存
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "apply/save" ,method=RequestMethod.POST)
	public String applySave(Model model, String processName, String orderId, float day, String reason) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("day", day);
		args.put("reason", reason);
		args.put("apply.operator", ShiroUtils.getUsername());
		args.put("approveDept.operator", ShiroUtils.getUsername());
		args.put("approveBoss.operator", ShiroUtils.getUsername());
		facets.startAndExecute(processName, null, ShiroUtils.getUsername(), args);
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 部门经理审批
	 * @param model
	 * @return 
	 */
	@RequestMapping(value = "approveDept" ,method=RequestMethod.GET)
	public String approveDept(Model model, String orderId, String taskId) {
		model.addAttribute("orderId", orderId);
		model.addAttribute("taskId", taskId);
		List<HistoryTask> tasks = facets.getEngine().query().getHistoryTasks(new QueryFilter().setOrderId(orderId));
		for(HistoryTask history : tasks) {
			model.addAttribute("variable_" + history.getTaskName(), history.getVariableMap());
		}
		return "flow/leave/approveDept";
	}
	
	/**
	 * 部门经理审批保存
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "approveDept/save" ,method=RequestMethod.POST)
	public String approveDeptSave(Model model, String taskId, HttpServletRequest request, float day) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("approveDept.suggest", request.getParameter("approveDept.suggest"));
		args.put("departmentResult", request.getParameter("departmentResult"));
		String departmentResult = request.getParameter("departmentResult");
		if(departmentResult.equals("-1")) {
			facets.executeAndJump(taskId, ShiroUtils.getUsername(), args, null);
		} else if(departmentResult.equals("2")) {
			facets.getEngine().task().createNewTask(taskId, TaskType.Major.ordinal(), request.getParameter("nextOperator"));
			facets.getEngine().task().complete(taskId, ShiroUtils.getUsername());
		} else {
			facets.execute(request.getParameter("taskId"), ShiroUtils.getUsername(), args);
		}
		
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 总经理审批
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "approveBoss" ,method=RequestMethod.GET)
	public String approveBoss(Model model, String orderId, String taskId) {
		model.addAttribute("orderId", orderId);
		model.addAttribute("taskId", taskId);
		List<HistoryTask> tasks = facets.getEngine().query().getHistoryTasks(new QueryFilter().setOrderId(orderId));
		for(HistoryTask history : tasks) {
			model.addAttribute("variable_" + history.getTaskName(), history.getVariableMap());
		}
		return "flow/leave/approveBoss";
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
