package com.appcnd.potato.generate.executor;

import com.appcnd.potato.generate.context.FrontContext;

/**
 * @author nihao 2021/07/24
 */
public abstract class FrontExecutor extends Executor {
    protected FrontContext context;

    public FrontExecutor(FrontContext context) {
        this.context = context;
    }

}
