# param-validate
> 在开发过程中，用户传递的数据不一定合法，虽然可以通过前端进行一些校验，但是为了确保程序的安全性，保证数据的合法，在后台进行数据校验也是十分必要的。

## 后台的参数校验的解决方案

### 在controller方法中校验：

后台的参数是通过controller方法获取的，所以最简单的参数校验的方法，就是在controller方法中进行参数校验。在controller方法中如果进行参数校验会有大量重复、没有太大意义的代码。



### 使用拦截器、过滤器校验

为了保证controller中的代码有更好的可读性，可以将参数校验的工作交由拦截器（Interceptor）或者过滤器（Filter）来完成，但是此时又存在一个问题：非共性的参数需要每个方法都创建一个与之对应的拦截器（或者过滤器）。



### 实现对Entity的统一校验

鉴于上述解决方案的缺点，我们可以借助AOP的思想来进行统一的参数校验。思想是通过自定义注解来完成对实体类属性的标注，在AOP中扫描加了自定义注解的属性，对其进行注解属性标注的校验。对于不满足的参数直接抛出自定义异常，交由全局异常处理来处理并返回友好的提示信息。



## 认识Param-Validate

以上的解决方案各有优劣，param-validate采用了AOP+注解的形式解决了统一参数的校验。使用四个注解可以解决大部分的参数校验，有效的简化了代码、提高了程序的可读性和开发效率。



Param-Validate基于SpringBoot2.3.0.RELEASE，所以请保证你的SpringBoot版本和其一致，如果必须使用其他版本，可以将其内包含的SpringBoot依赖排除。



### 依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.github.18338862369</groupId>
    <artifactId>param-validate</artifactId>
    <version>1.0.0</version>
    <description>SpringBoot ParamValidate</description>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
        <aspectjweaver.version>1.9.5</aspectjweaver.version>
        <lombok.version>1.18.12</lombok.version>
    </properties>


    <dependencies>
        <!--web内置了jackson-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- aspect -->
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectjweaver.version}</version>
        </dependency>
        <!-- lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
<!--            <optional>true</optional>-->
            <version>${lombok.version}</version>
        </dependency>
    </dependencies>

</project>
```



### 项目结构

下图是项目的结构图。接下来，我们逐个包来介绍一下

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200809205334.png)





### response包

此包中定义的内容为用户相应的类

#### HttpStatus枚举

这个枚举是从Spring源码中拿到，并且拓展的一个枚举。其封装了很多常见的状态码和状态信息。

```java
package cn.rayfoo.validate.response;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * @date 2020/8/5 11:37
 * @description
 */
public enum HttpStatus {
    /**
     * 登录成功
     * {@code 600 Login Success}.
     */
    LOGIN_SUCCESS(600, "Login Success" ),


    /**
     * 重新登录
     * {@code 605 Login Success}.
     */
    LOGIN_AGAIN(605, "Login AGAIN" ),

    /**
     * 登录超时
     * {@code 601 Login Out Time}.
     */
    LOGIN_OUT_TIME(601, "Login Out Time" ),
    /**
     * 用户无访问权限
     * {@code 602 Not Roles}.
     */
    NOT_ROLES(602, "Not Roles" ),
    /**
     * 用户未注册
     * {@code 603 Not Register}.
     */
    NOT_REGISTER(603, "Not Register未找到该账号" ),

    /**
     * 用户未注册
     * {@code 604 AuthenticationExcption}.
     */
    AUTHENTICATION_EXCPTION(604, "身份认证错误" ),
    /**
     * 未知的账号异常
     * {@code 606 Unknown Account}.
     */
    UNKNOWN_ACCOUNT(606, "账号已注销" ),
    /**
     * 请求中的参数有误
     * {@code 705 Parameter Error}.
     */
    PARAMETER_ERROR(705, "Parameter Error" ),

    /**
     * 验证码错误
     * {@code 704 Invalid Captcha}.
     */
    INVALID_CAPTCHA(704, "Invalid Captcha验证码错误" ),

