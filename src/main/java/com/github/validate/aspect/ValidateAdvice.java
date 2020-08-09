package com.github.validate.aspect;


import com.github.validate.annotation.RequestEntity;
import com.github.validate.annotation.RequestMap;
import com.github.validate.annotation.Verify;
import com.github.validate.exception.MyAssert;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 12:42
 */
public class ValidateAdvice implements MethodInterceptor {

    /**
     * 校验的类型
     */
    private static final String LINK_HASH_MAP_TYPE = "java.util.LinkedHashMap";

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //获取方法
        Method method = invocation.getMethod();
        //获取参数上的所有注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        //获取参数列表
        Object[] args = method.getParameters();
        //判断是否加了RequestMap注解
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            //获取当前参数的位置
            int index = indexOf(parameterAnnotations, parameterAnnotation);
            for (Annotation annotation : parameterAnnotation) {
                //获取此注解修饰的具体的参数
                Object param = args[index];
                //如果有@RequestEntity注解
                hasRequestEntity(annotation, param);
                //如果有Verify注解 由于是参数上的注解 注意：此处传递的是具体的param 而非args
                hasVerify(annotation, param);
                //如果有RequestMap注解  由于是参数上的注解  注意：此处传递的是具体的param 而非args
                hasRequestMap(annotation, param);
            }
        }


        //执行方法
        return invocation.proceed();
    }

    /**
     * 如果参数存在RequestEntity注解
     *
     * @param annotation 参数上的注解
     * @param param      具体的参数
     */
    private void hasRequestEntity(Annotation annotation, Object param) throws Exception {
        //获取注解的全类名
        String requestEntityName = RequestEntity.class.getName();
        //获取当前注解的全类名
        String name = annotation.annotationType().getName();
        //匹配是否相同
        if (requestEntityName.equals(name)) {
            //获取参数的字节码
            Class clazz = param.getClass();
            //获取当前参数对应类型的所有属性
            Field[] fields = clazz.getDeclaredFields();
            //遍历属性
            for (Field field : fields) {
                //获取私有属性值
                field.setAccessible(true);
                //需要做校验的参数
                if (field.isAnnotationPresent(Verify.class)) {
                    //获取注解对象
                    Verify verify = field.getAnnotation(Verify.class);
                    //校验的对象
                    Object fieldObj = field.get(param);
                    //校验
                    validate(verify, fieldObj);
                }
            }
        }
    }


    /**
     * 如果参数上加的是Verify注解
     *
     * @param annotation 参数上的注解
     * @param param      参数
     */
    private void hasVerify(Annotation annotation, Object param) throws Exception {
        //获取注解的全类名
        String verifyName = Verify.class.getName();
        //获取当前注解的全类名
        String name = annotation.annotationType().getName();
        //匹配是否相同
        if (verifyName.equals(name)) {
            //获取此注解修饰的具体的参数
            //获取当前注解的具值
            Verify verify = (Verify) annotation;
            //进行校验
            validate(verify, param);
        }
    }

    /**
     * 判断是否加了@RequestMap注解 加了再进行下一步的操作
     *
     * @param annotation 所有参数前的注解
     * @param param      当前参数
     */
    private void hasRequestMap(Annotation annotation, Object param) throws Exception {
        //获取注解的全类名
        String RequestMapName = RequestMap.class.getName();
        //获取当前注解的全类名
        String name = annotation.annotationType().getName();
        //匹配是否相同
        if (RequestMapName.equals(name)) {
            //如果存在此注解，执行方法
            isLinkedHashMap(annotation, param);
        }
    }

    /**
     * 判断是否为LinkedHashMap，如果是，进行进一步的操作
     *
     * @param annotation 参数上的注解
     * @param param      注解所修饰的参数
     */
    private void isLinkedHashMap(Annotation annotation, Object param) throws Exception {
        //获取注解
        RequestMap RequestMap = (RequestMap) annotation;
        //获取要校验的所有entity
        String[] entitys = RequestMap.baseEntityList();
        //如果是map接收参数
        if (LINK_HASH_MAP_TYPE.equals(param.getClass().getName())) {
            //如果存在Verify注解
            hasVerify(entitys, param);
        }
    }

    /**
     * 如果EntityList中的实体存在Verify注解
     *
     * @param entityList 实体列表
     * @param param      加入@RequestMap的注解 的参数
     */
    private void hasVerify(String[] entityList, Object param) throws Exception {

        //迭代entityList
        for (String entity : entityList) {
            //获取所有字段
            Field[] fields = Class.forName(entity).getDeclaredFields();
            //迭代字段
            for (Field field : fields) {
                field.setAccessible(true);
                //判断是否加入了Verify注解
                if (field.isAnnotationPresent(Verify.class)) {
                    //如果有 获取注解的实例
                    Verify verify = field.getAnnotation(Verify.class);
                    //校验
                    fieldIsNeedValidate(param, verify, field.getName());
                }
            }
        }
    }

    /**
     * 字段是否需要校验
     *
     * @param param     增加@RequestMap注解的参数
     * @param verify    Verify注解的实例
     * @param fieldName 加了Verify的属性name值
     */
    private void fieldIsNeedValidate(Object param, Verify verify, String fieldName) throws Exception {
        //获取集合
        LinkedHashMap map = (LinkedHashMap) param;
        //获取key列表
        Set set = map.keySet();
        //迭代key
        for (Object key : set) {
            //如果key和注解的fieldName一致
            if (fieldName.equals(key)) {
                //当前值
                Object fieldObj = map.get(key);
                //真正的进行校验
                validate(verify, fieldObj);
            }
        }
    }


    /**
     * 正则的校验方法
     *
     * @param verify   校验规则
     * @param fieldObj 校验者
     */
    private void validate(Verify verify, Object fieldObj) throws Exception {
        //获取verify的name
        String name = verify.name();
        //是否时必传 断言判断
        if (verify.required()) {
            MyAssert.assertMethod(fieldObj != null, String.format("【%s】为必传参数", name));
        }
        //字符串的 非空校验
        if (verify.notNull()) {
            MyAssert.assertMethod(!(fieldObj == null || "".equals(fieldObj)), String.format("【%s】不能为空", name));
        }
        //是否有最大长度限制 断言判断
        int maxLength = verify.maxLength();
        if (Integer.MAX_VALUE != maxLength) {
            MyAssert.assertMethod(maxLength > String.valueOf(fieldObj).length(), String.format("【%s】长度不合理，最大长度为【%s】", name, maxLength));
        }
        //是否有最小长度限制 断言判断
        int minLength = verify.minLength();
        if (Integer.MIN_VALUE != minLength) {
            MyAssert.assertMethod(minLength < String.valueOf(fieldObj).length(), String.format("【%s】长度不合理，最小长度为【%s】", name, minLength));
        }
        //是否有正则校验
        if (!"".equals(verify.regular().getRegex())) {
            //初始化Pattern
            Pattern pattern = Pattern.compile(verify.regular().getRegex());
            //断言判断正则
            MyAssert.assertMethod(pattern.matcher(String.valueOf(fieldObj)).matches(), String.format("参数【%s】的请求数据不符合规则", name));
        }
    }

    /**
     * hutool中的方法
     */
    private  <T> int indexOf(T[] array, Object value) {
        if (null != array) {
            for(int i = 0; i < array.length; ++i) {
                if (equal(value, array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * hutool中的方法
     */
    private boolean equal(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

}
