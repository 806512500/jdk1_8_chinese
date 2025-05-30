/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.sql;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * <P>一个提供数据库访问错误或其他错误信息的异常。
 *
 * <P>每个 <code>SQLException</code> 提供以下几种信息：
 * <UL>
 *   <LI> 描述错误的字符串。这是用作 Java 异常消息的，可以通过 <code>getMesasge</code> 方法获取。
 *   <LI> 一个 "SQLstate" 字符串，遵循 XOPEN SQLstate 规范或 SQL:2003 规范。
 *       SQLState 字符串的值在相应的规范中有描述。可以通过 <code>DatabaseMetaData</code> 方法 <code>getSQLStateType</code>
 *       来确定驱动程序返回的是 XOPEN 类型还是 SQL:2003 类型。
 *   <LI> 一个特定于供应商的整数错误代码。通常这是底层数据库返回的实际错误代码。
 *   <LI> 一个链接到下一个异常的链。可以用于提供额外的错误信息。
 *   <LI> 此 <code>SQLException</code> 的因果关系，如果有。
 * </UL>
 */
public class SQLException extends java.lang.Exception
                          implements Iterable<Throwable> {

    /**
     * 用给定的 <code>reason</code>、<code>SQLState</code> 和 <code>vendorCode</code>
     * 构造一个 <code>SQLException</code> 对象。
     *
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     */
    public SQLException(String reason, String SQLState, int vendorCode) {
        super(reason);
        this.SQLState = SQLState;
        this.vendorCode = vendorCode;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                DriverManager.println("SQLState(" + SQLState +
                                                ") vendor code(" + vendorCode + ")");
                printStackTrace(DriverManager.getLogWriter());
            }
        }
    }


    /**
     * 用给定的 <code>reason</code> 和 <code>SQLState</code>
     * 构造一个 <code>SQLException</code> 对象。
     *
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述
     * @param SQLState 识别异常的 XOPEN 或 SQL:2003 代码
     */
    public SQLException(String reason, String SQLState) {
        super(reason);
        this.SQLState = SQLState;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                printStackTrace(DriverManager.getLogWriter());
                DriverManager.println("SQLException: SQLState(" + SQLState + ")");
            }
        }
    }

    /**
     * 用给定的 <code>reason</code> 构造一个 <code>SQLException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     *
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。
     * <p>
     * @param reason 异常的描述
     */
    public SQLException(String reason) {
        super(reason);
        this.SQLState = null;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                printStackTrace(DriverManager.getLogWriter());
            }
        }
    }

    /**
     * 构造一个 <code>SQLException</code> 对象。
     * <code>reason</code> 和 <code>SQLState</code> 初始化为 <code>null</code>，
     * 供应商代码初始化为 0。
     *
     * <code>cause</code> 未初始化，可以随后通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法初始化。
     *
     */
    public SQLException() {
        super();
        this.SQLState = null;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                printStackTrace(DriverManager.getLogWriter());
            }
        }
    }

    /**
     * 用给定的 <code>cause</code> 构造一个 <code>SQLException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <code>reason</code> 如果 <code>cause==null</code> 则初始化为 <code>null</code>，
     * 否则初始化为 <code>cause.toString()</code>。
     * <p>
     * @param cause 该 <code>SQLException</code> 的根本原因
     * （稍后可以通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLException(Throwable cause) {
        super(cause);

        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                printStackTrace(DriverManager.getLogWriter());
            }
        }
    }

    /**
     * 用给定的 <code>reason</code> 和 <code>cause</code>
     * 构造一个 <code>SQLException</code> 对象。
     * <code>SQLState</code> 初始化为 <code>null</code>，供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param cause 该 <code>SQLException</code> 的根本原因
     * （稍后可以通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLException(String reason, Throwable cause) {
        super(reason,cause);

        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                    printStackTrace(DriverManager.getLogWriter());
            }
        }
    }

    /**
     * 用给定的 <code>reason</code>、<code>SQLState</code> 和 <code>cause</code>
     * 构造一个 <code>SQLException</code> 对象。
     * 供应商代码初始化为 0。
     * <p>
     * @param reason 异常的描述。
     * @param sqlState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param cause 该 <code>SQLException</code> 的根本原因
     * （稍后可以通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLException(String reason, String sqlState, Throwable cause) {
        super(reason,cause);

        this.SQLState = sqlState;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                printStackTrace(DriverManager.getLogWriter());
                DriverManager.println("SQLState(" + SQLState + ")");
            }
        }
    }

    /**
     * 用给定的 <code>reason</code>、<code>SQLState</code>、<code>vendorCode</code>
     * 和 <code>cause</code> 构造一个 <code>SQLException</code> 对象。
     * <p>
     * @param reason 异常的描述
     * @param sqlState 识别异常的 XOPEN 或 SQL:2003 代码
     * @param vendorCode 数据库供应商特定的异常代码
     * @param cause 该 <code>SQLException</code> 的根本原因
     * （稍后可以通过 <code>getCause()</code> 方法检索）；可以为 null，表示原因不存在或未知。
     * @since 1.6
     */
    public SQLException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason,cause);

        this.SQLState = sqlState;
        this.vendorCode = vendorCode;
        if (!(this instanceof SQLWarning)) {
            if (DriverManager.getLogWriter() != null) {
                DriverManager.println("SQLState(" + SQLState +
                                                ") vendor code(" + vendorCode + ")");
                printStackTrace(DriverManager.getLogWriter());
            }
        }
    }

    /**
     * 检索此 <code>SQLException</code> 对象的 SQLState。
     *
     * @return SQLState 值
     */
    public String getSQLState() {
        return (SQLState);
    }

    /**
     * 检索此 <code>SQLException</code> 对象的供应商特定异常代码。
     *
     * @return 供应商的错误代码
     */
    public int getErrorCode() {
        return (vendorCode);
    }

    /**
     * 检索通过 setNextException(SQLException ex) 链接到此
     * <code>SQLException</code> 对象的异常。
     *
     * @return 链中的下一个 <code>SQLException</code> 对象；
     *         如果没有则返回 <code>null</code>
     * @see #setNextException
     */
    public SQLException getNextException() {
        return (next);
    }

    /**
     * 将一个 <code>SQLException</code> 对象添加到链的末尾。
     *
     * @param ex 将要添加到 <code>SQLException</code> 链末尾的新异常
     * @see #getNextException
     */
    public void setNextException(SQLException ex) {

        SQLException current = this;
        for(;;) {
            SQLException next=current.next;
            if (next != null) {
                current = next;
                continue;
            }

            if (nextUpdater.compareAndSet(current,null,ex)) {
                return;
            }
            current=current.next;
        }
    }

    /**
     * 返回一个迭代器，用于遍历链中的 SQLExceptions。迭代器将
     * 用于遍历每个 SQLException 及其底层原因（如果有）。
     *
     * @return 一个按正确顺序遍历链中的 SQLExceptions 和原因的迭代器
     *
     * @since 1.6
     */
    public Iterator<Throwable> iterator() {

       return new Iterator<Throwable>() {

           SQLException firstException = SQLException.this;
           SQLException nextException = firstException.getNextException();
           Throwable cause = firstException.getCause();

           public boolean hasNext() {
               if(firstException != null || nextException != null || cause != null)
                   return true;
               return false;
           }

           public Throwable next() {
               Throwable throwable = null;
               if(firstException != null){
                   throwable = firstException;
                   firstException = null;
               }
               else if(cause != null){
                   throwable = cause;
                   cause = cause.getCause();
               }
               else if(nextException != null){
                   throwable = nextException;
                   cause = nextException.getCause();
                   nextException = nextException.getNextException();
               }
               else
                   throw new NoSuchElementException();
               return throwable;
           }

           public void remove() {
               throw new UnsupportedOperationException();
           }

       };

    }

    /**
         * @serial
         */
    private String SQLState;

        /**
         * @serial
         */
    private int vendorCode;

        /**
         * @serial
         */
    private volatile SQLException next;

    private static final AtomicReferenceFieldUpdater<SQLException,SQLException> nextUpdater =
            AtomicReferenceFieldUpdater.newUpdater(SQLException.class,SQLException.class,"next");

    private static final long serialVersionUID = 2135244094396331484L;
}