    /**
     * 邮箱和手机号验证错误
     */
    EMAIL_OR_PHONE_ERROR(703, "email or phone error" ),

    /**
     * 用户已激活
     */
    HAS_BEEN_ACTIVATED(702, "has been activated" ),

    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(700, "username or password error用户名或密码错误" ),

    /**
     * 用户未启用
     */
    USER_NOT_ENABLED(699, "user not enabled" ),

    /**
     * 验证码错误
     */
    ACTIVATION_CODE_ERROR(698, "Activation code error" ),

    /**
     * 用户名被占用
     */
    USERNAME_IS_OCCUPIED(697, "Username is occupied" ),

    /**
     * 返回无数据
     * {@code 706 not data}.
     */
    NOT_DATA(706, "not data" ),

    /**
     * 流程操作成功
     * {@code 710 Successful operation}.
     */
    SUCCESSFUL_OPERATION(710, "Successful operation" ),

    /**
     * 数据冲突，无法存储！
     */
    DATA_CONFLICT(711, "Data conflict" ),

    /**
     * 删除操作失败
     */
    CANNOT_DELETE(712, "Cannot delete" ),

    /**
     * 操作失败
     * {@code 720 operation failed}.
     */
    FAILED_OPERATION(720, "operation failed" ),

    NOT_VTASKSTATUS(709, "not vtaskstatus" ),

    // 1xx Informational

    /**
     * {@code 100 Continue}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.1">HTTP/1.1</a>
     */
    CONTINUE(100, "Continue" ),
    /**
     * {@code 101 Switching Protocols}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.1.2">HTTP/1.1</a>
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols" ),
    /**
     * {@code 102 Processing}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
     */
    PROCESSING(102, "Processing" ),
    /**
     * {@code 103 Checkpoint}.
     *
     * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
     * resumable POST/PUT HTTP requests in HTTP/1.0</a>
     */
    CHECKPOINT(103, "Checkpoint" ),

    // 2xx Success

    /**
     * {@code 200 OK}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.1">HTTP/1.1</a>
     */
    OK(200, "OK" ),
    /**
     * {@code 201 Created}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.2">HTTP/1.1</a>
     */
    CREATED(201, "Created" ),
    /**
     * {@code 202 Accepted}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.3">HTTP/1.1</a>
     */
    ACCEPTED(202, "Accepted" ),
    /**
     * {@code 203 Non-Authoritative Information}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.4">HTTP/1.1</a>
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information" ),
    /**
     * {@code 204 No Content}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.5">HTTP/1.1</a>
     */
    NO_CONTENT(204, "No Content" ),
    /**
     * {@code 205 Reset Content}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.6">HTTP/1.1</a>
     */
    RESET_CONTENT(205, "Reset Content" ),
    /**
     * {@code 206 Partial Content}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.2.7">HTTP/1.1</a>
     */
    PARTIAL_CONTENT(206, "Partial Content" ),
    /**
     * {@code 207 Multi-Status}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
     */
    MULTI_STATUS(207, "Multi-Status" ),
    /**
     * {@code 208 Already Reported}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
     */
    ALREADY_REPORTED(208, "Already Reported" ),
    /**
     * {@code 226 IM Used}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
     */
    IM_USED(226, "IM Used" ),

    // 3xx Redirection

