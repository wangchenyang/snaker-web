package org.snaker.modules.base.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.StreamHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.ProcessModel;
import org.snaker.framework.security.shiro.ShiroUtils;
import org.snaker.modules.base.helper.SnakerJsonHelper;
import org.snaker.modules.base.service.SnakerEngineFacets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
	private SnakerEngine snakerEngine;
	@Autowired
	private SnakerEngineFacets facets;
	/**
	 * 根据当前用户查询待办任务列表
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/user", method=RequestMethod.GET)
	public String userTaskList(Model model, Page<WorkItem> page) {
		snakerEngine.query().getWorkItems(page, 
				new QueryFilter().setOperators(new String[]{ShiroUtils.getUsername()}));
		model.addAttribute("page", page);
		return "snaker/userTask";
	}
	
	/**
	 * 活动任务查询列表
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "task/active", method=RequestMethod.GET)
	public String activeTaskList(Model model, Page<WorkItem> page, String error) {
		List<String> list = ShiroUtils.getGroups();
		list.add(ShiroUtils.getUsername());
		log.info(list.toString());
		String[] assignees = new String[list.size()];
		list.toArray(assignees);
		snakerEngine.query().getWorkItems(page, 
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
		snakerEngine.executeTask(taskId);
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
			snakerEngine.executeAndJumpTask(taskId, ShiroUtils.getUsername(), null, null);
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
		snakerEngine.query().getHistoryWorkItems(page, 
				new QueryFilter().setOperators(new String[]{ShiroUtils.getUsername()}));
		model.addAttribute("page", page);
		return "snaker/historyTask";
	}
	
	/**
	 * 历史任务撤回
	 * @param taskId
	 * @return
	 */
	@RequestMapping(value = "task/undo", method=RequestMethod.GET)
	public String historyTaskUndo(String taskId) {
		snakerEngine.task().withdrawTask(taskId, ShiroUtils.getUsername());
		return "redirect:/snaker/task/active";
	}
	
	/**
	 * 流程实例查询
	 * @param model
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "order", method=RequestMethod.GET)
	public String order(Model model, Page<HistoryOrder> page) {
		snakerEngine.query().getHistoryOrders(page, new QueryFilter());
		model.addAttribute("page", page);
		return "snaker/order";
	}
	
	/**
	 * 流程定义查询列表
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/list", method=RequestMethod.GET)
	public String processList(Model model, Page<Process> page, String displayName) {
		QueryFilter filter = new QueryFilter();
		if(StringHelper.isNotEmpty(displayName)) {
			filter.setDisplayName(displayName);
		}
		snakerEngine.process().getProcesss(page, filter);
		model.addAttribute("page", page);
		return "snaker/processList";
	}
	
	/**
	 * 初始化流程定义
	 * @return
	 */
	@RequestMapping(value = "process/init", method=RequestMethod.GET)
	public String processInit() {
		facets.initFlows();
		return "redirect:/snaker/process/list";
	}
	
	/**
	 * 根据流程定义部署
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/deploy", method=RequestMethod.GET)
	public String processDeploy(Model model) {
		return "snaker/processDeploy";
	}
	
	/**
	 * 新建流程定义
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/add", method=RequestMethod.GET)
	public String processAdd(Model model) {
		return "snaker/processAdd";
	}
	
	/**
	 * 新建流程定义[web流程设计器]
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/designer", method=RequestMethod.GET)
	public String processDesigner(String processId, Model model) {
		if(StringUtils.isNotEmpty(processId)) {
			Process process = snakerEngine.process().getProcessById(processId);
			AssertHelper.notNull(process);
			ProcessModel processModel = process.getModel();
			if(processModel != null) {
				String json = SnakerJsonHelper.getModelJson(processModel);
				model.addAttribute("process", json);
				System.out.println(json);
			}
			model.addAttribute("processId", processId);
		}
		return "snaker/processDesigner";
	}
	
	/**
	 * 编辑流程定义
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/edit/{id}", method=RequestMethod.GET)
	public String processEdit(Model model, @PathVariable("id") String id) {
		Process process = snakerEngine.process().getProcessById(id);
		model.addAttribute("process", process);
		if(process.getDBContent() != null) {
			try {
				model.addAttribute("content", StringHelper.textXML(new String(process.getDBContent(), "GBK")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return "snaker/processEdit";
	}
	
	/**
	 * 根据流程定义ID，删除流程定义
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "process/delete/{id}", method=RequestMethod.GET)
	public String processDelete(@PathVariable("id") String id) {
		snakerEngine.process().undeploy(id);
		return "redirect:/snaker/process/list";
	}
	
	/**
	 * 添加流程定义后的部署
	 * @param snakerFile
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/deploy", method=RequestMethod.POST)
	public String processDeploy(@RequestParam(value = "snakerFile") MultipartFile snakerFile, String id) {
		try {
			InputStream input = snakerFile.getInputStream();
			if(StringUtils.isNotEmpty(id)) {
				snakerEngine.process().redeploy(id, input);
			} else {
				snakerEngine.process().deploy(input);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/snaker/process/list";
	}
	
	/**
	 * 保存流程定义[web流程设计器]
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/deployXml", method=RequestMethod.POST)
	@ResponseBody
	public boolean processDeploy(String model, String id) {
		try {
			String xml = "<?xml version=\"1.0\" encoding=\"GBK\" standalone=\"no\"?>\n" + model;
			System.out.println("model xml=\n" + xml);
			InputStream input = StreamHelper.getStreamFromString(xml);
			if(StringUtils.isNotEmpty(id)) {
				snakerEngine.process().redeploy(id, input);
			} else {
				snakerEngine.process().deploy(input);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 新建流程定义
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "process/save", method=RequestMethod.POST)
	public String processSave(Process process) {
		process.setId(StringHelper.getPrimaryKey());
		process.setState(1);
		snakerEngine.process().saveProcess(process);
		return "redirect:/snaker/process/list";
	}
	
	@RequestMapping(value = "process/start", method=RequestMethod.GET)
	public String processStart(Model model, String processId) {
		snakerEngine.startInstanceById(processId);
		return "redirect:/snaker/process/list";
	}
	
	@RequestMapping(value = "process/json", method=RequestMethod.GET)
	@ResponseBody
	public Object json(String orderId) {
		HistoryOrder order = snakerEngine.query().getHistOrder(orderId);
		List<Task> tasks = snakerEngine.query().getActiveTasks(new QueryFilter().setOrderId(orderId));
		Process process = snakerEngine.process().getProcessById(order.getProcessId());
		AssertHelper.notNull(process);
		ProcessModel model = process.getModel();
		Map<String, String> jsonMap = new HashMap<String, String>();
		if(model != null) {
			jsonMap.put("process", SnakerJsonHelper.getModelJson(model));
		}
		
		//{"activeRects":{"rects":[{"paths":[],"name":"任务3"},{"paths":[],"name":"任务4"},{"paths":[],"name":"任务2"}]},"historyRects":{"rects":[{"paths":["TO 任务1"],"name":"开始"},{"paths":["TO 分支"],"name":"任务1"},{"paths":["TO 任务3","TO 任务4","TO 任务2"],"name":"分支"}]}}
		if(tasks != null && !tasks.isEmpty()) {
			jsonMap.put("active", SnakerJsonHelper.getActiveJson(tasks));
		}
		return jsonMap;
	}
	
	@RequestMapping(value = "process/display", method=RequestMethod.GET)
	public String display(Model model, String orderId) {
		HistoryOrder order = snakerEngine.query().getHistOrder(orderId);
		model.addAttribute("order", order);
		List<HistoryTask> tasks = snakerEngine.query().getHistoryTasks(new QueryFilter().setOrderId(orderId));
		model.addAttribute("tasks", tasks);
		return "snaker/processView";
	}
}
