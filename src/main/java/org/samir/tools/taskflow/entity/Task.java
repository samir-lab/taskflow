package org.samir.tools.taskflow.entity;

import java.util.List;

/**
 * @author Samir
 * @date 2019/12/17 13:49
 */
public class Task {

    /**
     * task step
     */
    private Integer step;

    /**
     * task class full name
     */
    private String processor;

    /**
     * paralleled tasks triggered after processor's work done
     */
    private List<String> paralleledSubProcessor;

    /**
     * subchain assigned to mainchain's task node
     */
    private TaskChain subChain;

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public List<String> getParalleledSubProcessor() {
        return paralleledSubProcessor;
    }

    public void setParalleledSubProcessor(List<String> paralleledSubProcessor) {
        this.paralleledSubProcessor = paralleledSubProcessor;
    }

    public TaskChain getSubChain() {
        return subChain;
    }

    public void setSubChain(TaskChain subChain) {
        this.subChain = subChain;
    }
}