    /**
     * {@code 300 Multiple Choices}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.1">HTTP/1.1</a>
     */
    MULTIPLE_CHOICES(300, "Multiple Choices" ),
    /**
     * {@code 301 Moved Permanently}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.2">HTTP/1.1</a>
     */
    MOVED_PERMANENTLY(301, "Moved Permanently" ),
    /**
     * {@code 302 Found}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.3">HTTP/1.1</a>
     */
    FOUND(302, "Found" ),
    /**
     * {@code 302 Moved Temporarily}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0</a>
     * @deprecated In favor of {@link #FOUND} which will be returned from {@code HttpStatus.valueOf(302)}
     */
    @Deprecated
    MOVED_TEMPORARILY(302, "Moved Temporarily" ),
    /**
     * {@code 303 See Other}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.4">HTTP/1.1</a>
     */
    SEE_OTHER(303, "See Other" ),
    /**
     * {@code 304 Not Modified}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.5">HTTP/1.1</a>
     */
    NOT_MODIFIED(304, "Not Modified" ),
    /**
     * {@code 305 Use Proxy}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.6">HTTP/1.1</a>
     */
    USE_PROXY(305, "Use Proxy" ),
    /**
     * {@code 307 Temporary Redirect}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.3.8">HTTP/1.1</a>
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect" ),
    /**
     * {@code 308 Resume Incomplete}.
     *
     * @see <a href="http://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
     * resumable POST/PUT HTTP requests in HTTP/1.0</a>
     */
    RESUME_INCOMPLETE(308, "Resume Incomplete" ),

    // --- 4xx Client Error ---

    /**
     * {@code 400 Bad Request}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.1">HTTP/1.1</a>
     */
    BAD_REQUEST(400, "Bad Request" ),
    /**
     * {@code 401 Unauthorized}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.2">HTTP/1.1</a>
     */
    UNAUTHORIZED(401, "Unauthorized" ),
    /**
     * {@code 402 Payment Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.3">HTTP/1.1</a>
     */
    PAYMENT_REQUIRED(402, "Payment Required" ),
    /**
     * {@code 403 Forbidden}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.4">HTTP/1.1</a>
     */
    FORBIDDEN(403, "Forbidden" ),
    /**
     * {@code 404 Not Found}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.5">HTTP/1.1</a>
     */
    NOT_FOUND(404, "Not Found" ),
    /**
     * {@code 405 Method Not Allowed}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.6">HTTP/1.1</a>
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed" ),
    /**
     * {@code 406 Not Acceptable}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.7">HTTP/1.1</a>
     */
    NOT_ACCEPTABLE(406, "Not Acceptable" ),
    /**
     * {@code 407 Proxy Authentication Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.8">HTTP/1.1</a>
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required" ),
    /**
     * {@code 408 Request Timeout}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.9">HTTP/1.1</a>
     */
    REQUEST_TIMEOUT(408, "Request Timeout" ),
    /**
     * {@code 409 Conflict}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.10">HTTP/1.1</a>
     */
    CONFLICT(409, "Conflict" ),
    /**
     * {@code 410 Gone}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.11">HTTP/1.1</a>
     */
    GONE(410, "Gone" ),
    /**
     * {@code 411 Length Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.12">HTTP/1.1</a>
     */
    LENGTH_REQUIRED(411, "Length Required" ),
    /**
     * {@code 412 Precondition failed}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.13">HTTP/1.1</a>
     */
    PRECONDITION_FAILED(412, "Precondition Failed" ),
    /**
     * {@code 413 Request entity Too Large}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1</a>
     */
    REQUEST_ENTITY_TOO_LARGE(413, "Request entity Too Large" ),
    /**
     * {@code 414 Request-URI Too Long}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1</a>
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long" ),
    /**
     * {@code 415 Unsupported Media Type}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.16">HTTP/1.1</a>
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type" ),
    /**
     * {@code 416 Requested Range Not Satisfiable}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.17">HTTP/1.1</a>
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable" ),
    /**
     * {@code 417 Expectation Failed}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.18">HTTP/1.1</a>
     */
    EXPECTATION_FAILED(417, "Expectation Failed" ),
    /**
     * {@code 418 I'm a teapot}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
     */
    I_AM_A_TEAPOT(418, "I'm a teapot" ),
    /**
     * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
     */
    @Deprecated INSUFFICIENT_SPACE_ON_RESOURCE(419, "Insufficient Space On Resource" ),
    /**
     * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
     */
    @Deprecated METHOD_FAILURE(420, "Method Failure" ),
    /**
     * @deprecated See <a href="http://tools.ietf.org/rfcdiff?difftype=--hwdiff&url2=draft-ietf-webdav-protocol-06.txt">WebDAV Draft Changes</a>
     */
    @Deprecated DESTINATION_LOCKED(421, "Destination Locked" ),
    /**
     * {@code 422 Unprocessable entity}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable entity" ),
    /**
     * {@code 423 Locked}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
     */
    LOCKED(423, "帐号已锁定" ),
    /**
     * {@code 424 Failed Dependency}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
     */
    FAILED_DEPENDENCY(424, "Failed Dependency" ),
    /**
     * {@code 426 Upgrade Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
     */
    UPGRADE_REQUIRED(426, "Upgrade Required" ),
    /**
     * {@code 428 Precondition Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
     */
    PRECONDITION_REQUIRED(428, "Precondition Required" ),
    /**
     * {@code 429 Too Many Requests}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests" ),
    /**
     * {@code 431 Request Header Fields Too Large}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large" ),

    // --- 5xx Server Error ---

    /**
     * {@code 500 Internal Server Error}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.1">HTTP/1.1</a>
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error" ),
    /**
     * {@code 501 Not Implemented}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.2">HTTP/1.1</a>
     */
    NOT_IMPLEMENTED(501, "Not Implemented" ),
    /**
     * {@code 502 Bad Gateway}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.3">HTTP/1.1</a>
     */
    BAD_GATEWAY(502, "Bad Gateway" ),
    /**
     * {@code 503 service Unavailable}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.4">HTTP/1.1</a>
     */
    SERVICE_UNAVAILABLE(503, "service Unavailable" ),
    /**
     * {@code 504 Gateway Timeout}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.5">HTTP/1.1</a>
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout" ),
    /**
     * {@code 505 HTTP Version Not Supported}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.5.6">HTTP/1.1</a>
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported" ),
    /**
     * {@code 506 Variant Also Negotiates}
     *
     * @see <a href="http://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates" ),
    /**
     * {@code 507 Insufficient Storage}
     *
     * @see <a href="http://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage" ),
    /**
     * {@code 508 Loop Detected}
     *
     * @see <a href="http://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
     */
    LOOP_DETECTED(508, "Loop Detected" ),
    /**
     * {@code 509 Bandwidth Limit Exceeded}
     */
    BANDWIDTH_LIMIT_EXCEEDED(509, "Bandwidth Limit Exceeded" ),
    /**
     * {@code 510 Not Extended}
     *
     * @see <a href="http://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
     */
    NOT_EXTENDED(510, "Not Extended" ),
    /**
     * {@code 511 Network Authentication Required}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required" ),
    SMS_CLIENT_EXCEPTION(512, "输入手机号不合法，请重新输入" ),
    SMS_SERVER_EXCEPTION(513, "发送短信出现异常，请稍后重试~" );


    private final int value;

    private final String reasonPhrase;


    private HttpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this status code.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this status code.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Returns the HTTP status series of this status code.
     *
     * @see Series
     */
    public Series series() {
        return Series.valueOf(this);
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }


