package org.samir.tools.taskflow;

import com.google.common.collect.Sets;
import org.samir.tools.taskflow.entity.Task;
import org.samir.tools.taskflow.entity.TaskChain;
import org.samir.tools.taskflow.entity.config.SerializedTask;
import org.samir.tools.taskflow.executor.TaskFlowForkJoinPool;
import org.samir.tools.taskflow.executor.TaskFlowThreadPool;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Samir
 * @date 2019/12/17 11:31
 */
public class DefaultTaskTopology<T, V> implements TaskTopology {

    private static final Map<String,Object> INSTANCE_CACHE = new ConcurrentHashMap<>();
    private TaskChain chain;
    private T params;
    private V result;
    private Boolean useForkJoinPool;
    private int totalChainNum;

    public DefaultTaskTopology(SerializedTask serializedTask, T params, V result, Boolean buildTree, Boolean useForkJoinPool){
        this.chain = TaskChain.build(serializedTask, buildTree);
        this.params = params;
        this.result = result;
        this.useForkJoinPool = useForkJoinPool;
        if(buildTree){
            this.totalChainNum = calcTotalChainNum(chain);
        } else {
            this.totalChainNum = 1;
        }
    }

    private int calcTotalChainNum(TaskChain paramChain) {
        int num = 1;
        TaskChain tempChain = paramChain;
        do{
            if(tempChain.getCurrent() != null && tempChain.getCurrent().getSubChain() != null){
                num += calcTotalChainNum(tempChain.getCurrent().getSubChain());
            }
        } while ((tempChain = tempChain.getNextNode()) != null);
        return num;
    }


    /**
     * simple linked constructed task chain, return value sensitive
     * @throws Exception
     */
    @Override
    public void runSimpleChain() throws Exception {
        runSimpleChain(true);
    }

    @Override
    public void runSimpleChain(boolean checkResult) throws Exception {
        if(params == null || result == null || chain == null){
            throw new IllegalArgumentException("params and result can not be empty");
        }
        do{
            Task current = chain.getCurrent();
            if(current != null){
                if(!doSimpleTask(current,params,result) && checkResult){
                    return;
                }
            }
        } while ((chain = chain.getNextNode()) != null);
    }

    private Boolean doSimpleTask(Task current, T params, V result) throws Exception {
        Boolean runNext = Boolean.TRUE;
        String currentProcessor = current.getProcessor();
        if(currentProcessor != null && !currentProcessor.trim().isEmpty()){
            Class<?> processor = Class.forName(currentProcessor);
            List<Class<?>> classes = Arrays.asList(processor.getInterfaces());
            if(!classes.contains(Processor.class)){
                throw new Exception(String.format("processor class name:%s doesn't implements Processor interface",currentProcessor));
            }
            Method method = processor.getDeclaredMethod("process", params.getClass(), result.getClass());
            if(INSTANCE_CACHE.get(currentProcessor) == null){
                INSTANCE_CACHE.put(currentProcessor, processor.newInstance());
            }
            runNext = (Boolean) method.invoke(INSTANCE_CACHE.get(currentProcessor), params, result);
        }
        if(current.getParalleledSubProcessor() != null && !current.getParalleledSubProcessor().isEmpty()){
            List<String> paralleledSubProcessor = current.getParalleledSubProcessor();
            runNext = TaskFlowThreadPool.invokeAll(paralleledSubProcessor,params,result,INSTANCE_CACHE) && runNext;
        }
        return runNext;
    }


    /**
     * complex tree constructed task chain, return value insensitive
     *
     *  ******* !!!WARN!!! NO SUFFICIENT TEST *******
     *
     * @throws Exception
     */
    @Override
    public void runTreeChain() throws Exception {
        if(useForkJoinPool()){
            //do not support (log tag) ThreadLocal context transfer
            runWithForkJoinPool();
        } else {
            runWithThreadPool();
        }
    }

    private void runWithThreadPool() throws Exception {
        Set<Future<Void>> futureSet = Sets.newConcurrentHashSet();
        if(params == null || result == null || chain == null){
            throw new IllegalArgumentException("params and result can not be empty");
        }
        do{
            Task current = chain.getCurrent();
            if(current != null){
                doTreeTask(current,params,result,futureSet);
            }
        } while ((chain = chain.getNextNode()) != null);
        while (totalChainNum != futureSet.size() + 1){
            //no need to sleep
        }
        for(Future<Void> f : futureSet){
            //block until all sub chain task well done
            f.get();
        }
    }

    /**
     * do not support (log tag) ThreadLocal context transfer
     */
    private void runWithForkJoinPool() {
        Set<TaskFlowForkJoinPool.Chain> chainHolder = Sets.newConcurrentHashSet();
        TaskFlowForkJoinPool.Chain task = new TaskFlowForkJoinPool.Chain(chain,params,result,INSTANCE_CACHE,chainHolder);
        TaskFlowForkJoinPool.getPool().invoke(task);
        while (totalChainNum != chainHolder.size()){
            //no need to sleep
        }
        for(TaskFlowForkJoinPool.Chain chain : chainHolder){
            //block until all sub chain task well done
            chain.join();
        }
    }

    private boolean useForkJoinPool() {
        return useForkJoinPool;
    }

    private void doTreeTask(Task current, T params, V result, Set<Future<Void>> futureSet) throws Exception {
        doSimpleTask(current, params, result);
        doSubChainTask(current.getSubChain(),futureSet);
    }

    private void doSubChainTask(TaskChain subChain, Set<Future<Void>> futureSet) {
        if(subChain == null){
            return;
        }
        Future<Void> future = TaskFlowThreadPool.getPool().submit(() -> {
            try {
                TaskChain tempChain = subChain;
                do {
                    Task current = tempChain.getCurrent();
                    if (current != null) {
                        doTreeTask(current, params, result, futureSet);
                    }
                } while ((tempChain = tempChain.getNextNode()) != null);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            return null;
        });
        futureSet.add(future);
    }
}
