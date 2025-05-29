
/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang.invoke;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import sun.invoke.util.BytecodeDescriptor;
import sun.invoke.util.Wrapper;
import static sun.invoke.util.Wrapper.*;

class TypeConvertingMethodAdapter extends MethodVisitor {

    TypeConvertingMethodAdapter(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    private static final int NUM_WRAPPERS = Wrapper.values().length;

    private static final String NAME_OBJECT = "java/lang/Object";
    private static final String WRAPPER_PREFIX = "Ljava/lang/";

    // 所有基本类型的名称，装箱方法的名称
    private static final String NAME_BOX_METHOD = "valueOf";

    // 用于扩展基本类型转换的指令表；NOP = 无转换
    private static final int[][] wideningOpcodes = new int[NUM_WRAPPERS][NUM_WRAPPERS];

    private static final Wrapper[] FROM_WRAPPER_NAME = new Wrapper[16];

    // 按 ASM 类型排序索引的基本类型包装器表
    private static final Wrapper[] FROM_TYPE_SORT = new Wrapper[16];

    static {
        for (Wrapper w : Wrapper.values()) {
            if (w.basicTypeChar() != 'L') {
                int wi = hashWrapperName(w.wrapperSimpleName());
                assert (FROM_WRAPPER_NAME[wi] == null);
                FROM_WRAPPER_NAME[wi] = w;
            }
        }

        for (int i = 0; i < NUM_WRAPPERS; i++) {
            for (int j = 0; j < NUM_WRAPPERS; j++) {
                wideningOpcodes[i][j] = Opcodes.NOP;
            }
        }

        initWidening(LONG,   Opcodes.I2L, BYTE, SHORT, INT, CHAR);
        initWidening(LONG,   Opcodes.F2L, FLOAT);
        initWidening(FLOAT,  Opcodes.I2F, BYTE, SHORT, INT, CHAR);
        initWidening(FLOAT,  Opcodes.L2F, LONG);
        initWidening(DOUBLE, Opcodes.I2D, BYTE, SHORT, INT, CHAR);
        initWidening(DOUBLE, Opcodes.F2D, FLOAT);
        initWidening(DOUBLE, Opcodes.L2D, LONG);

        FROM_TYPE_SORT[Type.BYTE] = Wrapper.BYTE;
        FROM_TYPE_SORT[Type.SHORT] = Wrapper.SHORT;
        FROM_TYPE_SORT[Type.INT] = Wrapper.INT;
        FROM_TYPE_SORT[Type.LONG] = Wrapper.LONG;
        FROM_TYPE_SORT[Type.CHAR] = Wrapper.CHAR;
        FROM_TYPE_SORT[Type.FLOAT] = Wrapper.FLOAT;
        FROM_TYPE_SORT[Type.DOUBLE] = Wrapper.DOUBLE;
        FROM_TYPE_SORT[Type.BOOLEAN] = Wrapper.BOOLEAN;
    }

    private static void initWidening(Wrapper to, int opcode, Wrapper... from) {
        for (Wrapper f : from) {
            wideningOpcodes[f.ordinal()][to.ordinal()] = opcode;
        }
    }

    /**
     * 从 Wrapper.hashWrap() 派生的类名到 Wrapper 的哈希
     * @param xn
     * @return 0-15 的哈希码
     */
    private static int hashWrapperName(String xn) {
        if (xn.length() < 3) {
            return 0;
        }
        return (3 * xn.charAt(1) + xn.charAt(2)) % 16;
    }

    private Wrapper wrapperOrNullFromDescriptor(String desc) {
        if (!desc.startsWith(WRAPPER_PREFIX)) {
            // 不是类类型（数组或方法），因此不是装箱类型
            // 或不在正确的包中
            return null;
        }
        // 剪裁为简单的类名
        String cname = desc.substring(WRAPPER_PREFIX.length(), desc.length() - 1);
        // 哈希到 Wrapper
        Wrapper w = FROM_WRAPPER_NAME[hashWrapperName(cname)];
        if (w == null || w.wrapperSimpleName().equals(cname)) {
            return w;
        } else {
            return null;
        }
    }

    private static String wrapperName(Wrapper w) {
        return "java/lang/" + w.wrapperSimpleName();
    }

    private static String unboxMethod(Wrapper w) {
        return w.primitiveSimpleName() + "Value";
    }

    private static String boxingDescriptor(Wrapper w) {
        return String.format("(%s)L%s;", w.basicTypeChar(), wrapperName(w));
    }

    private static String unboxingDescriptor(Wrapper w) {
        return "()" + w.basicTypeChar();
    }

    void boxIfTypePrimitive(Type t) {
        Wrapper w = FROM_TYPE_SORT[t.getSort()];
        if (w != null) {
            box(w);
        }
    }

    void widen(Wrapper ws, Wrapper wt) {
        if (ws != wt) {
            int opcode = wideningOpcodes[ws.ordinal()][wt.ordinal()];
            if (opcode != Opcodes.NOP) {
                visitInsn(opcode);
            }
        }
    }

    void box(Wrapper w) {
        visitMethodInsn(Opcodes.INVOKESTATIC,
                wrapperName(w),
                NAME_BOX_METHOD,
                boxingDescriptor(w), false);
    }

    /**
     * 通过拆箱转换类型。源类型已知为基本类型包装器。
     * @param sname 与包装的引用源类型对应的基本类型包装器
     * @param wt 转换为的基本类型包装器
     */
    void unbox(String sname, Wrapper wt) {
        visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                sname,
                unboxMethod(wt),
                unboxingDescriptor(wt), false);
    }