    /**
     * Return the enum constant of this type with the specified numeric value.
     *
     * @param statusCode the numeric value of the enum to be returned
     * @return the enum constant with the specified numeric value
     * @throws IllegalArgumentException if this enum has no constant for the specified numeric value
     */
    public static HttpStatus valueOf(int statusCode) {
        for (HttpStatus status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + statusCode + "]" );
    }


    /**
     * Java 5 enumeration of HTTP status series.
     * <p>Retrievable via {@link HttpStatus#series()}.
     */
    public static enum Series {

        INFORMATIONAL(1),
        SUCCESSFUL(2),
        REDIRECTION(3),
        CLIENT_ERROR(4),
        SERVER_ERROR(5);

        private final int value;

        private Series(int value) {
            this.value = value;
        }

        /**
         * Return the integer value of this status series. Ranges from 1 to 5.
         */
        public int value() {
            return this.value;
        }

        public static Series valueOf(int status) {
            int seriesCode = status / 100;
            for (Series series : values()) {
                if (series.value == seriesCode) {
                    return series;
                }
            }
            throw new IllegalArgumentException("No matching constant for [" + status + "]" );
        }

        public static Series valueOf(HttpStatus status) {
            return valueOf(status.value);
        }

    }

}

```

#### Result类

此类是一个通用的返回结果类，起初使用了Swagger文档标注，后期单独整合为工具包，为了减少jar包的依赖就删去了Swagger部分。

```java
package cn.rayfoo.validate.response;

