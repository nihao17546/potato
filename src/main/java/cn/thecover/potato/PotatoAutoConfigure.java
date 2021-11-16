package cn.thecover.potato;

import cn.thecover.potato.aop.ExceptionHandlerAop;
import cn.thecover.potato.config.BeanConfig;
import cn.thecover.potato.config.PotatoDataSourceAutoConfigure;
import cn.thecover.potato.generate.boot.GenerateBoot;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.SpringContextUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author nihao 2021/11/14
 */
@Configuration
@ConditionalOnProperty("spring.potato.path")
@EnableConfigurationProperties({CoreProperties.class})
@Import({PotatoDataSourceAutoConfigure.class, BeanConfig.class, SpringContextUtil.class, ExceptionHandlerAop.class,
        GenerateBoot.class})
public class PotatoAutoConfigure {
}
