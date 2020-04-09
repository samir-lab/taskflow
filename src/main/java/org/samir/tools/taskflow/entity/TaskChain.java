package org.samir.tools.taskflow.entity;

import org.samir.tools.taskflow.entity.config.SerializedTask;

import java.util.Comparator;
import java.util.List;

/**
 * @author Samir
 * @date 2019/12/17 11:39
 */
public class TaskChain {

    private Task current;

    private TaskChain nextNode;

    /**
     * build from json config
     * @param serializedTask
     * @return
     */
    public static TaskChain build(SerializedTask serializedTask, Boolean buildTree){
        assert serializedTask != null && serializedTask.getSerializedTasks() != null && !serializedTask.getSerializedTasks().isEmpty();
        return buildAssert(serializedTask.getSerializedTasks(),buildTree);
    }

    /**
     * build from task list
     * @param tasks
     * @param buildTree
     * @return
     */
    public static TaskChain buildAssert(List<Task> tasks, Boolean buildTree) {
        assert tasks != null && tasks.size() > 0;
        assert tasks.stream().noneMatch(p -> p.getStep() == null);
        if(!buildTree){
            assert tasks.stream().noneMatch(p -> p.getSubChain() != null);
        }
        return build(tasks);
    }

    private static TaskChain build(List<Task> tasks) {
        tasks.sort(Comparator.comparingInt(Task::getStep));
        TaskChain root = new TaskChain();
        TaskChain tempNode = new TaskChain();
        for(int i = 0; i < tasks.size(); ++i){
            Task node = tasks.get(i);
            TaskChain nextNode = new TaskChain();
            if(i == 0){
                root.setCurrent(node);
                root.setNextNode(nextNode);
            } else {
                tempNode.setCurrent(node);
                tempNode.setNextNode(i == tasks.size() - 1 ? null : nextNode);
            }
            tempNode = nextNode;
        }
        return root;
    }

    public Task getCurrent() {
        return current;
    }

    public void setCurrent(Task current) {
        this.current = current;
    }

    public TaskChain getNextNode() {
        return nextNode;
    }

    public void setNextNode(TaskChain nextNode) {
        this.nextNode = nextNode;
    }
}