import lombok.Builder;
import lombok.Data;


/**
 * @author rayfoo@qq.com
 * @date 2020年8月6日
 */
@Data@Builder
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */

    private String  msg;

    /**
     * 数据记录
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}

```

### exception包

此包内封装了关于异常的定义和处理类



#### MyAssert类

从类名就可以看出，这个类是进行断言判断的类，其内定义了多个重载的断言判断方法，断言方法可以在特定条件下中止方法的运行。

```java
package cn.rayfoo.validate.exception;

import HttpStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p>断言类</p>
 * @date 2020/8/7 9:43
 */
@Slf4j
public class MyAssert {

    /**
     * 如果为空直接抛出异常 类似于断言的思想
     * @param status 当status为false 就会抛出异常 不继续执行后续语句
     * @param msg  异常描述
     */
    public static void assertMethod(boolean status, String msg) throws Exception {
        //为false抛出异常
        if (!status) {
            //记录错误信息
            log.error(msg);
            //抛出异常
            throw MyException.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(msg).build();
        }
    }

    /**
     * 如果为空直接抛出异常 类似于断言的思想
     * @param status 当status为false 就会抛出异常 不继续执行后续语句
     * @param code 状态码
     * @param msg  异常描述
     */
    public static void assertMethod(boolean status,Integer code, String msg) throws Exception {
        //为false抛出异常
        if (!status) {
            //记录错误信息
            log.error(msg);
            //抛出异常
            throw MyException.builder().code(code).msg(msg).build();
        }
    }

    /**
     * 如果为空直接抛出异常 类似于断言的思想
     * @param status 当status为false 就会抛出异常 不继续执行后续语句
     */
    public static void assertMethod(boolean status) throws Exception {
        //为false抛出异常
        if (!status) {
            //记录错误信息
            log.error(HttpStatus.INTERNAL_SERVER_ERROR.name());
            //抛出异常
            throw MyException.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(HttpStatus.INTERNAL_SERVER_ERROR.name()).build();
        }
    }
}
```

#### MyException类

这个类是自定义异常的声明，由于AOP中只有RuntimeException及其子类的异常可以被全局异常处理器处理，所以其继承了RuntimeException。拓展了code和msg属性

```java
package cn.rayfoo.validate.exception;

import lombok.*;

/**
 * @Author: rayfoo@qq.com
 * @Date: 2020/7/20 9:26 下午
 * @Description: 自定义的异常...
 */
@Getter@Setter@Builder@NoArgsConstructor@AllArgsConstructor
public class MyException extends RuntimeException{

    private int code;

    private String msg;

}

```

#### ServerExceptionResolver类

此类用来处理手动抛出的自定义异常。

```java
package cn.rayfoo.validate.exception;

import HttpStatus;
import Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * @date 2020/8/5 14:55
 * @description 全局异常处理
 */
@ControllerAdvice@Slf4j
public class ServerExceptionResolver {

    /**
     * 处理自定义的异常
     * @param ex
     * @return
     */
    @ExceptionHandler(MyException.class)@ResponseBody
    public Result<String> resolveMyException(MyException ex){
        //打印完整的异常信息
        ex.printStackTrace();
        //创建result
        Result<String> result = new Result<>();
        //设置result属性
        result.setData(ex.getMsg());
        result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setMsg(ex.getMsg());
        //保存自定义异常日志
        log.error(ex.getMsg());
        return result;
    }
}

```

### enums包

很明显这个包中存放的是一个枚举，这个枚举是为了@Verify注解创建的，其作为@Verify的属性提供正则校验表。

#### RegexOption枚举

如果需要新的校验条件只需要拓展这个枚举即可。

```java
package cn.rayfoo.validate.enums;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p>参数校验枚举</p>
 * @date 2020/8/7 15:51
 */
