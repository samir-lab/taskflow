package org.samir.tools.taskflow;

/**
 * @author Samir
 * @date 2019/12/17 11:19
 */
public interface TaskTopology {

    /**
     * simple chain task
     */
    void runSimpleChain() throws Exception;

    /**
     * simple chain task whit option
     */
    void runSimpleChain(boolean checkResult) throws Exception;

    /**
     * tree chain task
     */
    void runTreeChain() throws Exception;
}
