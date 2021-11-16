package cn.thecover.potato.generate.executor.handler;

import cn.thecover.potato.generate.executor.ComponentExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nihao 2021/11/03
 */
public abstract class ComponentHandler {
    private List<ComponentHandler> nexts;

    public void setNext(ComponentHandler next) {
        if (nexts == null) {
            nexts = new ArrayList<>();
        }
        nexts.add(next);
    }

    protected abstract List<ComponentExecutor.El> handler(HandlerRequest request);

    public void execute(HandlerRequest request) {
        List<ComponentExecutor.El> elList = handler(request);
        if (elList != null && !elList.isEmpty()) {
            if (request.getEls() == null) {
                request.setEls(elList);
            } else {
                request.getEls().addAll(elList);
            }
        }

        if (nexts != null && !nexts.isEmpty()) {
            for (ComponentHandler next : nexts) {
                next.execute(request);
            }
        }
    }
}