public enum RegexOption {

    /**
     * 缺省，表示不进行正则校验
     */
    DEFAULT(""),

    /**
     * 邮箱正则
     */
    EMAIL_REGEX("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"),

    /**
     * 手机号正则
     */
    PHONE_NUMBER_REGEX("^((13[0-9])|(14[0|5|6|7|9])|(15[0-3])|(15[5-9])|(16[6|7])|(17[2|3|5|6|7|8])|(18[0-9])|(19[1|8|9]))\\d{8}$"),

    /**
     * 身份证正则
     */
    IDENTITY_CARD_REGEX("(^\\d{18}$)|(^\\d{15}$)"),

    /**
     * URL正则
     */
    URL_REGEX("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?"),

    /**
     * IP地址正则
     */
    IP_ADDR_REGEX("(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)"),

    /**
     * 用户名正则
     */
    USERNAME_REGEX("^[a-zA-Z]\\w{5,20}$"),

    /**
     * 密码正则
     */
    PASSWORD_REGEX("^[a-zA-Z0-9]{6,20}$");

    /**
     * 正则
     */
    private String regex;

    /**
     * 构造方法
     *
     * @param regex
     */
    private RegexOption(String regex) {
        this.regex = regex;
    }


    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}

```

### annotation包

显而易见，这个包是用来存放注解的。这四个注解可以完成大部分情况下的参数校验工作。

#### @Verify注解

标注在参数、字段上，用于标注需要校验的数据以及其校验的规则。

- name：描述，当校验结果为false时，返回用户提示信息时使用
- maxLength：最大长度，用于判断字符串类型，如果时默认值代表不进行判断
- minLength：最小长度，用于判断字符串类型，如果时默认值代表不进行判断
- required：是否为必填属性
- notNull：是否允许为空
- regular：是否需要进行正则校验，如果需要正则内容是什么，默认为不进行正则校验
- isEntity：是否是一个entity，如果是，递归调用判断其内的属性

```java
package com.github.validate.annotation;

import com.github.validate.enums.RegexOption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/7 15:33
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.PARAMETER})
public @interface Verify {

    /** 参数名称 */
    String name();

    /** 参数最大长度 */
    int maxLength() default Integer.MAX_VALUE;

    /*** 是否是实体，如果是，继续判断其内部的值 */
    boolean isEntity() default false;

    /** 是否必填 这里只是判断是否为null */
    boolean required() default true;

    /** 是否为非空 是否为null和空串都判断 */
    boolean notNull() default true;

    /** 最小长度 */
    int minLength() default Integer.MIN_VALUE;

    /** 正则匹配 */
    RegexOption regular() default RegexOption.DEFAULT;

}

```

#### @RequestMap注解

用于标注Controller方法的参数，用于指定Map类型参数。

- baseEntityList制定Entity的全类名，如果Entity的属性名和key一致，就进行参数校验，校验规则由Entity中加了@Verify注解的属性来决定

```java
package cn.rayfoo.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p>对Map</p>
 * @date 2020/8/8 19:50
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestMap {

    /**
     * 实体类全类名列表
     */
    String[] baseEntityList();

}

```

#### @RequestEntity注解

用法同上，不过其标注的是Entity类型。

```java
package cn.rayfoo.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/8 22:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestEntity {

    String value() default "";

}

```

#### @EnableVerify注解

用于标注在SpringBoot启动类

- execution：指定execution表达式，只有execution标识的方法才会进行全局参数校验

```java
package cn.rayfoo.validate.annotation;

import EnableVerifyConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 13:36
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EnableVerifyConfig.class})
public @interface EnableVerify {

    String execution();

}

```

### Config包

#### EnableVerifyConfig类

@EnableVerify引用了EnableVerifyConfig类，下面我们介绍一下这个类是如何 编写的：

 这个类被@EnableVerify所引用，当读取到此注解时，就会自动注册此类registerBeanDefinitions方法内的代码主要是进行Bean的注册（由于默认时没有扫描cn,rayfoo包的，所以需要注册到Spring中的Bean要手动的放入Bean容器中），所以我们需要手动的注册好Aspect类，这里借鉴了通用Mapper的源码。

```java
package cn.rayfoo.validate.config;

