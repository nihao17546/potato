package com.appcnd.potato.generate.boot.executor;

import com.appcnd.potato.model.po.Meta;
import com.appcnd.potato.service.IGenerateService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nihao 2022/01/08
 */
@Slf4j
public class BootThread implements Runnable {
    private ExecutorType boot;
    private Meta meta;
    private IGenerateService generateService;

    BootThread(Meta meta, IGenerateService generateService, ExecutorType boot) {
        this.meta = meta;
        this.generateService = generateService;
        this.boot = boot;
    }

    @Override
    public void run() {
        if (boot == ExecutorType.BOOT) {
            boot();
        } else if (boot == ExecutorType.UN_BOOT) {
            unBoot();
        } else if (boot == ExecutorType.RE_BOOT) {
            synchronized (meta.getId().toString().intern()) {
                unBoot();
                boot();
            }
        }
    }

    private void unBoot() {
        log.info("开始卸载{}", meta.getId());
        try {
            generateService.unBoot(meta.getId(), meta.getVersion());
            log.info("卸载成功 {}", meta.getId());
        } catch (Exception e) {
            log.error("卸载{}异常", meta.getId(), e);
        }
    }

    private void boot() {
        log.info("开始加载{}", meta.getId());
        try {
            generateService.boot(meta.getId(), meta.getVersion());
            log.info("加载成功 {}", meta.getId());
        } catch (Exception e) {
            log.error("加载{}异常", meta.getId(), e);
        }
    }
}
