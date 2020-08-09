package cn.rayfoo.validate.config;

import cn.rayfoo.validate.annotation.EnableVerify;
import cn.rayfoo.validate.aspect.ValidateAdvice;
import cn.rayfoo.validate.exception.ServerExceptionResolver;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 13:37
 */
public class EnableVerifyConfig implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {


        /*
         * 注册Aspect
         */

        //获取启动器上的注解
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableVerify.class.getName()));

        //获取自定义注解中的属性
        String execution = annoAttrs.getString("execution");

        //创建Bean
        BeanDefinition validateAdvice = new GenericBeanDefinition();

        //指定Bean的字节码
        validateAdvice.setBeanClassName(AspectJExpressionPointcutAdvisor.class.getName());
        MutablePropertyValues validateAdviceProp = validateAdvice.getPropertyValues();

        //设置Bean的属性
        validateAdviceProp.addPropertyValue("advice", new ValidateAdvice());
        validateAdviceProp.addPropertyValue("expression", execution);

        //注册bean
        registry.registerBeanDefinition("validateAdvice", validateAdvice);


        /*
         * 注册全局异常处理  当包名为cn.rayfoo时，无需添加此内容
         */
        //创建Bean
        BeanDefinition serverExceptionResolver = new GenericBeanDefinition();
        //指定Bean的字节码
        serverExceptionResolver.setBeanClassName(ServerExceptionResolver.class.getName());
        //注册bean
        registry.registerBeanDefinition("serverExceptionResolver", serverExceptionResolver);


    }

}
