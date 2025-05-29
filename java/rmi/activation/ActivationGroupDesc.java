
/*
 * 版权所有 (c) 1997, 2008, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.activation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.util.Arrays;
import java.util.Properties;

/**
 * 激活组描述符包含创建/重新创建激活组所需的信息，以激活对象。
 * 这样的描述符包含： <ul>
 * <li> 组的类名，
 * <li> 组的代码位置（组类的位置），以及
 * <li> 可以包含组特定初始化数据的“序列化”对象。 </ul> <p>
 *
 * 组的类必须是 <code>ActivationGroup</code> 的具体子类。通过
 * <code>ActivationGroup.createGroup</code> 静态方法创建/重新创建
 * <code>ActivationGroup</code> 的子类，该方法调用一个特殊的构造函数，该构造函数接受两个参数： <ul>
 *
 * <li> 组的 <code>ActivationGroupID</code>，以及
 * <li> 组的初始化数据（在 <code>java.rmi.MarshalledObject</code> 中）</ul><p>
 *
 * @author      Ann Wollrath
 * @since       1.2
 * @see         ActivationGroup
 * @see         ActivationGroupID
 */
public final class ActivationGroupDesc implements Serializable {

    /**
     * @serial 组的完全包限定类名。
     */
    private String className;

    /**
     * @serial 从何处加载组类的位置。
     */
    private String location;

    /**
     * @serial 组的初始化数据。
     */
    private MarshalledObject<?> data;

    /**
     * @serial 用于在另一个进程中执行 VM 的控制选项。
     */
    private CommandEnvironment env;

    /**
     * @serial 用于覆盖子进程中默认设置的属性映射。
     */
    private Properties props;

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private static final long serialVersionUID = -4936225423168276595L;

    /**
     * 构造一个使用系统默认组实现和代码位置的组描述符。属性指定 Java
     * 环境覆盖（这将覆盖组实现 VM 中的系统属性）。命令
     * 环境可以控制启动子 VM 时使用的确切命令/选项，或者可以是 <code>null</code> 以接受
     * rmid 的默认值。
     *
     * <p>此构造函数将创建一个 <code>ActivationGroupDesc</code>
     * ，其组类名为 <code>null</code>，表示系统的默认 <code>ActivationGroup</code> 实现。
     *
     * @param overrides 当组被重新创建时设置的属性集。
     * @param cmd 用于在另一个进程中执行 VM 的控制选项（或 <code>null</code>）。
     * @since 1.2
     */
    public ActivationGroupDesc(Properties overrides,
                               CommandEnvironment cmd)
    {
        this(null, null, null, overrides, cmd);
    }

    /**
     * 指定用于组的备用组实现和执行环境。
     *
     * @param className 组的包限定类名或 <code>null</code>。组类名为 <code>null</code> 表示
     * 系统的默认 <code>ActivationGroup</code> 实现。
     * @param location 从何处加载组类的位置
     * @param data 以序列化形式包含的组初始化数据（例如，可以包含属性）
     * @param overrides 用于覆盖子进程中默认设置的属性映射（将转换为 <code>-D</code> 选项），或 <code>null</code>。
     * @param cmd 用于在另一个进程中执行 VM 的控制选项（或 <code>null</code>）。
     * @since 1.2
     */
    public ActivationGroupDesc(String className,
                               String location,
                               MarshalledObject<?> data,
                               Properties overrides,
                               CommandEnvironment cmd)
    {
        this.props = overrides;
        this.env = cmd;
        this.data = data;
        this.location = location;
        this.className = className;
    }

    /**
     * 返回组的类名（可能是 <code>null</code>）。组类名为 <code>null</code> 表示
     * 系统的默认 <code>ActivationGroup</code> 实现。
     * @return 组的类名
     * @since 1.2
     */
    public String getClassName() {
        return className;
    }

    /**
     * 返回组的代码位置。
     * @return 组的代码位置
     * @since 1.2
     */
    public String getLocation() {
        return location;
    }

    /**
     * 返回组的初始化数据。
     * @return 组的初始化数据
     * @since 1.2
     */
    public MarshalledObject<?> getData() {
        return data;
    }

    /**
     * 返回组的属性覆盖列表。
     * @return 属性覆盖列表，或 <code>null</code>
     * @since 1.2
     */
    public Properties getPropertyOverrides() {
        return (props != null) ? (Properties) props.clone() : null;
    }

    /**
     * 返回组的命令环境控制对象。
     * @return 命令环境对象，或 <code>null</code>
     * @since 1.2
     */
    public CommandEnvironment getCommandEnvironment() {
        return this.env;
    }


