package com.itheima.a02;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.annotation.Resource;

public class TestBeanFactory {

    public static void main(String[] args) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        // bean 的定义（class, scope, 初始化, 销毁）
        // 1. 通过 BeanDefinitionBuilder 构建 BeanDefinition：Config 单例
        AbstractBeanDefinition beanDefinition =
                BeanDefinitionBuilder.genericBeanDefinition(Config.class).setScope("singleton").getBeanDefinition();
        // 2. 把 Config 注册到 BeanFactory
        beanFactory.registerBeanDefinition("config", beanDefinition);

        // 给 BeanFactory 添加一些常用的后处理器
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);

        // 把所有的 BeanFactory 后处理器(BeanFactoryPostProcessor) 都拿到，然后调用 postProcessBeanFactory 方法
        // 补充了一些 bean 定义，例如 @Autowired 的解析
        beanFactory.getBeansOfType(BeanFactoryPostProcessor.class).values().forEach(beanFactoryPostProcessor -> {
            beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
        });

        // Bean 后处理器, 针对 bean 的生命周期的各个阶段提供扩展, 例如 @Autowired、@Resource
        beanFactory.getBeansOfType(BeanPostProcessor.class).values().stream()
                .sorted(beanFactory.getDependencyComparator())   // 比较器排序，设置优先级，比如说加了之后就是优先级Resource > Autowired
                .forEach(beanPostProcessor -> {
                    System.out.println(">>>>" + beanPostProcessor);  // 验证优先级：internalAutowiredAnnotationProcessor(@Autowired) > internalCommonAnnotationProcessor(@Resource)
                    beanFactory.addBeanPostProcessor(beanPostProcessor); //建立bean工厂和后置处理器的联系
                });

        for (String name : beanFactory.getBeanDefinitionNames()) {
            System.out.println(name);
        }

        beanFactory.preInstantiateSingletons(); // 提前准备好所有单例，而不是等到要用的时候才去创建【默认】
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        // 直接打印的话bean2是null，因为bean2还没有初始化，依赖注入还需要其他bean处理器，处理@Autowire
//        System.out.println(beanFactory.getBean(Bean1.class).getBean2());
        System.out.println(beanFactory.getBean(Bean1.class).getInter());
        /*
            学到了什么:
            a. beanFactory 不会做的事    --applicationContext 就会完成这些事情
                   1. 不会主动调用 BeanFactory 后处理器
                   2. 不会主动添加 Bean 后处理器
                   3. 不会主动初始化单例
                   4. 不会解析beanFactory 还不会解析 ${ } 与 #{ }
            b. bean 后处理器会有排序的逻辑，通过order接口实现
         */

        // 打印不同后置处理器的优先级
        System.out.println("Common:" + (Ordered.LOWEST_PRECEDENCE - 3));
        System.out.println("Autowired:" + (Ordered.LOWEST_PRECEDENCE - 2));
    }

    @Configuration
    static class Config {
        @Bean
        public Bean1 bean1() {
            return new Bean1();
        }

        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }

        @Bean
        public Bean3 bean3() {
            return new Bean3();
        }

        @Bean
        public Bean4 bean4() {
            return new Bean4();
        }
    }

    interface Inter {

    }

    static class Bean3 implements Inter {

    }

    static class Bean4 implements Inter {

    }

    static class Bean1 {
        private static final Logger log = LoggerFactory.getLogger(Bean1.class);

        public Bean1() {
            log.debug("构造 Bean1()");
        }

        @Autowired
        private Bean2 bean2;

        public Bean2 getBean2() {
            return bean2;
        }

        @Autowired
        @Resource(name = "bean4")
        private Inter bean3;

        public Inter getInter() {
            return bean3;
        }
    }

    static class Bean2 {
        private static final Logger log = LoggerFactory.getLogger(Bean2.class);

        public Bean2() {
            log.debug("构造 Bean2()");
        }
    }
}
