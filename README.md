# **param-validate**的使用



## 下载

如果你只想使用此工具，可以前往github下载其jar包。[点击进入下载页面](https://github.com/18338862369/param-validate/releases)

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200810141322.png)



## 解压和安装

解压后，你将会得到下述两个文件

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200810141418.png)

运行install.bat(请确保你的M2_HOME是否正确配置)



## 使用**param-validate**

接下来我们创建一个maven项目，引入

```xml
        <dependency>
            <groupId>com.github.18338862369</groupId>
            <artifactId>param-validate</artifactId>
            <version>1.0.0</version>
        </dependency>
```

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200810141609.png)

可以看到其关联了SpringBoot-web启动器依赖、aspectj依赖和lombok依赖，如果版本与你的程序冲突，请手动将其所关联的依赖排除，但是请确保项目中引入了相关的依赖。



## 注解介绍

完成了上面的操作，就可以使用param-validate了，在使用之前，我们先了解一下它的四个注解：

#### @Verify注解

标注在参数、字段上，用于标注需要校验的数据以及其校验的规则。

- name：描述，当校验结果为false时，返回用户提示信息时使用
- maxLength：最大长度，用于判断字符串类型，如果时默认值代表不进行判断
- minLength：最小长度，用于判断字符串类型，如果时默认值代表不进行判断
- required：是否为必填属性
- notNull：是否允许为空
- regular：是否需要进行正则校验，如果需要正则内容是什么，默认为不进行正则校验
- isEntity：是否是一个entity，如果是，递归调用判断其内的属性



#### @RequestMap注解

用于标注Controller方法的参数，用于指定Map类型参数。

- baseEntityList制定Entity的全类名，如果Entity的属性名和key一致，就进行参数校验，校验规则由Entity中加了@Verify注解的属性来决定



#### @RequestEntity注解

用法同上，不过其标注的是Entity类型。



#### @EnableVerify注解

用于标注在SpringBoot启动类

- execution：指定execution表达式，只有execution标识的方法才会进行全局参数校验



## 创建测试环境



### 创建启动类

在启动类上加入`@EnableVerify(execution = "execution(* cn.giao.web..*(..))")`注解

```java
package cn.giao;

import com.github.validate.annotation.EnableVerify;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 16:05
 */
@SpringBootApplication
@EnableVerify(execution = "execution(* cn.giao.web..*(..))")
public class Runner {

    public static void main(String[] args) {
        SpringApplication.run(Runner.class, args);
    }

}

```





### 创建一个java实体

在实体上引入@Verify注解，标注校验的规则：

```java
package cn.giao.entity;

import com.github.validate.annotation.Verify;
import com.github.validate.enums.RegexOption;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * @date 2020/8/3 19:03
 * @description 用户实体
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = -1840831686851699943L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名
     */
    @Verify(name = "用户名", regular = RegexOption.USERNAME_REGEX)
    private String username;

    /**
     * 加密后的密码
     */
    @Verify(name = "密码", regular = RegexOption.PASSWORD_REGEX)
    private String password;

    /**
     * 加密使用的盐
     */
    private String salt;

    /**
     * 邮箱
     */
    @Verify(name = "邮箱", regular = RegexOption.EMAIL_REGEX)
    private String email;

    /**
     * 手机号码
     */
    @Verify(name = "手机号", regular = RegexOption.PHONE_NUMBER_REGEX)
    private String phoneNumber;

    /**
     * 状态，-1：逻辑删除，0：禁用，1：启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上次登录时间
     */
    private Date lastLoginTime;

    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;

    @Verify(name = "用户", isEntity = true, notNull = false)
    private User user;

}

```



### 创建Controller类测试



```java
package cn.giao.web;

import cn.giao.entity.User;
import com.github.validate.annotation.RequestEntity;
import com.github.validate.annotation.RequestMap;
import com.github.validate.annotation.Verify;
import com.github.validate.enums.RegexOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 16:12
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test(@Verify(name = "用户名",regular = RegexOption.USERNAME_REGEX)String username){
        return "validate success";
    }

    @PostMapping("/test")
    public String test(@RequestBody @RequestEntity User user){
        return "validate success";
    }

    @PostMapping("/user")
    public String test(@RequestBody @RequestMap(baseEntityList = {"cn.giao.entity.User"}) Map user){
        return "validate success";
    }

}
```



### postman测试

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200810142421.png)



https://github.com/18338862369/param-validate页面有完整的源码，如果这工具对你有帮助，请帮我点个star哦

