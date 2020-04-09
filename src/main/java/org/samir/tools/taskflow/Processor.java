package org.samir.tools.taskflow;

/**
 * @author Samir
 * @date 2019/12/17 17:57
 */
public interface Processor<T, V> {

    /**
     * process
     * @param params
     * @param result
     * @return true or false means run next task or not (simple chain)
     */
    boolean process(T params, V result);

}
