package com.appcnd.potato.generate.boot.executor;

import com.appcnd.potato.dao.MetaDao;
import com.appcnd.potato.generate.boot.BootResult;
import com.appcnd.potato.generate.boot.GenerateBoot;
import com.appcnd.potato.model.po.Meta;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.service.IGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author nihao 2022/01/08
 */
@Slf4j
public class BootExecutor {
    @Autowired
    private IGenerateService generateService;
    @Autowired
    private MetaDao metaDao;
    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private GenerateBoot generateBoot;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newSingleThreadExecutor();
        List<Meta> metaList = metaDao.selectList(null, true, null, null);
        if (metaList != null && !metaList.isEmpty()) {
            for (Meta meta : metaList) {
                execute(meta, ExecutorType.BOOT);
            }
        }
        if (Boolean.TRUE.equals(coreProperties.getCluster())) {
            log.info("集群部署，执行定时扫描更新任务");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    List<Meta> metaList = metaDao.selectList(null, null, null, null);
                    if (metaList != null && !metaList.isEmpty()) {
                        for (Meta meta : metaList) {
                            try {
                                if (meta.getLoaded()) {
                                    BootResult bootResult = generateBoot.getLoaded(meta.getId());
                                    if (bootResult == null) {
                                        // 加载
                                        execute(meta, ExecutorType.BOOT);
                                    } else if (!bootResult.getVersion().equals(meta.getVersion())) {
                                        // 更新
                                        execute(meta, ExecutorType.RE_BOOT);
                                    }
                                } else {
                                    BootResult bootResult = generateBoot.getLoaded(meta.getId());
                                    if (bootResult != null) {
                                        // 卸载
                                        execute(meta, ExecutorType.UN_BOOT);
                                    }
                                }
                            } catch (Exception e) {
                                log.error("定时任务中加载异常 {}", meta.getId(), e);
                            }
                        }
                    }
                }
            }, 10000L, 5000L);
        }
    }

    public void execute(Meta meta, ExecutorType type) {
        BootThread bootThread = new BootThread(meta, generateService, type);
        executorService.execute(bootThread);
    }

}