import EnableVerify;
import ValidateAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 13:37
 */
public class EnableVerifyConfig implements ImportBeanDefinitionRegistrar{

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
        registry.registerBeanDefinition("validateAdvice",validateAdvice);


        /*
         * 注册全局异常处理 如果你的包恰好是cn.rayfoo就需要将其注释
         */

        //创建Bean
//        BeanDefinition serverExceptionResolver = new GenericBeanDefinition();
//        //指定Bean的字节码
//        serverExceptionResolver.setBeanClassName(ServerExceptionResolver.class.getName());
//        //注册bean
//        registry.registerBeanDefinition("serverExceptionResolver",serverExceptionResolver);




    }

}

```

### aspect包

#### ValidateAdvice类

最后就是此篇文章的重中之重了，所有的注解解析和参数校验都是在这个类中完成的，其主要使用了前置通知，在通知中获取目标方法，通过反射拿到目标方法的参数，根据方法参数的类型再选择进行何种判断。

```java
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
        Object[] args = invocation.getArguments();
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
            //是否是实体类
            isEntity(param);
        }
    }

    /**
     * 递归判断是否存在实体属性
     * @param entity 判断的内容
     */
    public void isEntity(Object entity) throws Exception {
        //获取其内部的所有属性
        Field[] fields = entity.getClass().getDeclaredFields();
        //遍历属性
        for (Field field : fields) {
            //获取私有属性值
            field.setAccessible(true);
            //需要做校验的参数
            if (field.isAnnotationPresent(Verify.class)) {
                //获取注解对象
                Verify verify = field.getAnnotation(Verify.class);
                //校验的对象
                Object fieldObj = field.get(entity);
                //如果这个属性是一个实体
                if (verify.isEntity()) {
                    //递归调用
                    isEntity(fieldObj);
                }
                //校验
                validate(verify, fieldObj);
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
    private <T> int indexOf(T[] array, Object value) {
        if (null != array) {
            for (int i = 0; i < array.length; ++i) {
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

```

## 使用Param-Validate

### 导入依赖

```java
        <dependency>
            <groupId>com.github.18338862369</groupId>
            <artifactId>param-validate</artifactId>
            <version>1.0.0</version>
        </dependency>
```

此依赖中包含了下述依赖，如果是其他SpringBoot版本，建议对依赖进行排除，并重新引入依赖。

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200809212655.png)

### 本地引入依赖

创建一个文件夹将param-validate-1.0.0.jar放入，创建一个名为install.bat的bat文件，执行文件即可将jar安装到本地maven仓库。

```bat
mvn install:install-file -Dfile=%~dp0param-validate-1.0.0.jar -DgroupId=com.github.18338862369 -DartifactId=param-validate -Dversion=1.0.0 -Dpackaging=jar
```

### 使用

#### 在启动类上加入`@EnableVerify(execution = "execution(* cn.rayfoo.web..*(..))")`注解

```java
package cn.rayfoo;

import EnableVerify;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 16:05
 */
@SpringBootApplication
@EnableVerify(execution = "execution(* cn.rayfoo.web..*(..))")
public class Runner {

    public static void main(String[] args) {
        SpringApplication.run(Runner.class, args);
    }

}

```

#### 在Controller测试

```java
package cn.rayfoo.web;

import Verify;
import RegexOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rayfoo@qq.com
 * @version 1.0
 * <p></p>
 * @date 2020/8/9 16:12
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public void test(@Verify(name = "用户名",regular = RegexOption.USERNAME_REGEX)String username){
        System.out.println("validate");
    }

}

```

#### 使用postMan测试

![](https://rayfoo-dev-tst.oss-cn-beijing.aliyuncs.com/img/20200809213217.png)

如果成功进行了参数校验，表示jar包执行正常



