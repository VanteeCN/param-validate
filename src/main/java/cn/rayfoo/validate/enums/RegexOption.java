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
