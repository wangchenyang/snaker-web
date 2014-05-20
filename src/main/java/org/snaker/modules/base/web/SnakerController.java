package org.snaker.modules.base.web;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel.TaskType;
import org.snaker.engine.model.WorkModel;
import org.snaker.framework.security.shiro.ShiroUtils;
import org.snaker.modules.base.service.SnakerEngineFacets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Snaker流程引擎常用Controller
 * @author yuqs
 * @since 0.1
 */
@Controller
@RequestMapping(value = "/snaker")
public class SnakerController {
	private static final Logger log = LoggerFactory.getLogger(SnakerController.class);
	@Autowired
	private SnakerEngineFacets facets;
	
	@RequestMapping(value = "task/active", method=RequestMethod.GET)
	public String homeTaskList(Model model) {
		List<String> list = ShiroUtils.getGroups();
		list.add(ShiroUtils.getUsername());
		log.info(list.toString());
		String[] assignees = new String[list.size()];
		list.toArray(assignees);
		
		Page<WorkItem> majorPage = new Page<WorkItem>(5);
		Page<WorkItem> aidantPage = new Page<WorkItem>(5);
		List<WorkItem> majorWorks = facets.getEngine()
				.query()
				.getWorkItems(majorPage, new QueryFilter()
				.setOperators(assignees)
				.setTaskType(TaskType.Major.ordinal()));
		List<WorkItem> aidantWorks = facets.getEngine()
				.query()
				.getWorkItems(aidantPage, new QueryFilter()
				.setOperators(assignees)
				.setTaskType(TaskType.Aidant.ordinal()));
		model.addAttribute("majorWorks", majorWorks);
		model.addAttribute("majorTotal", majorPage.getTotalCount());
		model.addAttribute("aidantWorks", aidantWorks);
		model.addAttribute("aidantTotal", aidantPage.getTotalCount());
		return "snaker/activeTask";
	}
	
	/**
	 * 根据当前用户查询待办任务列表
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/user", method=RequestMethod.GET)
	public String userTaskList(Model model, Page<WorkItem> page) {
		facets.getEngine().query().getWorkItems(page, 
				new QueryFilter().setOperator(ShiroUtils.getUsername()));
		model.addAttribute("page", page);
		return "snaker/userTask";
	}
	
	/**
	 * 活动任务查询列表
	 * @param model
	 * @return
	 */
	//@RequestMapping(value = "task/active", method=RequestMethod.GET)
	public String activeTaskList(Model model, Page<WorkItem> page, String error) {
		List<String> list = ShiroUtils.getGroups();
		list.add(ShiroUtils.getUsername());
		log.info(list.toString());
		String[] assignees = new String[list.size()];
		list.toArray(assignees);
		facets.getEngine().query().getWorkItems(page, 
				new QueryFilter().setOperators(assignees));
		model.addAttribute("page", page);
		model.addAttribute("error", error);
		return "snaker/activeTask";
	}
	
	/**
	 * 测试任务的执行
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/exec", method=RequestMethod.GET)
	public String activeTaskExec(Model model, String taskId) {
		facets.execute(taskId, ShiroUtils.getUsername(), null);
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 活动任务的驳回
	 * @param model
	 * @param taskId
	 * @return
	 */
	@RequestMapping(value = "task/reject", method=RequestMethod.GET)
	public String activeTaskReject(Model model, String taskId) {
		String error = "";
		try {
			facets.executeAndJump(taskId, ShiroUtils.getUsername(), null, null);
		} catch(Exception e) {
			error = "?error=1";
		}
		return "redirect:/snaker/task/active" + error;
	}
	
	/**
	 * 历史完成任务查询列表
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/history", method=RequestMethod.GET)
	public String historyTaskList(Model model, Page<WorkItem> page) {
		facets.getEngine().query().getHistoryWorkItems(page, 
				new QueryFilter().setOperator(ShiroUtils.getUsername()));
		model.addAttribute("page", page);
		return "snaker/historyTask";
	}
	
	/**
	 * 历史任务撤回
	 * @param taskId
	 * @return
	 */
	@RequestMapping(value = "task/undo", method=RequestMethod.GET)
	public String historyTaskUndo(Model model, String taskId) {
		String returnMessage = "";
		try {
			facets.getEngine().task().withdrawTask(taskId, ShiroUtils.getUsername());
			returnMessage = "任务撤回成功.";
		} catch(Exception e) {
			returnMessage = e.getMessage();
		}
		model.addAttribute("returnMessage", returnMessage);
		return "redirect:/snaker/task/history";
	}
	
	/**
	 * 流程实例查询
	 * @param model
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "order", method=RequestMethod.GET)
	public String order(Model model, Page<HistoryOrder> page) {
		facets.getEngine().query().getHistoryOrders(page, new QueryFilter());
		model.addAttribute("page", page);
		return "snaker/order";
	}
	
	/**
	 * 流程实例all视图
	 * @param model
	 * @param processName
	 * @param orderId
	 * @param taskId
	 * @return
	 */
	@RequestMapping(value = "all", method=RequestMethod.GET)
	public String all(Model model, String processId, String orderId, String taskId) {
		Process process = facets.getEngine().process().getProcessById(processId);
		if(StringUtils.isNotEmpty(process.getInstanceUrl())) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("redirect:");
			buffer.append(process.getInstanceUrl());
			buffer.append("?processId=").append(processId);
			buffer.append("&processName=").append(process.getName());
			if(StringUtils.isNotEmpty(orderId)) {
				buffer.append("&orderId=").append(orderId);
			}
			if(StringUtils.isNotEmpty(taskId)) {
				buffer.append("&taskId=").append(taskId);
			}
			
			return buffer.toString();
		}
		List<WorkModel> models = process.getModel().getWorkModels();
		String currentTaskName = "";
		if(StringUtils.isNotEmpty(orderId) && StringUtils.isNotEmpty(taskId)) {
			Order order = facets.getEngine().query().getOrder(orderId);
			Task task = facets.getEngine().query().getTask(taskId);
			model.addAttribute("order", order);
			model.addAttribute("task", task);
			currentTaskName = task.getTaskName();
			List<HistoryTask> tasks = facets.getEngine().query().getHistoryTasks(new QueryFilter().setOrderId(orderId));
			for(HistoryTask history : tasks) {
				model.addAttribute("variable_" + history.getTaskName(), history.getVariableMap());
			}
		} else {
			currentTaskName = models.isEmpty() ? "" : models.get(0).getName();
		}
		model.addAttribute("works", models);
		model.addAttribute("process", process);
		model.addAttribute("current", currentTaskName);
		return "flow/all";
	}
}
