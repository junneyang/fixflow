package com.founder.fix.fixflow.core.impl.bpmn.behavior;

import java.util.Date;

import org.eclipse.bpmn2.impl.StartEventImpl;

import com.founder.fix.fixflow.core.event.BaseElementEvent;
import com.founder.fix.fixflow.core.impl.Context;
import com.founder.fix.fixflow.core.impl.identity.Authentication;
import com.founder.fix.fixflow.core.impl.runtime.TokenEntity;
import com.founder.fix.fixflow.core.impl.task.TaskInstanceEntity;
import com.founder.fix.fixflow.core.impl.util.ClockUtil;
import com.founder.fix.fixflow.core.impl.util.EMFExtensionUtil;
import com.founder.fix.fixflow.core.impl.util.GuidUtil;
import com.founder.fix.fixflow.core.impl.util.StringUtil;
import com.founder.fix.fixflow.core.runtime.ExecutionContext;
import com.founder.fix.fixflow.core.task.TaskInstance;
import com.founder.fix.fixflow.core.task.TaskInstanceType;


public class StartEventBehavior extends StartEventImpl {
	
	
	public boolean isPersistence() {
		
		Object isPersistenceObj=EMFExtensionUtil.getAnyAttributeValue(this, "isPersistence");
		if(isPersistenceObj==null||isPersistenceObj.equals("")){
			return true;
		}else{
			return StringUtil.getBoolean(isPersistenceObj);
		}
		
		
		//return isAsync;
	}
	
	

	public void enter(ExecutionContext executionContext) {

		TokenEntity token = executionContext.getToken();

		// 把令牌的所在节点设置为当前节点
		token.setFlowNode(this);

		// 触发流程启动事件
		token.getProcessInstance().getProcessDefinition().fireEvent(BaseElementEvent.EVENTTYPE_PROCESS_START, executionContext);

		// 设置令牌进入节点的时间
		token.setNodeEnterTime(new Date());
		
		//插入流程启动记录
		createStartEventTask(executionContext);

		// 移除执行内容对象的线条关联
		executionContext.setSequenceFlow(null);

		executionContext.setSequenceFlowSource(null);

		execute(executionContext);

	}
	
	private void createStartEventTask(ExecutionContext executionContext){
		
		//构造创建人物所需的数据
		String newTaskId=GuidUtil.CreateGuid();
		String newTaskProcessInstanceId=executionContext.getProcessInstance().getId();		
		String newTaskProcessDefinitionId=executionContext.getProcessDefinition().getProcessDefinitionId();		
		String newTaskTokenId=executionContext.getToken().getId();		
		String newTaskNodeId=executionContext.getToken().getNodeId();		
		String newTaskNodeName=executionContext.getToken().getFlowNode().getName();		
		String newTaskDescription=newTaskNodeName;	
		Date newTaskCreateTime=ClockUtil.getCurrentTime();
		int newTaskPriority= TaskInstance.PRIORITY_NORMAL;
		String newTaskProcessDefinitionKey=executionContext.getProcessDefinition().getProcessDefinitionKey();
		TaskInstanceType newTaskTaskInstanceType=TaskInstanceType.FIXSTARTEVENT;
		String newTaskProcessDefinitionName=executionContext.getProcessDefinition().getName();
		boolean isDraft=false;
				
		
				
		//创建任务
		TaskInstanceEntity taskInstance=new TaskInstanceEntity();
		taskInstance.setId(newTaskId);
		taskInstance.setNodeName(newTaskNodeName);
		taskInstance.setProcessInstanceId(newTaskProcessInstanceId);
		taskInstance.setProcessDefinitionId(newTaskProcessDefinitionId);
		taskInstance.setTokenId(newTaskTokenId);
		taskInstance.setNodeId(newTaskNodeId);
		taskInstance.setName(newTaskNodeName);
		taskInstance.setDescription(newTaskDescription);
		taskInstance.setCreateTime(newTaskCreateTime);
		taskInstance.setPriority(newTaskPriority);
		taskInstance.setProcessDefinitionKey(newTaskProcessDefinitionKey);
		taskInstance.setTaskInstanceType(newTaskTaskInstanceType);
		taskInstance.setProcessDefinitionName(newTaskProcessDefinitionName);
		taskInstance.setDraft(isDraft);
		
	
		taskInstance.setAssigneeId(Authentication.getAuthenticatedUserId());
		//taskInstance.setEndTime(newTaskEndTime);
		taskInstance.setCommandType("startEvent");
		taskInstance.setCommandMessage("流程启动");
		
		
		taskInstance.setEndTime(ClockUtil.getCurrentTime());
		//taskInstance.setCallActivityInstanceId(subProcessInstanceId);
				
		Context.getCommandContext().getTaskManager().saveTaskInstanceEntity((TaskInstanceEntity)taskInstance);
		
	}

}