    private String descriptorToName(String desc) {
        int last = desc.length() - 1;
        if (desc.charAt(0) == 'L' && desc.charAt(last) == ';') {
            // 以描述符形式
            return desc.substring(1, last);
        } else {
            // 已经是内部名称形式
            return desc;
        }
    }

    void cast(String ds, String dt) {
        String ns = descriptorToName(ds);
        String nt = descriptorToName(dt);
        if (!nt.equals(ns) && !nt.equals(NAME_OBJECT)) {
            visitTypeInsn(Opcodes.CHECKCAST, nt);
        }
    }

    private boolean isPrimitive(Wrapper w) {
        return w != OBJECT;
    }

    private Wrapper toWrapper(String desc) {
        char first = desc.charAt(0);
        if (first == '[' || first == '(') {
            first = 'L';
        }
        return Wrapper.forBasicType(first);
    }


                /**
     * 将类型为 'arg' 的参数转换为可以传递给 'target' 的形式，确保其为 'functional'。
     * 在方法代码中插入所需的转换指令。
     * @param arg
     * @param target
     * @param functional
     */
    void convertType(Class<?> arg, Class<?> target, Class<?> functional) {
        if (arg.equals(target) && arg.equals(functional)) {
            return;
        }
        if (arg == Void.TYPE || target == Void.TYPE) {
            return;
        }
        if (arg.isPrimitive()) {
            Wrapper wArg = Wrapper.forPrimitiveType(arg);
            if (target.isPrimitive()) {
                // 两个都是基本类型：扩展
                widen(wArg, Wrapper.forPrimitiveType(target));
            } else {
                // 基本类型参数转换为引用类型目标
                String dTarget = BytecodeDescriptor.unparse(target);
                Wrapper wPrimTarget = wrapperOrNullFromDescriptor(dTarget);
                if (wPrimTarget != null) {
                    // 目标是装箱的基本类型，先扩展再装箱
                    widen(wArg, wPrimTarget);
                    box(wPrimTarget);
                } else {
                    // 否则，装箱并强制转换
                    box(wArg);
                    cast(wrapperName(wArg), dTarget);
                }
            }
        } else {
            String dArg = BytecodeDescriptor.unparse(arg);
            String dSrc;
            if (functional.isPrimitive()) {
                dSrc = dArg;
            } else {
                // 强制转换以转换为可能更具体的类型，并为无效参数生成 CCE
                dSrc = BytecodeDescriptor.unparse(functional);
                cast(dArg, dSrc);
            }
            String dTarget = BytecodeDescriptor.unparse(target);
            if (target.isPrimitive()) {
                Wrapper wTarget = toWrapper(dTarget);
                // 引用类型参数转换为基本类型目标
                Wrapper wps = wrapperOrNullFromDescriptor(dSrc);
                if (wps != null) {
                    if (wps.isSigned() || wps.isFloating()) {
                        // 装箱数字转换为基本类型
                        unbox(wrapperName(wps), wTarget);
                    } else {
                        // 字符或布尔值
                        unbox(wrapperName(wps), wps);
                        widen(wps, wTarget);
                    }
                } else {
                    // 源类型是引用类型，但不是装箱类型，
                    // 假设它是目标类型的超类型
                    String intermediate;
                    if (wTarget.isSigned() || wTarget.isFloating()) {
                        // 装箱数字转换为基本类型
                        intermediate = "java/lang/Number";
                    } else {
                        // 字符或布尔值
                        intermediate = wrapperName(wTarget);
                    }
                    cast(dSrc, intermediate);
                    unbox(intermediate, wTarget);
                }
            } else {
                // 两个都是引用类型：直接转换为目标类型
                cast(dSrc, dTarget);
            }
        }
    }

    /**
     * 以下方法是从
     * org.objectweb.asm.commons.InstructionAdapter 复制的。ASM 是一个非常小且快速的 Java 字节码操作框架。
     * 版权所有 (c) 2000-2005 INRIA, France Telecom 保留所有权利。
     */
    void iconst(final int cst) {
        if (cst >= -1 && cst <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + cst);
        } else if (cst >= Byte.MIN_VALUE && cst <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, cst);
        } else if (cst >= Short.MIN_VALUE && cst <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, cst);
        } else {
            mv.visitLdcInsn(cst);
        }
    }
}
