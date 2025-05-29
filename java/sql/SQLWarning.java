/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.sql;

/**
 * <P>一个提供有关数据库访问警告信息的异常。警告会静默地链接到导致其报告的对象的方法。
 * <P>
 * 可以从 <code>Connection</code>、<code>Statement</code> 和 <code>ResultSet</code> 对象中检索警告。
 * 尝试在连接关闭后检索警告将导致异常被抛出。
 * 同样，尝试在语句关闭后或结果集关闭后检索警告将导致异常被抛出。注意，关闭语句也会关闭它可能产生的结果集。
 *
 * @see Connection#getWarnings
 * @see Statement#getWarnings
 * @see ResultSet#getWarnings
 */
public class SQLWarning extends SQLException {

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code> 的 <code>SQLWarning</code> 对象。
     *
     * <code>cause</code> 未初始化，可以通过调用 {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     * <p>
     * @param reason 警告的描述
     * @param SQLState 识别警告的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的警告代码
     */
     public SQLWarning(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
        DriverManager.println("SQLWarning: reason(" + reason +
                              ") SQLState(" + SQLState +
                              ") vendor code(" + vendorCode + ")");
    }


    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>SQLState</code> 的 <code>SQLWarning</code> 对象。
     *
     * <code>cause</code> 未初始化，可以通过调用 {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。供应商代码初始化为 0。
     * <p>
     * @param reason 警告的描述
     * @param SQLState 识别警告的 XOPEN 或 SQL:2003 代码
     */
    public SQLWarning(String reason, String SQLState) {
        super(reason, SQLState);
        DriverManager.println("SQLWarning: reason(" + reason +
                                  ") SQLState(" + SQLState + ")");
    }

    /**
     * 构造一个带有给定 <code>reason</code> 的 <code>SQLWarning</code> 对象。将 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     *
     * <code>cause</code> 未初始化，可以通过调用 {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     * <p>
     * @param reason 警告的描述
     */
    public SQLWarning(String reason) {
        super(reason);
        DriverManager.println("SQLWarning: reason(" + reason + ")");
    }

    /**
     * 构造一个 <code>SQLWarning</code> 对象。将 <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     *
     * <code>cause</code> 未初始化，可以通过调用 {@link Throwable#initCause(java.lang.Throwable)} 方法来初始化。
     *
     */
    public SQLWarning() {
        super();
        DriverManager.println("SQLWarning: ");
    }

    /**
     * 构造一个带有给定 <code>cause</code> 的 <code>SQLWarning</code> 对象。将 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * 如果 <code>cause==null</code>，则将 <code>reason</code> 初始化为 <code>null</code>；如果 <code>cause!=null</code>，则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLWarning</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     */
    public SQLWarning(Throwable cause) {
        super(cause);
        DriverManager.println("SQLWarning");
    }

    /**
     * 构造一个带有给定 <code>reason</code> 和 <code>cause</code> 的 <code>SQLWarning</code> 对象。将 <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 警告的描述
     * @param cause 该 <code>SQLWarning</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     */
    public SQLWarning(String reason, Throwable cause) {
        super(reason,cause);
        DriverManager.println("SQLWarning : reason("+ reason + ")");
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code> 的 <code>SQLWarning</code> 对象。供应商代码初始化为 0。
     * <p>
     * @param reason 警告的描述
     * @param SQLState 识别警告的 XOPEN 或 SQL:2003 代码
     * @param cause 该 <code>SQLWarning</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     */
    public SQLWarning(String reason, String SQLState, Throwable cause) {
        super(reason,SQLState,cause);
        DriverManager.println("SQLWarning: reason(" + reason +
                                  ") SQLState(" + SQLState + ")");
    }

    /**
     * 构造一个带有给定 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code> 和 <code>cause</code> 的 <code>SQLWarning</code> 对象。
     * <p>
     * @param reason 警告的描述
     * @param SQLState 识别警告的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的警告代码
     * @param cause 该 <code>SQLWarning</code> 的根本原因（通过 <code>getCause()</code> 方法保存以供后续检索）；可以为 null，表示原因不存在或未知。
     */
    public SQLWarning(String reason, String SQLState, int vendorCode, Throwable cause) {
        super(reason,SQLState,vendorCode,cause);
        DriverManager.println("SQLWarning: reason(" + reason +
                              ") SQLState(" + SQLState +
                              ") vendor code(" + vendorCode + ")");
    }

                }
    /**
     * 获取通过 <code>setNextWarning</code> 链接到此 <code>SQLWarning</code> 对象的警告。
     *
     * @return 链中的下一个 <code>SQLException</code>；如果不存在，则为 <code>null</code>
     * @see #setNextWarning
     */
    public SQLWarning getNextWarning() {
        try {
            return ((SQLWarning)getNextException());
        } catch (ClassCastException ex) {
            // 链接的值不是 SQLWarning。
            // 这是添加到 SQLWarning 链中的编程错误。我们抛出一个 Java "Error"。
            throw new Error("SQLWarning 链中包含的值不是 SQLWarning");
        }
    }

    /**
     * 将一个 <code>SQLWarning</code> 对象添加到链的末尾。
     *
     * @param w 链的新的末尾 <code>SQLException</code>
     * @see #getNextWarning
     */
    public void setNextWarning(SQLWarning w) {
        setNextException(w);
    }

    private static final long serialVersionUID = 3917336774604784856L;
}
