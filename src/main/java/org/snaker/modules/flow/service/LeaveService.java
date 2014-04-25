/* Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.modules.flow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Task;
import org.snaker.framework.security.entity.User;
import org.snaker.framework.security.shiro.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuqs
 * @since 0.1
 */
@Component
public class LeaveService {
	@Autowired
	private SnakerEngine snakerEngine;
	public void applySave(String processId, String orderId, float day, String reason) {
		User user = ShiroUtils.getUser();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("apply.operator", user.getUsername());
		args.put("approveDept.operator", user.getUsername());
		args.put("approveBoss.operator", user.getUsername());
		args.put("day", day);
		args.put("reason", reason);
		if(StringUtils.isEmpty(orderId)) {
			Order order = snakerEngine.startInstanceById(processId, user.getUsername(), args);
			orderId = order.getId();
		}
		
		List<Task> tasks = snakerEngine.query().getActiveTasks(new QueryFilter().setOrderId(orderId));
		if(tasks != null && tasks.size() > 0) {
			Task task = tasks.get(0);
			//执行申请任务
			snakerEngine.executeTask(task.getId(), user.getUsername(), args);
		}
	}
}
