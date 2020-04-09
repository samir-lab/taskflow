package taskflow.entity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Samir
 * @date 2019/12/31 14:19
 */
public class CalcResultEntity {

    private ConcurrentMap<String,String> stepTracer = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getStepTracer() {
        return stepTracer;
    }

    public void setStepTracer(ConcurrentMap<String, String> stepTracer) {
        this.stepTracer = stepTracer;
    }
}
