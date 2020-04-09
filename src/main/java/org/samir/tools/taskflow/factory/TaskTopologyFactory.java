package org.samir.tools.taskflow.factory;

import org.samir.tools.taskflow.DefaultTaskTopology;
import org.samir.tools.taskflow.TaskTopology;
import org.samir.tools.taskflow.entity.config.SerializedTask;

/**
 * @author Samir
 * @date 2019/12/17 11:35
 */
public class TaskTopologyFactory<T, V> {

    public TaskTopology createDefaultTopology(SerializedTask serializedTask, T params, V result){
        return createDefaultTopology(serializedTask,params,result,false);
    }

    public TaskTopology createDefaultTopology(SerializedTask serializedTask, T params, V result, Boolean buildTree){
        return createDefaultTopology(serializedTask,params,result,buildTree,false);
    }

    public TaskTopology createDefaultTopology(SerializedTask serializedTask, T params, V result, Boolean buildTree, Boolean useForkJoinPool){
        return new DefaultTaskTopology<>(serializedTask,params,result,buildTree,useForkJoinPool);
    }

}
