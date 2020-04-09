package org.samir.tools.taskflow.executor;

import org.samir.tools.taskflow.Processor;
import org.samir.tools.taskflow.entity.Task;
import org.samir.tools.taskflow.entity.TaskChain;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

/**
 * @author Samir
 * @date 2020/1/2 13:18
 */
public class TaskFlowForkJoinPool {

    private static final ForkJoinPool pool = new ForkJoinPool();

    public static ForkJoinPool getPool(){
        return pool;
    }

    /**
     * TODO i/o task shold be used
     * @param supplier
     * @param <T>
     * @return
     */
    public static<T> T callInManagedBlock(final Supplier<T> supplier) throws InterruptedException {
        final SupplierManagedBlock<T> managedBlock = new SupplierManagedBlock<>(supplier);
        try {
            ForkJoinPool.managedBlock(managedBlock);
        } catch (InterruptedException e) {
            throw e;
        }
        return managedBlock.getResult();
    }

    private static final class SupplierManagedBlock<T> implements ForkJoinPool.ManagedBlocker {
        private final Supplier<T> supplier;
        private T result;
        private boolean done = false;

        private SupplierManagedBlock(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean block() {
            result = supplier.get();
            done = true;
            return true;
        }

        @Override
        public boolean isReleasable() {
            return done;
        }

        public T getResult() {
            return result;
        }
    }


    public static final class Chain extends RecursiveTask<Void> {
        private TaskChain chain;
        private Object params;
        private Object result;
        private Map<String, Object> instanceCache;
        private Set<Chain> chainHolder;

        public Chain(TaskChain chain, Object param, Object result, Map<String, Object> instanceCache, Set<Chain> chainHolder){
            this.chain = chain;
            this.params = param;
            this.result = result;
            this.instanceCache = instanceCache;
            this.chainHolder = chainHolder;
            chainHolder.add(this);
        }

        @Override
        protected Void compute() {
            do{
                doTreeTask(chain.getCurrent(), params, result);
            } while ((chain = chain.getNextNode()) != null);
            return null;
        }

        /**
         * do not support (log tag) ThreadLocal context transfer
         * @param current
         * @param params
         * @param result
         */
        private void doTreeTask(Task current, Object params, Object result) {
            try {
                String currentProcessor = current.getProcessor();
                if(currentProcessor != null && !currentProcessor.trim().isEmpty()){
                    Class<?> processor = Class.forName(currentProcessor);
                    List<Class<?>> classes = Arrays.asList(processor.getInterfaces());
                    if(!classes.contains(Processor.class)){
                        throw new Exception(String.format("processor class name:%s doesn't implements Processor interface ", currentProcessor));
                    }
                    Method method = processor.getDeclaredMethod("process", params.getClass(), result.getClass());
                    if(instanceCache.get(currentProcessor) == null){
                        instanceCache.put(currentProcessor, processor.newInstance());
                    }
                    method.invoke(instanceCache.get(currentProcessor), params, result);
                }
                if(current.getParalleledSubProcessor() != null && !current.getParalleledSubProcessor().isEmpty()){
                    List<String> paralleledSubProcessor = current.getParalleledSubProcessor();
                    TaskFlowThreadPool.invokeAll(paralleledSubProcessor,params,result,instanceCache);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            if(current.getSubChain() != null){
                Chain subChain = new Chain(current.getSubChain(),params,result,instanceCache,chainHolder);
                subChain.fork();
            }
        }
    }


}
