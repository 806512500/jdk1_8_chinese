/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.net;


/**
 * PasswordAuthentication 类是 Authenticator 使用的数据持有者。它只是一个用于存储用户名和密码的仓库。
 *
 * @see java.net.Authenticator
 * @see java.net.Authenticator#getPasswordAuthentication()
 *
 * @author  Bill Foote
 * @since   1.2
 */

public final class PasswordAuthentication {

    private String userName;
    private char[] password;

    /**
     * 从给定的用户名和密码创建一个新的 {@code PasswordAuthentication} 对象。
     *
     * <p> 注意，给定的用户密码在存储到新的 {@code PasswordAuthentication} 对象之前会被克隆。
     *
     * @param userName 用户名
     * @param password 用户的密码
     */
    public PasswordAuthentication(String userName, char[] password) {
        this.userName = userName;
        this.password = password.clone();
    }

    /**
     * 返回用户名。
     *
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 返回用户密码。
     *
     * <p> 注意，此方法返回的是密码的引用。调用者有责任在不再需要密码信息后将其清零。
     *
     * @return 密码
     */
    public char[] getPassword() {
        return password;
    }
}
