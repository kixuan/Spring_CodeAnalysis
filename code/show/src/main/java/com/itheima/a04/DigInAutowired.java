package com.itheima.a04;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.StandardEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// AutowiredAnnotationBeanPostProcessor 运行分析
public class DigInAutowired {
    public static void main(String[] args) throws Throwable {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("bean2", new Bean2()); // 创建过程,依赖注入,初始化
        beanFactory.registerSingleton("bean3", new Bean3());
        beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver()); // @Value
        beanFactory.addEmbeddedValueResolver(new StandardEnvironment()::resolvePlaceholders); // ${} 的解析器

        // 1. 查找哪些属性、方法加了 @Autowired, 这称之为 InjectionMetadata
        // AutowiredAnnotationBeanPostProcessor 会在 BeanPostProcessor 中执行，所以需要将其注册到 BeanFactory 中
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(beanFactory);

        Bean1 bean1 = new Bean1();
        System.out.println(bean1);
        processor.postProcessProperties(null, bean1, "bean1"); // 执行依赖注入 @Autowired @Value
        System.out.println(bean1);

        // postProcessProperties的具体实现：
        //  1. findAutowiringMetadata
        //  2. InjectionMetadata.inject    InjectionMetadata 封装了加了@Autowired的Bean的信息
        // Method findAutowiringMetadata = AutowiredAnnotationBeanPostProcessor.class.getDeclaredMethod("findAutowiringMetadata", String.class, Class.class, PropertyValues.class);
        // findAutowiringMetadata.setAccessible(true);
        // InjectionMetadata metadata = (InjectionMetadata) findAutowiringMetadata.invoke(processor, "bean1", Bean1.class, null);// 获取 Bean1 上加了 @Value @Autowired 的成员变量，方法参数信息
        // System.out.println(metadata);

        //  2. 调用 InjectionMetadata 来进行依赖注入, 注入时按类型查找值
        // metadata.inject(bean1, "bean1", null);
        // System.out.println(bean1);

        // 3. 如何按类型查找值：也就是inject方法的具体实现
        // 3.1 成员变量（Field）：通过反射获取bean3字段，然后调用beanFactory.doResolveDependency解析bean3的依赖
        Field bean3 = Bean1.class.getDeclaredField("bean3");
        DependencyDescriptor dd1 = new DependencyDescriptor(bean3, false);
        Object o = beanFactory.doResolveDependency(dd1, null, null, null);  // 查找bean3的值
        System.out.println(o);

        // 3.2 成员方法  根据方法参数的类型去容器中查找：通过反射获取setBean2方法，然后调用beanFactory.doResolveDependency解析bean2的依赖
        Method setBean2 = Bean1.class.getDeclaredMethod("setBean2", Bean2.class);
        DependencyDescriptor dd2 =
                new DependencyDescriptor(new MethodParameter(setBean2, 0), true);
        Object o1 = beanFactory.doResolveDependency(dd2, null, null, null);
        System.out.println(o1);

        // 3.3 @Value：通过反射获取setHome方法，然后调用beanFactory.doResolveDependency解析 @Value 的依赖
        Method setHome = Bean1.class.getDeclaredMethod("setHome", String.class);
        DependencyDescriptor dd3 = new DependencyDescriptor(new MethodParameter(setHome, 0), true);
        Object o2 = beanFactory.doResolveDependency(dd3, null, null, null);
        System.out.println(o2);

    }
}
