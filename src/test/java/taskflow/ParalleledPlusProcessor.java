package taskflow;

import org.samir.tools.taskflow.Processor;
import taskflow.entity.CalcEntity;
import taskflow.entity.CalcResultEntity;

/**
 * @author Samir
 * @date 2019/12/31 14:15
 */
public class ParalleledPlusProcessor implements Processor<CalcEntity, CalcResultEntity> {
    @Override
    public boolean process(CalcEntity params, CalcResultEntity result) {
        params.getAtomicPlusSeed().incrementAndGet();
        params.setPlusSeed(params.getPlusSeed() + 1);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }
        result.getStepTracer().put(Thread.currentThread().getName(),"ParalleledPlusProcessor");
        return params.getRunNext();
    }
}