    /**
     * 激活组实现的启动选项。
     *
     * 此类允许覆盖默认系统属性并指定 ActivationGroups 的实现定义选项。
     * @since 1.2
     */
    public static class CommandEnvironment implements Serializable {
        private static final long serialVersionUID = 6165754737887770191L;


                    /**
         * @serial
         */
        private String command;

        /**
         * @serial
         */
        private String[] options;

        /**
         * 使用所有必要的信息创建一个 CommandEnvironment。
         *
         * @param cmdpath 包含完整路径的 Java 可执行文件的名称，或者 <code>null</code>，表示“使用 rmid 的默认值”。
         * 命名的程序 <em>必须</em> 能够接受多个 <code>-Dpropname=value</code> 选项（如“java”工具文档中所述）
         *
         * @param argv 在创建 ActivationGroup 时将使用的额外选项。Null 与空列表具有相同的效果。
         * @since 1.2
         */
        public CommandEnvironment(String cmdpath,
                                  String[] argv)
        {
            this.command = cmdpath;     // 可能为 null

            // 在 this.options 中保存 argv 的安全副本
            if (argv == null) {
                this.options = new String[0];
            } else {
                this.options = new String[argv.length];
                System.arraycopy(argv, 0, this.options, 0, argv.length);
            }
        }

        /**
         * 获取配置的路径限定的 java 命令名称。
         *
         * @return 配置的名称，或者如果配置为接受默认值，则返回 <code>null</code>
         * @since 1.2
         */
        public String getCommandPath() {
            return (this.command);
        }

        /**
         * 获取配置的 java 命令选项。
         *
         * @return 将传递给新子命令的命令选项数组
         * 请注意，rmid 可能在这些选项之前或之后添加其他选项，或者两者都有。
         * 永不返回 <code>null</code>。
         * @since 1.2
         */
        public String[] getCommandOptions() {
            return options.clone();
        }

        /**
         * 比较两个命令环境的内容是否相等。
         *
         * @param       obj     要比较的对象
         * @return      如果这些对象相等则返回 true；否则返回 false。
         * @see         java.util.Hashtable
         * @since 1.2
         */
        public boolean equals(Object obj) {

            if (obj instanceof CommandEnvironment) {
                CommandEnvironment env = (CommandEnvironment) obj;
                return
                    ((command == null ? env.command == null :
                      command.equals(env.command)) &&
                     Arrays.equals(options, env.options));
            } else {
                return false;
            }
        }

        /**
         * 对于相似的 <code>CommandEnvironment</code> 返回相同的值。
         * @return 一个整数
         * @see java.util.Hashtable
         */
        public int hashCode()
        {
            // 哈希 command 并忽略可能昂贵的 options
            return (command == null ? 0 : command.hashCode());
        }

        /**
         * 用于自定义序列化的 <code>readObject</code>。
         *
         * <p>此方法按如下方式读取此类的序列化形式：
         *
         * <p>此方法首先在指定的对象输入流上调用 <code>defaultReadObject</code>，如果 <code>options</code>
         * 为 <code>null</code>，则将 <code>options</code> 设置为零长度的 <code>String</code> 数组。
         */
        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            if (options == null) {
                options = new String[0];
            }
        }
    }

    /**
     * 比较两个激活组描述符的内容是否相等。
     *
     * @param   obj     要比较的对象
     * @return  如果这些对象相等则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since 1.2
     */
    public boolean equals(Object obj) {

        if (obj instanceof ActivationGroupDesc) {
            ActivationGroupDesc desc = (ActivationGroupDesc) obj;
            return
                ((className == null ? desc.className == null :
                  className.equals(desc.className)) &&
                 (location == null ? desc.location == null :
                  location.equals(desc.location)) &&
                 (data == null ? desc.data == null : data.equals(desc.data)) &&
                 (env == null ? desc.env == null : env.equals(desc.env)) &&
                 (props == null ? desc.props == null :
                  props.equals(desc.props)));
        } else {
            return false;
        }
    }

    /**
     * 对于相似的 <code>ActivationGroupDesc</code> 生成相同的数字。
     * @return 一个整数
     * @see java.util.Hashtable
     */
    public int hashCode() {
        // 哈希 location, className, data, 和 env
        // 但忽略 props（可能昂贵）
        return ((location == null
                    ? 0
                    : location.hashCode() << 24) ^
                (env == null
                    ? 0
                    : env.hashCode() << 16) ^
                (className == null
                    ? 0
                    : className.hashCode() << 8) ^
                (data == null
                    ? 0
                    : data.hashCode()));
    }
}
