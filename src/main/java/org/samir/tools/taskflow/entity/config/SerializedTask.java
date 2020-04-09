package org.samir.tools.taskflow.entity.config;

import org.samir.tools.taskflow.entity.Task;

import java.util.List;

/**
 * @author Samir
 * @date 2019/12/17 13:43
 */
public class SerializedTask {

    /**
     * deserialized tasks json configuration
     */
    private List<Task> serializedTasks;

    public List<Task> getSerializedTasks() {
        return serializedTasks;
    }

    public void setSerializedTasks(List<Task> serializedTasks) {
        this.serializedTasks = serializedTasks;
    }
}
