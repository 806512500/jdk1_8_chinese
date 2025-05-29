/*
 * 版权所有 (c) 1996, 1998, Oracle 和/或其附属公司。保留所有权利。
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
package java.rmi.dgc;

/**
 * 租约包含一个唯一的 VM 标识符和一个租约期限。Lease 对象用于请求和授予远程对象引用的租约。
 */
public final class Lease implements java.io.Serializable {

    /**
     * @serial 与此租约关联的虚拟机 ID。
     * @see #getVMID
     */
    private VMID vmid;

    /**
     * @serial 此租约的期限。
     * @see #getValue
     */
    private long value;
    /** 表示与 JDK 1.1.x 版本的类的兼容性 */
    private static final long serialVersionUID = -5713411624328831948L;

    /**
     * 构造具有特定 VMID 和租约期限的租约。vmid 可能为 null。
     * @param id 与此租约关联的 VMID
     * @param duration 租约期限
     */
    public Lease(VMID id, long duration)
    {
        vmid = id;
        value = duration;
    }

    /**
     * 返回与租约关联的客户端 VMID。
     * @return 客户端 VMID
     */
    public VMID getVMID()
    {
        return vmid;
    }

    /**
     * 返回租约期限。
     * @return 租约期限
     */
    public long getValue()
    {
        return value;
    }
}
