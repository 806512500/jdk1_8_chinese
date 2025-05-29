/*
 * 版权所有 (c) 2007, 2013，Oracle及其附属公司。保留所有权利。
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

/**
 * 提供访问文件和文件系统属性的接口和类。
 *
 * <blockquote><table cellspacing=1 cellpadding=0 summary="属性视图">
 * <tr><th align="left">属性视图</th><th align="left">描述</th></tr>
 * <tr><td valign=top><tt><i>{@link java.nio.file.attribute.AttributeView}</i></tt></td>
 *     <td>可以读取或更新文件系统中对象的非不透明值</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileAttributeView}</i></tt></td>
 *     <td>可以读取或更新文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.BasicFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新一组基本文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.PosixFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新POSIX定义的文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.DosFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新FAT文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileOwnerAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新文件的所有者</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.AclFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新访问控制列表</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;&nbsp;&nbsp;<i>{@link java.nio.file.attribute.UserDefinedFileAttributeView}&nbsp;&nbsp;</i></tt></td>
 *     <td>可以读取或更新用户定义的文件属性</td></tr>
 * <tr><td valign=top><tt>&nbsp;&nbsp;<i>{@link java.nio.file.attribute.FileStoreAttributeView}</i></tt></td>
 *     <td>可以读取或更新文件系统属性</td></tr>
 * </table></blockquote>
 *
 * <p> 属性视图提供了一个文件系统中对象的非不透明值，或称为<em>元数据</em>的只读或可更新视图。
 * {@link java.nio.file.attribute.FileAttributeView} 接口被其他几个接口扩展，这些接口提供了对特定文件属性集的视图。
 * 通过使用一个<em>类型标记</em>调用 {@link java.nio.file.Files#getFileAttributeView} 方法来选择所需的视图。
 * 视图也可以通过名称来识别。{@link java.nio.file.attribute.FileStoreAttributeView} 接口提供了对文件存储属性的访问。
 * 通过调用 {@link java.nio.file.FileStore#getFileStoreAttributeView} 方法可以获得给定类型的 {@code FileStoreAttributeView}。
 *
 * <p> {@link java.nio.file.attribute.BasicFileAttributeView} 类定义了读取和更新一组<em>基本</em>文件属性的方法，这些属性是许多文件系统共有的。
 *
 * <p> {@link java.nio.file.attribute.PosixFileAttributeView} 接口通过定义方法来访问POSIX标准系列中常用的文件属性，扩展了 {@code BasicFileAttributeView}。
 *
 * <p> {@link java.nio.file.attribute.DosFileAttributeView} 类通过定义方法来访问文件分配表（FAT）等文件系统中支持的旧“DOS”文件属性，扩展了 {@code BasicFileAttributeView}。
 *
 * <p> {@link java.nio.file.attribute.AclFileAttributeView} 类定义了读取和写入访问控制列表（ACL）文件属性的方法。此文件属性视图使用的ACL模型基于 <a href="http://www.ietf.org/rfc/rfc3530.txt">
 * <i>RFC&nbsp;3530: 网络文件系统（NFS）版本4协议</i></a> 中定义的模型。
 *
 * <p> 除了属性视图之外，此包还定义了在访问属性时使用的类和接口：
 *
 * <ul>
 *
 *   <li> {@link java.nio.file.attribute.UserPrincipal} 和
 *   {@link java.nio.file.attribute.GroupPrincipal} 接口表示一个身份或组身份。 </li>
 *
 *   <li> {@link java.nio.file.attribute.UserPrincipalLookupService}
 *   接口定义了查找用户或组身份的方法。 </li>
 *
 *   <li> {@link java.nio.file.attribute.FileAttribute} 接口
 *   表示在创建文件系统中的对象时需要原子设置的属性值。 </li>
 *
 * </ul>
 *
 *
 * <p> 除非另有说明，否则将 <tt>null</tt> 参数传递给此包中任何类或接口的构造函数或方法将导致抛出 {@link
 * java.lang.NullPointerException NullPointerException}。
 *
 * @since 1.7
 */

package java.nio.file.attribute;
