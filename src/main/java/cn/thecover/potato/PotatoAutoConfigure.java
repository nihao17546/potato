package cn.thecover.potato;

import cn.thecover.potato.aop.ExceptionHandlerAop;
import cn.thecover.potato.config.BeanConfig;
import cn.thecover.potato.config.PotatoDataSourceAutoConfigure;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.properties.DbProperties;
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
@EnableConfigurationProperties({CoreProperties.class, DbProperties.class})
@Import({PotatoDataSourceAutoConfigure.class, BeanConfig.class, SpringContextUtil.class, ExceptionHandlerAop.class})
public class PotatoAutoConfigure {
    public PotatoAutoConfigure() {
        System.out.println(" ____  ____  _____  ____  _____  ____ \n" +
                        "/  __\\/  _ \\/__ __\\/  _ \\/__ __\\/  _ \\\n" +
                        "|  \\/|| / \\|  / \\  | / \\|  / \\  | / \\|\n" +
                        "|  __/| \\_/|  | |  | |-||  | |  | \\_/|\n" +
                        "\\_/   \\____/  \\_/  \\_/ \\|  \\_/  \\____/\n" +
                        " v1.0.0        https://www.appcnd.com\n");
    }
}
