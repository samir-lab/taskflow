import org.junit.Assert;
import org.junit.Test;
import org.samir.tools.taskflow.TaskTopology;
import org.samir.tools.taskflow.entity.Task;
import org.samir.tools.taskflow.entity.TaskChain;
import org.samir.tools.taskflow.entity.config.SerializedTask;
import org.samir.tools.taskflow.executor.TaskFlowThreadPool;
import org.samir.tools.taskflow.factory.TaskTopologyFactory;
import taskflow.entity.CalcEntity;
import taskflow.entity.CalcResultEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Samir
 * @date 2019/12/17 16:19
 */

public class SegmentTest {

    static List<Task> tasks = new ArrayList<>();

    static {
        Random rand = new Random();
        for(int i = 0; i < 5; ++i){
            Task t = new Task();
            t.setStep(rand.nextInt(20));
            tasks.add(t);
        }
    }


    @Test
    public void testChainBuilder(){
        tasks.sort(Comparator.comparingInt(Task::getStep));
        TaskChain chain = TaskChain.buildAssert(tasks,true);
        Assert.assertNotNull(chain);
        TaskChain temp = chain;
        Assert.assertTrue(temp.getCurrent().getStep() <= temp.getNextNode().getCurrent().getStep());
        temp = temp.getNextNode();
        Assert.assertTrue(temp.getCurrent().getStep() <= temp.getNextNode().getCurrent().getStep());
        temp = temp.getNextNode();
        Assert.assertTrue(temp.getCurrent().getStep() <= temp.getNextNode().getCurrent().getStep());
        temp = temp.getNextNode();
        Assert.assertTrue(temp.getCurrent().getStep() <= temp.getNextNode().getCurrent().getStep());
        temp = temp.getNextNode();
        Assert.assertNull(temp.getNextNode());
    }


    private static SerializedTask serializedTask = new SerializedTask();
    private static CalcEntity x = new CalcEntity();
    static{
        List<Task> list = new ArrayList<>();
        //step1
        Task t1 = new Task();
        t1.setStep(1);
        t1.setProcessor("taskflow.MultiProcessor");
        list.add(t1);
        //step2
        Task t2 = new Task();
        t2.setStep(2);
        t2.setProcessor("taskflow.SubtractProcessor");
        t2.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<2000;++i){
            t2.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        list.add(t2);
        //step3
        Task t3 = new Task();
        t3.setStep(3);
        t3.setProcessor("taskflow.DivideProcessor");
        list.add(t3);
        //step4
        Task t4 = new Task();
        t4.setStep(4);
        t4.setProcessor("taskflow.MultiProcessor");
        list.add(t4);

        serializedTask.setSerializedTasks(list);
        x.setSeed(5);
        x.setAtomicPlusSeed(new AtomicInteger(3));
        x.setPlusSeed(3);
    }


    @Test
    public void testSimpleChain() throws Exception {
        x.setSeed(5);
        x.setAtomicPlusSeed(new AtomicInteger(3));
        x.setPlusSeed(3);
        CalcResultEntity result = new CalcResultEntity();
        TaskTopologyFactory<CalcEntity, CalcResultEntity> factory = new TaskTopologyFactory<>();
        TaskTopology topology = factory.createDefaultTopology(serializedTask, x, result);
        TaskFlowThreadPool.getContext().set(new ConcurrentHashMap<>());
        topology.runSimpleChain();

        Assert.assertEquals(8, (int) x.getSeed());
        Assert.assertEquals(2003, x.getAtomicPlusSeed().get());
        Assert.assertTrue(x.getPlusSeed() < 2003);
        Assert.assertNotNull(result.getStepTracer());
    }

    @Test
    public void testTreeChainWithThreadPool() throws Exception {
        x.setSeed(5);
        x.setAtomicPlusSeed(new AtomicInteger(3));
        x.setPlusSeed(3);
        TaskChain subChain = new TaskChain();
        Task t1 = new Task();
        t1.setStep(1);
        t1.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<500;i++){
            t1.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        subChain.setCurrent(t1);
        TaskChain subChain2 = new TaskChain();
        Task t2 = new Task();
        t2.setStep(1);
        t2.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<500;i++){
            t2.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        subChain2.setCurrent(t2);
        t1.setSubChain(subChain2);

        TaskChain subChain3 = new TaskChain();
        Task t3 = new Task();
        t3.setStep(1);
        t3.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<300;i++){
            t3.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        subChain3.setCurrent(t3);
        t2.setSubChain(subChain3);

        serializedTask.getSerializedTasks().get(1).setSubChain(subChain);
        CalcResultEntity result = new CalcResultEntity();
        TaskTopologyFactory<CalcEntity, CalcResultEntity> factory = new TaskTopologyFactory<>();
        TaskTopology topology = factory.createDefaultTopology(serializedTask, x, result,true);
        TaskFlowThreadPool.getContext().set(new ConcurrentHashMap<>());
        topology.runTreeChain();

        System.out.println("AtomicPlusSeed: " + x.getAtomicPlusSeed().get());
        System.out.println("PlusSeed: " + x.getPlusSeed());
        Assert.assertEquals(8, (int) x.getSeed());
        Assert.assertEquals(3303, x.getAtomicPlusSeed().get());
        Assert.assertTrue(x.getPlusSeed() < 3303);
        Assert.assertNotNull(result.getStepTracer());

        // do not affect other case
        serializedTask.getSerializedTasks().get(1).setSubChain(null);
    }

    @Test
    public void testTreeChainWithForkJoinPool() throws Exception {
        x.setSeed(5);
        x.setAtomicPlusSeed(new AtomicInteger(3));
        x.setPlusSeed(3);
        TaskChain subChain = new TaskChain();
        Task t1 = new Task();
        t1.setStep(1);
        t1.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<300;i++){
            t1.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }

        subChain.setCurrent(t1);
        TaskChain subChain2 = new TaskChain();
        Task t2 = new Task();
        t2.setStep(1);
        t2.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<300;i++){
            t2.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        subChain2.setCurrent(t2);
        t1.setSubChain(subChain2);

        TaskChain subChain3 = new TaskChain();
        Task t3 = new Task();
        t3.setStep(1);
        t3.setParalleledSubProcessor(new ArrayList<>());
        for(int i=0;i<300;i++){
            t3.getParalleledSubProcessor().add("taskflow.ParalleledPlusProcessor");
        }
        subChain3.setCurrent(t3);
        t2.setSubChain(subChain3);

        serializedTask.getSerializedTasks().get(2).setSubChain(subChain);
        CalcResultEntity result = new CalcResultEntity();
        TaskTopologyFactory<CalcEntity, CalcResultEntity> factory = new TaskTopologyFactory<>();
        TaskTopology topology = factory.createDefaultTopology(serializedTask, x, result,true,true);
        TaskFlowThreadPool.getContext().set(new ConcurrentHashMap<>());
        topology.runTreeChain();

        System.out.println("AtomicPlusSeed: " + x.getAtomicPlusSeed().get());
        System.out.println("PlusSeed: " + x.getPlusSeed());
        Assert.assertEquals(8, (int) x.getSeed());
        Assert.assertEquals(2903, x.getAtomicPlusSeed().get());
        Assert.assertTrue(x.getPlusSeed() < 2903);
        Assert.assertNotNull(result.getStepTracer());

        // do not affect other case
        serializedTask.getSerializedTasks().get(2).setSubChain(null);
    }

}
