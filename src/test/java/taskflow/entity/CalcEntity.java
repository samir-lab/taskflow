package taskflow.entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Samir
 * @date 2019/12/31 14:19
 */
public class CalcEntity {

    private AtomicInteger atomicPlusSeed = new AtomicInteger(10);

    private Integer plusSeed = 10;

    private Integer seed = 100;

    private Boolean runNext = Boolean.TRUE;

    public AtomicInteger getAtomicPlusSeed() {
        return atomicPlusSeed;
    }

    public void setAtomicPlusSeed(AtomicInteger atomicPlusSeed) {
        this.atomicPlusSeed = atomicPlusSeed;
    }

    public Integer getPlusSeed() {
        return plusSeed;
    }

    public void setPlusSeed(Integer plusSeed) {
        this.plusSeed = plusSeed;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Boolean getRunNext() {
        return runNext;
    }

    public void setRunNext(Boolean runNext) {
        this.runNext = runNext;
    }
}
