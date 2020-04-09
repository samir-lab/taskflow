package taskflow;

import org.samir.tools.taskflow.Processor;
import taskflow.entity.CalcEntity;
import taskflow.entity.CalcResultEntity;

/**
 * @author Samir
 * @date 2019/12/31 14:17
 */
public class SubtractProcessor implements Processor<CalcEntity, CalcResultEntity> {
    @Override
    public boolean process(CalcEntity params, CalcResultEntity result) {
        params.setSeed(params.getSeed() - 2);
        result.getStepTracer().put(Thread.currentThread().getName(),"SubtractProcessor");
        return params.getRunNext();
    }
}
