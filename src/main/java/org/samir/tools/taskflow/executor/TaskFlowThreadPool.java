package org.samir.tools.taskflow.executor;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.samir.tools.taskflow.Processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Samir
 * @date 2019/12/18 13:21
 */
public class TaskFlowThreadPool {

    private static final TransmittableThreadLocal<ConcurrentMap<String,String>> context = new TransmittableThreadLocal<>();

    private static final ExecutorService executor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 10,
            Runtime.getRuntime().availableProcessors() * 10 + 40,
            1,
            TimeUnit.DAYS,
            new ArrayBlockingQueue<>(40),
            new ThreadFactory() {
                private final AtomicInteger mThreadNum = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("TaskFlowWorker-%d", mThreadNum.getAndIncrement()));
                }
            }, new ThreadPoolExecutor.CallerRunsPolicy());

    public static ExecutorService getPool(){
        return TtlExecutors.getTtlExecutorService(executor);
    }

    public static TransmittableThreadLocal<ConcurrentMap<String,String>> getContext(){
        return context;
    }

    public static <T, V>  Boolean invokeAll(List<String> taskList, T params, V result, Map<String, Object> instanceCache){
        Boolean success = Boolean.TRUE;
        CompletionService<Boolean> service = new ExecutorCompletionService<>(getPool());
        for(String taskName : taskList){
            service.submit(() -> {
                try {
                    Class<?> task = Class.forName(taskName);
                    List<Class<?>> classes = Arrays.asList(task.getInterfaces());
                    if(!classes.contains(Processor.class)){
                        throw new Exception(String.format("processor class name: %s doesn't implements Processor interface", taskName));
                    }
                    Method method = task.getMethod("process", params.getClass(), result.getClass());
                    if(instanceCache.get(taskName) == null){
                        instanceCache.put(taskName, task.newInstance());
                    }
                    return (Boolean) method.invoke(instanceCache.get(taskName), params, result);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            });
        }
        for(String task : taskList){
            try {
                Future<Boolean> future = service.take();
                if(!future.get()){
                    success = Boolean.FALSE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                success = Boolean.FALSE;
            }
        }
        service = null; //help GC
        return success;
    }

}
