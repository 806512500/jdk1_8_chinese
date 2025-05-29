
/*
 * 版权所有 (c) 2012, 2013, Oracle 及/或其附属公司。保留所有权利。
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

import java.io.*;
import java.util.*;
import java.lang.reflect.Modifier;

import jdk.internal.org.objectweb.asm.*;

import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleNatives.Constants.*;

import sun.invoke.util.VerifyAccess;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;
import sun.reflect.misc.ReflectUtil;

/**
 * LambdaForm 的代码生成后端。
 * <p>
 * @author John Rose, JSR 292 EG
 */
class InvokerBytecodeGenerator {
    /** 为了方便定义类名。 */
    private static final String MH      = "java/lang/invoke/MethodHandle";
    private static final String MHI     = "java/lang/invoke/MethodHandleImpl";
    private static final String LF      = "java/lang/invoke/LambdaForm";
    private static final String LFN     = "java/lang/invoke/LambdaForm$Name";
    private static final String CLS     = "java/lang/Class";
    private static final String OBJ     = "java/lang/Object";
    private static final String OBJARY  = "[Ljava/lang/Object;";

    private static final String MH_SIG  = "L" + MH + ";";
    private static final String LF_SIG  = "L" + LF + ";";
    private static final String LFN_SIG = "L" + LFN + ";";
    private static final String LL_SIG  = "(L" + OBJ + ";)L" + OBJ + ";";
    private static final String LLV_SIG = "(L" + OBJ + ";L" + OBJ + ";)V";
    private static final String CLL_SIG = "(L" + CLS + ";L" + OBJ + ";)L" + OBJ + ";";

    /** 其超类的名称 */
    private static final String superName = OBJ;

    /** 新类的名称 */
    private final String className;

    /** 源文件的名称（用于堆栈跟踪打印）。 */
    private final String sourceFile;

    private final LambdaForm lambdaForm;
    private final String     invokerName;
    private final MethodType invokerType;

    /** 编译的 LambdaForm 中的局部变量信息 */
    private final int[]       localsMap;    // 索引
    private final BasicType[] localTypes;   // 基本类型
    private final Class<?>[]  localClasses; // 类型

    /** ASM 字节码生成。 */
    private ClassWriter cw;
    private MethodVisitor mv;

    private static final MemberName.Factory MEMBERNAME_FACTORY = MemberName.getFactory();
    private static final Class<?> HOST_CLASS = LambdaForm.class;

    /** 主构造函数；其他构造函数委托给这个构造函数。 */
    private InvokerBytecodeGenerator(LambdaForm lambdaForm, int localsMapSize,
                                     String className, String invokerName, MethodType invokerType) {
        if (invokerName.contains(".")) {
            int p = invokerName.indexOf(".");
            className = invokerName.substring(0, p);
            invokerName = invokerName.substring(p+1);
        }
        if (DUMP_CLASS_FILES) {
            className = makeDumpableClassName(className);
        }
        this.className  = LF + "$" + className;
        this.sourceFile = "LambdaForm$" + className;
        this.lambdaForm = lambdaForm;
        this.invokerName = invokerName;
        this.invokerType = invokerType;
        this.localsMap = new int[localsMapSize+1];
        // localsMap 的最后一个条目是分配的局部槽的数量
        this.localTypes = new BasicType[localsMapSize+1];
        this.localClasses = new Class<?>[localsMapSize+1];
    }

    /** 用于生成 LambdaForm 解释器入口点。 */
    private InvokerBytecodeGenerator(String className, String invokerName, MethodType invokerType) {
        this(null, invokerType.parameterCount(),
             className, invokerName, invokerType);
        // 创建一个数组，将名称索引映射到局部索引。
        localTypes[localTypes.length - 1] = V_TYPE;
        for (int i = 0; i < localsMap.length; i++) {
            localsMap[i] = invokerType.parameterSlotCount() - invokerType.parameterSlotDepth(i);
            if (i < invokerType.parameterCount())
                localTypes[i] = basicType(invokerType.parameterType(i));
        }
    }

    /** 用于为单个 LambdaForm 生成自定义代码。 */
    private InvokerBytecodeGenerator(String className, LambdaForm form, MethodType invokerType) {
        this(form, form.names.length,
             className, form.debugName, invokerType);
        // 创建一个数组，将名称索引映射到局部索引。
        Name[] names = form.names;
        for (int i = 0, index = 0; i < localsMap.length; i++) {
            localsMap[i] = index;
            if (i < names.length) {
                BasicType type = names[i].type();
                index += type.basicTypeSlots();
                localTypes[i] = type;
            }
        }
    }


    /** 用于转储类的实例计数器 */
    private final static HashMap<String,Integer> DUMP_CLASS_FILES_COUNTERS;
    /** 用于保存生成的类文件的调试标志 */
    private final static File DUMP_CLASS_FILES_DIR;

    static {
        if (DUMP_CLASS_FILES) {
            DUMP_CLASS_FILES_COUNTERS = new HashMap<>();
            try {
                File dumpDir = new File("DUMP_CLASS_FILES");
                if (!dumpDir.exists()) {
                    dumpDir.mkdirs();
                }
                DUMP_CLASS_FILES_DIR = dumpDir;
                System.out.println("Dumping class files to "+DUMP_CLASS_FILES_DIR+"/...");
            } catch (Exception e) {
                throw newInternalError(e);
            }
        } else {
            DUMP_CLASS_FILES_COUNTERS = null;
            DUMP_CLASS_FILES_DIR = null;
        }
    }

    static void maybeDump(final String className, final byte[] classFile) {
        if (DUMP_CLASS_FILES) {
            java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        String dumpName = className;
                        //dumpName = dumpName.replace('/', '-');
                        File dumpFile = new File(DUMP_CLASS_FILES_DIR, dumpName+".class");
                        System.out.println("dump: " + dumpFile);
                        dumpFile.getParentFile().mkdirs();
                        FileOutputStream file = new FileOutputStream(dumpFile);
                        file.write(classFile);
                        file.close();
                        return null;
                    } catch (IOException ex) {
                        throw newInternalError(ex);
                    }
                }
            });
        }
    }


                }

    private static String makeDumpableClassName(String className) {
        Integer ctr;
        synchronized (DUMP_CLASS_FILES_COUNTERS) {
            ctr = DUMP_CLASS_FILES_COUNTERS.get(className);
            if (ctr == null)  ctr = 0;
            DUMP_CLASS_FILES_COUNTERS.put(className, ctr+1);
        }
        String sfx = ctr.toString();
        while (sfx.length() < 3)
            sfx = "0"+sfx;
        className += sfx;
        return className;
    }

    class CpPatch {
        final int index;
        final String placeholder;
        final Object value;
        CpPatch(int index, String placeholder, Object value) {
            this.index = index;
            this.placeholder = placeholder;
            this.value = value;
        }
        public String toString() {
            return "CpPatch/index="+index+",placeholder="+placeholder+",value="+value;
        }
    }

    Map<Object, CpPatch> cpPatches = new HashMap<>();

    int cph = 0;  // 用于计数常量占位符

    String constantPlaceholder(Object arg) {
        String cpPlaceholder = "CONSTANT_PLACEHOLDER_" + cph++;
        if (DUMP_CLASS_FILES) cpPlaceholder += " <<" + debugString(arg) + ">>";  // 调试辅助
        if (cpPatches.containsKey(cpPlaceholder)) {
            throw new InternalError("observed CP placeholder twice: " + cpPlaceholder);
        }
        // 在常量池中插入占位符并记住修补程序
        int index = cw.newConst((Object) cpPlaceholder);  // TODO 检查是否已在常量池中
        cpPatches.put(cpPlaceholder, new CpPatch(index, cpPlaceholder, arg));
        return cpPlaceholder;
    }

    Object[] cpPatches(byte[] classFile) {
        int size = getConstantPoolSize(classFile);
        Object[] res = new Object[size];
        for (CpPatch p : cpPatches.values()) {
            if (p.index >= size)
                throw new InternalError("in cpool["+size+"]: "+p+"\n"+Arrays.toString(Arrays.copyOf(classFile, 20)));
            res[p.index] = p.value;
        }
        return res;
    }

    private static String debugString(Object arg) {
        if (arg instanceof MethodHandle) {
            MethodHandle mh = (MethodHandle) arg;
            MemberName member = mh.internalMemberName();
            if (member != null)
                return member.toString();
            return mh.debugString();
        }
        return arg.toString();
    }

    /**
     * 从给定的类文件中提取常量池条目的数量。
     *
     * @param classFile 问题类文件的字节。
     * @return 常量池中的条目数量。
     */
    private static int getConstantPoolSize(byte[] classFile) {
        // 前几个字节：
        // u4 magic;
        // u2 minor_version;
        // u2 major_version;
        // u2 constant_pool_count;
        return ((classFile[8] & 0xFF) << 8) | (classFile[9] & 0xFF);
    }

    /**
     * 提取新定义方法的 MemberName。
     */
    private MemberName loadMethod(byte[] classFile) {
        Class<?> invokerClass = loadAndInitializeInvokerClass(classFile, cpPatches(classFile));
        return resolveInvokerMember(invokerClass, invokerName, invokerType);
    }

    /**
     * 在运行时系统中定义给定的类为匿名类。
     */
    private static Class<?> loadAndInitializeInvokerClass(byte[] classBytes, Object[] patches) {
        Class<?> invokerClass = UNSAFE.defineAnonymousClass(HOST_CLASS, classBytes, patches);
        UNSAFE.ensureClassInitialized(invokerClass);  // 确保类已初始化；VM 可能会抱怨。
        return invokerClass;
    }

    private static MemberName resolveInvokerMember(Class<?> invokerClass, String name, MethodType type) {
        MemberName member = new MemberName(invokerClass, name, type, REF_invokeStatic);
        //System.out.println("resolveInvokerMember => "+member);
        //for (Method m : invokerClass.getDeclaredMethods())  System.out.println("  "+m);
        try {
            member = MEMBERNAME_FACTORY.resolveOrFail(REF_invokeStatic, member, HOST_CLASS, ReflectiveOperationException.class);
        } catch (ReflectiveOperationException e) {
            throw newInternalError(e);
        }
        //System.out.println("resolveInvokerMember => "+member);
        return member;
    }

    /**
     * 设置类文件生成。
     */
    private void classFilePrologue() {
        final int NOT_ACC_PUBLIC = 0;  // 不是 ACC_PUBLIC
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, NOT_ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, className, null, superName, null);
        cw.visitSource(sourceFile, null);

        String invokerDesc = invokerType.toMethodDescriptorString();
        mv = cw.visitMethod(Opcodes.ACC_STATIC, invokerName, invokerDesc, null, null);
    }

    /**
     * 拆解类文件生成。
     */
    private void classFileEpilogue() {
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /*
     * 低级发射辅助函数。
     */
    private void emitConst(Object con) {
        if (con == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        if (con instanceof Integer) {
            emitIconstInsn((int) con);
            return;
        }
        if (con instanceof Long) {
            long x = (long) con;
            if (x == (short) x) {
                emitIconstInsn((int) x);
                mv.visitInsn(Opcodes.I2L);
                return;
            }
        }
        if (con instanceof Float) {
            float x = (float) con;
            if (x == (short) x) {
                emitIconstInsn((int) x);
                mv.visitInsn(Opcodes.I2F);
                return;
            }
        }
        if (con instanceof Double) {
            double x = (double) con;
            if (x == (short) x) {
                emitIconstInsn((int) x);
                mv.visitInsn(Opcodes.I2D);
                return;
            }
        }
        if (con instanceof Boolean) {
            emitIconstInsn((boolean) con ? 1 : 0);
            return;
        }
        // 穿透：
        mv.visitLdcInsn(con);
    }


                private void emitIconstInsn(int i) {
        int opcode;
        switch (i) {
        case 0:  opcode = Opcodes.ICONST_0;  break;
        case 1:  opcode = Opcodes.ICONST_1;  break;
        case 2:  opcode = Opcodes.ICONST_2;  break;
        case 3:  opcode = Opcodes.ICONST_3;  break;
        case 4:  opcode = Opcodes.ICONST_4;  break;
        case 5:  opcode = Opcodes.ICONST_5;  break;
        default:
            if (i == (byte) i) {
                mv.visitIntInsn(Opcodes.BIPUSH, i & 0xFF);
            } else if (i == (short) i) {
                mv.visitIntInsn(Opcodes.SIPUSH, (char) i);
            } else {
                mv.visitLdcInsn(i);
            }
            return;
        }
        mv.visitInsn(opcode);
    }

    /*
     * 注意：这些加载/存储方法使用 localsMap 来查找正确的索引！
     */
    private void emitLoadInsn(BasicType type, int index) {
        int opcode = loadInsnOpcode(type);
        mv.visitVarInsn(opcode, localsMap[index]);
    }

    private int loadInsnOpcode(BasicType type) throws InternalError {
        switch (type) {
            case I_TYPE: return Opcodes.ILOAD;
            case J_TYPE: return Opcodes.LLOAD;
            case F_TYPE: return Opcodes.FLOAD;
            case D_TYPE: return Opcodes.DLOAD;
            case L_TYPE: return Opcodes.ALOAD;
            default:
                throw new InternalError("未知类型: " + type);
        }
    }
    private void emitAloadInsn(int index) {
        emitLoadInsn(L_TYPE, index);
    }

    private void emitStoreInsn(BasicType type, int index) {
        int opcode = storeInsnOpcode(type);
        mv.visitVarInsn(opcode, localsMap[index]);
    }

    private int storeInsnOpcode(BasicType type) throws InternalError {
        switch (type) {
            case I_TYPE: return Opcodes.ISTORE;
            case J_TYPE: return Opcodes.LSTORE;
            case F_TYPE: return Opcodes.FSTORE;
            case D_TYPE: return Opcodes.DSTORE;
            case L_TYPE: return Opcodes.ASTORE;
            default:
                throw new InternalError("未知类型: " + type);
        }
    }
    private void emitAstoreInsn(int index) {
        emitStoreInsn(L_TYPE, index);
    }

    private byte arrayTypeCode(Wrapper elementType) {
        switch (elementType) {
            case BOOLEAN: return Opcodes.T_BOOLEAN;
            case BYTE:    return Opcodes.T_BYTE;
            case CHAR:    return Opcodes.T_CHAR;
            case SHORT:   return Opcodes.T_SHORT;
            case INT:     return Opcodes.T_INT;
            case LONG:    return Opcodes.T_LONG;
            case FLOAT:   return Opcodes.T_FLOAT;
            case DOUBLE:  return Opcodes.T_DOUBLE;
            case OBJECT:  return 0; // 代替 Opcodes.T_OBJECT
            default:      throw new InternalError();
        }
    }

    private int arrayInsnOpcode(byte tcode, int aaop) throws InternalError {
        assert(aaop == Opcodes.AASTORE || aaop == Opcodes.AALOAD);
        int xas;
        switch (tcode) {
            case Opcodes.T_BOOLEAN: xas = Opcodes.BASTORE; break;
            case Opcodes.T_BYTE:    xas = Opcodes.BASTORE; break;
            case Opcodes.T_CHAR:    xas = Opcodes.CASTORE; break;
            case Opcodes.T_SHORT:   xas = Opcodes.SASTORE; break;
            case Opcodes.T_INT:     xas = Opcodes.IASTORE; break;
            case Opcodes.T_LONG:    xas = Opcodes.LASTORE; break;
            case Opcodes.T_FLOAT:   xas = Opcodes.FASTORE; break;
            case Opcodes.T_DOUBLE:  xas = Opcodes.DASTORE; break;
            case 0:                 xas = Opcodes.AASTORE; break;
            default:      throw new InternalError();
        }
        return xas - Opcodes.AASTORE + aaop;
    }


    private void freeFrameLocal(int oldFrameLocal) {
        int i = indexForFrameLocal(oldFrameLocal);
        if (i < 0)  return;
        BasicType type = localTypes[i];
        int newFrameLocal = makeLocalTemp(type);
        mv.visitVarInsn(loadInsnOpcode(type), oldFrameLocal);
        mv.visitVarInsn(storeInsnOpcode(type), newFrameLocal);
        assert(localsMap[i] == oldFrameLocal);
        localsMap[i] = newFrameLocal;
        assert(indexForFrameLocal(oldFrameLocal) < 0);
    }
    private int indexForFrameLocal(int frameLocal) {
        for (int i = 0; i < localsMap.length; i++) {
            if (localsMap[i] == frameLocal && localTypes[i] != V_TYPE)
                return i;
        }
        return -1;
    }
    private int makeLocalTemp(BasicType type) {
        int frameLocal = localsMap[localsMap.length - 1];
        localsMap[localsMap.length - 1] = frameLocal + type.basicTypeSlots();
        return frameLocal;
    }

    /**
     * 发出装箱调用。
     *
     * @param wrapper 要装箱的原始类型类。
     */
    private void emitBoxing(Wrapper wrapper) {
        String owner = "java/lang/" + wrapper.wrapperType().getSimpleName();
        String name  = "valueOf";
        String desc  = "(" + wrapper.basicTypeChar() + ")L" + owner + ";";
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);
    }

    /**
     * 发出拆箱调用（加上前面的类型检查）。
     *
     * @param wrapper 要拆箱的包装类型类。
     */
    private void emitUnboxing(Wrapper wrapper) {
        String owner = "java/lang/" + wrapper.wrapperType().getSimpleName();
        String name  = wrapper.primitiveSimpleName() + "Value";
        String desc  = "()" + wrapper.basicTypeChar();
        emitReferenceCast(wrapper.wrapperType(), null);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, false);
    }

    /**
     * 发出隐式转换，用于必须是给定 pclass 的参数。
     * 通常这是一个空操作，除非 pclass 是子字类型或非 Object 或接口的引用。
     *
     * @param ptype 栈上值的类型
     * @param pclass 栈上所需值的类型
     * @param arg 栈上值的编译时表示（Node，常量）或 null 如果没有
     */
    private void emitImplicitConversion(BasicType ptype, Class<?> pclass, Object arg) {
        assert(basicType(pclass) == ptype);  // 装箱/拆箱由调用者处理
        if (pclass == ptype.basicTypeClass() && ptype != L_TYPE)
            return;   // 无需操作
        switch (ptype) {
            case L_TYPE:
                if (VerifyType.isNullConversion(Object.class, pclass, false)) {
                    if (PROFILE_LEVEL > 0)
                        emitReferenceCast(Object.class, arg);
                    return;
                }
                emitReferenceCast(pclass, arg);
                return;
            case I_TYPE:
                if (!VerifyType.isNullConversion(int.class, pclass, false))
                    emitPrimCast(ptype.basicTypeWrapper(), Wrapper.forPrimitiveType(pclass));
                return;
        }
        throw newInternalError("错误的隐式转换: tc="+ptype+": "+pclass);
    }


                /** 更新本地类类型映射。如果信息已经存在，则返回 true。 */
    private boolean assertStaticType(Class<?> cls, Name n) {
        int local = n.index();
        Class<?> aclass = localClasses[local];
        if (aclass != null && (aclass == cls || cls.isAssignableFrom(aclass))) {
            return true;  // 类型信息已经存在
        } else if (aclass == null || aclass.isAssignableFrom(cls)) {
            localClasses[local] = cls;  // 类型信息可以改进
        }
        return false;
    }

    private void emitReferenceCast(Class<?> cls, Object arg) {
        Name writeBack = null;  // 用于写回结果的本地变量
        if (arg instanceof Name) {
            Name n = (Name) arg;
            if (assertStaticType(cls, n))
                return;  // 此类型转换已经执行过
            if (lambdaForm.useCount(n) > 1) {
                // 该变量被使用多次。
                writeBack = n;
            }
        }
        if (isStaticallyNameable(cls)) {
            String sig = getInternalName(cls);
            mv.visitTypeInsn(Opcodes.CHECKCAST, sig);
        } else {
            mv.visitLdcInsn(constantPlaceholder(cls));
            mv.visitTypeInsn(Opcodes.CHECKCAST, CLS);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, MHI, "castReference", CLL_SIG, false);
            if (Object[].class.isAssignableFrom(cls))
                mv.visitTypeInsn(Opcodes.CHECKCAST, OBJARY);
            else if (PROFILE_LEVEL > 0)
                mv.visitTypeInsn(Opcodes.CHECKCAST, OBJ);
        }
        if (writeBack != null) {
            mv.visitInsn(Opcodes.DUP);
            emitAstoreInsn(writeBack.index());
        }
    }

    /**
     * 生成符合给定返回类型的实际返回指令。
     */
    private void emitReturnInsn(BasicType type) {
        int opcode;
        switch (type) {
        case I_TYPE:  opcode = Opcodes.IRETURN;  break;
        case J_TYPE:  opcode = Opcodes.LRETURN;  break;
        case F_TYPE:  opcode = Opcodes.FRETURN;  break;
        case D_TYPE:  opcode = Opcodes.DRETURN;  break;
        case L_TYPE:  opcode = Opcodes.ARETURN;  break;
        case V_TYPE:  opcode = Opcodes.RETURN;   break;
        default:
            throw new InternalError("未知的返回类型: " + type);
        }
        mv.visitInsn(opcode);
    }

    private static String getInternalName(Class<?> c) {
        if (c == Object.class)             return OBJ;
        else if (c == Object[].class)      return OBJARY;
        else if (c == Class.class)         return CLS;
        else if (c == MethodHandle.class)  return MH;
        assert(VerifyAccess.isTypeVisible(c, Object.class)) : c.getName();
        return c.getName().replace('.', '/');
    }

    /**
     * 为给定的 LambdaForm 生成自定义字节码。
     */
    static MemberName generateCustomizedCode(LambdaForm form, MethodType invokerType) {
        InvokerBytecodeGenerator g = new InvokerBytecodeGenerator("MH", form, invokerType);
        return g.loadMethod(g.generateCustomizedCodeBytes());
    }

    /** 生成代码以检查实际接收者和 LambdaForm 是否匹配 */
    private boolean checkActualReceiver() {
        // 期望栈顶有一个 MethodHandle 和实际接收者的 MethodHandle 在槽 #0
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, localsMap[0]);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, MHI, "assertSame", LLV_SIG, false);
        return true;
    }

    /**
     * 为传递的 {@link LambdaForm} 生成一个调用者方法。
     */
    private byte[] generateCustomizedCodeBytes() {
        classFilePrologue();

        // 在用户显示的回溯中抑制此方法。
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);

        // 标记此方法为编译的 LambdaForm
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Compiled;", true);

        if (lambdaForm.forceInline) {
            // 强制内联此调用者方法。
            mv.visitAnnotation("Ljava/lang/invoke/ForceInline;", true);
        } else {
            mv.visitAnnotation("Ljava/lang/invoke/DontInline;", true);
        }

        if (lambdaForm.customized != null) {
            // 由于 LambdaForm 是为特定的 MethodHandle 定制的，因此可以安全地将接收者 MethodHandle（在槽 #0）替换为嵌入的常量并使用它。
            // 这在某些情况下可以生成更高效的代码，因为嵌入的常量对于 JIT 编译器是编译时常量。
            mv.visitLdcInsn(constantPlaceholder(lambdaForm.customized));
            mv.visitTypeInsn(Opcodes.CHECKCAST, MH);
            assert(checkActualReceiver()); // 期望栈顶有一个 MethodHandle
            mv.visitVarInsn(Opcodes.ASTORE, localsMap[0]);
        }

        // 遍历表单的名称，为每个名称生成字节码指令
        // 从参数后的第一个名称开始遍历
        Name onStack = null;
        for (int i = lambdaForm.arity; i < lambdaForm.names.length; i++) {
            Name name = lambdaForm.names[i];

            emitStoreResult(onStack);
            onStack = name;  // 除非在下面的代码中被修改
            MethodHandleImpl.Intrinsic intr = name.function.intrinsicName();
            switch (intr) {
                case SELECT_ALTERNATIVE:
                    assert isSelectAlternative(i);
                    if (PROFILE_GWT) {
                        assert(name.arguments[0] instanceof Name &&
                               nameRefersTo((Name)name.arguments[0], MethodHandleImpl.class, "profileBoolean"));
                        mv.visitAnnotation("Ljava/lang/invoke/InjectedProfile;", true);
                    }
                    onStack = emitSelectAlternative(name, lambdaForm.names[i+1]);
                    i++;  // 跳过 selectAlternative 结果的 MH.invokeBasic
                    continue;
                case GUARD_WITH_CATCH:
                    assert isGuardWithCatch(i);
                    onStack = emitGuardWithCatch(i);
                    i = i+2; // 跳到 GWC 习惯用法的末尾
                    continue;
                case NEW_ARRAY:
                    Class<?> rtype = name.function.methodType().returnType();
                    if (isStaticallyNameable(rtype)) {
                        emitNewArray(name);
                        continue;
                    }
                    break;
                case ARRAY_LOAD:
                    emitArrayLoad(name);
                    continue;
                case ARRAY_STORE:
                    emitArrayStore(name);
                    continue;
                case IDENTITY:
                    assert(name.arguments.length == 1);
                    emitPushArguments(name);
                    continue;
                case ZERO:
                    assert(name.arguments.length == 0);
                    emitConst(name.type.basicTypeWrapper().zero());
                    continue;
                case NONE:
                    // 没有关联的内在函数
                    break;
                default:
                    throw newInternalError("未知的内在函数: "+intr);
            }


                        MemberName member = name.function.member();
            if (isStaticallyInvocable(member)) {
                emitStaticInvoke(member, name);
            } else {
                emitInvoke(name);
            }
        }

        // 返回语句
        emitReturn(onStack);

        classFileEpilogue();
        bogusMethod(lambdaForm);

        final byte[] classFile = cw.toByteArray();
        maybeDump(className, classFile);
        return classFile;
    }

    void emitArrayLoad(Name name)  { emitArrayOp(name, Opcodes.AALOAD);  }
    void emitArrayStore(Name name) { emitArrayOp(name, Opcodes.AASTORE); }

    void emitArrayOp(Name name, int arrayOpcode) {
        assert arrayOpcode == Opcodes.AALOAD || arrayOpcode == Opcodes.AASTORE;
        Class<?> elementType = name.function.methodType().parameterType(0).getComponentType();
        assert elementType != null;
        emitPushArguments(name);
        if (elementType.isPrimitive()) {
            Wrapper w = Wrapper.forPrimitiveType(elementType);
            arrayOpcode = arrayInsnOpcode(arrayTypeCode(w), arrayOpcode);
        }
        mv.visitInsn(arrayOpcode);
    }

    /**
     * 为给定的名称生成调用。
     */
    void emitInvoke(Name name) {
        assert(!isLinkerMethodInvoke(name));  // 应该为这些使用静态路径
        if (true) {
            // 推送接收者
            MethodHandle target = name.function.resolvedHandle;
            assert(target != null) : name.exprString();
            mv.visitLdcInsn(constantPlaceholder(target));
            emitReferenceCast(MethodHandle.class, target);
        } else {
            // 加载接收者
            emitAloadInsn(0);
            emitReferenceCast(MethodHandle.class, null);
            mv.visitFieldInsn(Opcodes.GETFIELD, MH, "form", LF_SIG);
            mv.visitFieldInsn(Opcodes.GETFIELD, LF, "names", LFN_SIG);
            // TODO 更多内容待添加
        }

        // 推送参数
        emitPushArguments(name);

        // 调用
        MethodType type = name.function.methodType();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH, "invokeBasic", type.basicType().toMethodDescriptorString(), false);
    }

    static private Class<?>[] STATICALLY_INVOCABLE_PACKAGES = {
        // 每个我们愿意静态绑定的包中的示例类：
        java.lang.Object.class,
        java.util.Arrays.class,
        sun.misc.Unsafe.class
        //MethodHandle.class 已经包含
    };

    static boolean isStaticallyInvocable(Name name) {
        return isStaticallyInvocable(name.function.member());
    }

    static boolean isStaticallyInvocable(MemberName member) {
        if (member == null)  return false;
        if (member.isConstructor())  return false;
        Class<?> cls = member.getDeclaringClass();
        if (cls.isArray() || cls.isPrimitive())
            return false;  // FIXME
        if (cls.isAnonymousClass() || cls.isLocalClass())
            return false;  // 某种形式的内部类
        if (cls.getClassLoader() != MethodHandle.class.getClassLoader())
            return false;  // 不在BCP上
        if (ReflectUtil.isVMAnonymousClass(cls)) // FIXME: 一旦添加支持的API，切换到支持的API
            return false;
        MethodType mtype = member.getMethodOrFieldType();
        if (!isStaticallyNameable(mtype.returnType()))
            return false;
        for (Class<?> ptype : mtype.parameterArray())
            if (!isStaticallyNameable(ptype))
                return false;
        if (!member.isPrivate() && VerifyAccess.isSamePackage(MethodHandle.class, cls))
            return true;   // 在java.lang.invoke包中
        if (member.isPublic() && isStaticallyNameable(cls))
            return true;
        return false;
    }

    static boolean isStaticallyNameable(Class<?> cls) {
        if (cls == Object.class)
            return true;
        while (cls.isArray())
            cls = cls.getComponentType();
        if (cls.isPrimitive())
            return true;  // 例如 int[].class
        if (ReflectUtil.isVMAnonymousClass(cls)) // FIXME: 一旦添加支持的API，切换到支持的API
            return false;
        // 可以使用VerifyAccess.isClassAccessible，但以下是一个安全的近似值
        if (cls.getClassLoader() != Object.class.getClassLoader())
            return false;
        if (VerifyAccess.isSamePackage(MethodHandle.class, cls))
            return true;
        if (!Modifier.isPublic(cls.getModifiers()))
            return false;
        for (Class<?> pkgcls : STATICALLY_INVOCABLE_PACKAGES) {
            if (VerifyAccess.isSamePackage(pkgcls, cls))
                return true;
        }
        return false;
    }

    void emitStaticInvoke(Name name) {
        emitStaticInvoke(name.function.member(), name);
    }

    /**
     * 为给定的名称生成调用，直接使用MemberName。
     */
    void emitStaticInvoke(MemberName member, Name name) {
        assert(member.equals(name.function.member()));
        Class<?> defc = member.getDeclaringClass();
        String cname = getInternalName(defc);
        String mname = member.getName();
        String mtype;
        byte refKind = member.getReferenceKind();
        if (refKind == REF_invokeSpecial) {
            // 为了通过验证器，我们需要在所有情况下将此转换为invokevirtual
            assert(member.canBeStaticallyBound()) : member;
            refKind = REF_invokeVirtual;
        }

        if (member.getDeclaringClass().isInterface() && refKind == REF_invokeVirtual) {
            // 在接口中声明的Object方法可以由JVM解析为invokevirtual类型。
            // 需要将其转换回invokeinterface以通过验证并使调用按预期工作。
            refKind = REF_invokeInterface;
        }

        // 推送参数
        emitPushArguments(name);

        // 调用
        if (member.isMethod()) {
            mtype = member.getMethodType().toMethodDescriptorString();
            mv.visitMethodInsn(refKindOpcode(refKind), cname, mname, mtype,
                               member.getDeclaringClass().isInterface());
        } else {
            mtype = MethodType.toFieldDescriptorString(member.getFieldType());
            mv.visitFieldInsn(refKindOpcode(refKind), cname, mname, mtype);
        }
        // 发出类型断言以避免后续的类型转换。
        if (name.type == L_TYPE) {
            Class<?> rtype = member.getInvocationType().returnType();
            assert(!rtype.isPrimitive());
            if (rtype != Object.class && !rtype.isInterface()) {
                assertStaticType(rtype, name);
            }
        }
    }


                void emitNewArray(Name name) throws InternalError {
        Class<?> rtype = name.function.methodType().returnType();
        if (name.arguments.length == 0) {
            // The array will be a constant.
            Object emptyArray;
            try {
                emptyArray = name.function.resolvedHandle.invoke();
            } catch (Throwable ex) {
                throw newInternalError(ex);
            }
            assert(java.lang.reflect.Array.getLength(emptyArray) == 0);
            assert(emptyArray.getClass() == rtype);  // exact typing
            mv.visitLdcInsn(constantPlaceholder(emptyArray));
            emitReferenceCast(rtype, emptyArray);
            return;
        }
        Class<?> arrayElementType = rtype.getComponentType();
        assert(arrayElementType != null);
        emitIconstInsn(name.arguments.length);
        int xas = Opcodes.AASTORE;
        if (!arrayElementType.isPrimitive()) {
            mv.visitTypeInsn(Opcodes.ANEWARRAY, getInternalName(arrayElementType));
        } else {
            byte tc = arrayTypeCode(Wrapper.forPrimitiveType(arrayElementType));
            xas = arrayInsnOpcode(tc, xas);
            mv.visitIntInsn(Opcodes.NEWARRAY, tc);
        }
        // store arguments
        for (int i = 0; i < name.arguments.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            emitIconstInsn(i);
            emitPushArgument(name, i);
            mv.visitInsn(xas);
        }
        // the array is left on the stack
        assertStaticType(rtype, name);
    }
    int refKindOpcode(byte refKind) {
        switch (refKind) {
        case REF_invokeVirtual:      return Opcodes.INVOKEVIRTUAL;
        case REF_invokeStatic:       return Opcodes.INVOKESTATIC;
        case REF_invokeSpecial:      return Opcodes.INVOKESPECIAL;
        case REF_invokeInterface:    return Opcodes.INVOKEINTERFACE;
        case REF_getField:           return Opcodes.GETFIELD;
        case REF_putField:           return Opcodes.PUTFIELD;
        case REF_getStatic:          return Opcodes.GETSTATIC;
        case REF_putStatic:          return Opcodes.PUTSTATIC;
        }
        throw new InternalError("refKind="+refKind);
    }

    /**
     * Check if MemberName is a call to a method named {@code name} in class {@code declaredClass}.
     */
    private boolean memberRefersTo(MemberName member, Class<?> declaringClass, String name) {
        return member != null &&
               member.getDeclaringClass() == declaringClass &&
               member.getName().equals(name);
    }
    private boolean nameRefersTo(Name name, Class<?> declaringClass, String methodName) {
        return name.function != null &&
               memberRefersTo(name.function.member(), declaringClass, methodName);
    }

    /**
     * Check if MemberName is a call to MethodHandle.invokeBasic.
     */
    private boolean isInvokeBasic(Name name) {
        if (name.function == null)
            return false;
        if (name.arguments.length < 1)
            return false;  // must have MH argument
        MemberName member = name.function.member();
        return memberRefersTo(member, MethodHandle.class, "invokeBasic") &&
               !member.isPublic() && !member.isStatic();
    }

    /**
     * Check if MemberName is a call to MethodHandle.linkToStatic, etc.
     */
    private boolean isLinkerMethodInvoke(Name name) {
        if (name.function == null)
            return false;
        if (name.arguments.length < 1)
            return false;  // must have MH argument
        MemberName member = name.function.member();
        return member != null &&
               member.getDeclaringClass() == MethodHandle.class &&
               !member.isPublic() && member.isStatic() &&
               member.getName().startsWith("linkTo");
    }

    /**
     * Check if i-th name is a call to MethodHandleImpl.selectAlternative.
     */
    private boolean isSelectAlternative(int pos) {
        // selectAlternative idiom:
        //   t_{n}:L=MethodHandleImpl.selectAlternative(...)
        //   t_{n+1}:?=MethodHandle.invokeBasic(t_{n}, ...)
        if (pos+1 >= lambdaForm.names.length)  return false;
        Name name0 = lambdaForm.names[pos];
        Name name1 = lambdaForm.names[pos+1];
        return nameRefersTo(name0, MethodHandleImpl.class, "selectAlternative") &&
               isInvokeBasic(name1) &&
               name1.lastUseIndex(name0) == 0 &&        // t_{n+1}:?=MethodHandle.invokeBasic(t_{n}, ...)
               lambdaForm.lastUseIndex(name0) == pos+1; // t_{n} is local: used only in t_{n+1}
    }

    /**
     * Check if i-th name is a start of GuardWithCatch idiom.
     */
    private boolean isGuardWithCatch(int pos) {
        // GuardWithCatch idiom:
        //   t_{n}:L=MethodHandle.invokeBasic(...)
        //   t_{n+1}:L=MethodHandleImpl.guardWithCatch(*, *, *, t_{n});
        //   t_{n+2}:?=MethodHandle.invokeBasic(t_{n+1})
        if (pos+2 >= lambdaForm.names.length)  return false;
        Name name0 = lambdaForm.names[pos];
        Name name1 = lambdaForm.names[pos+1];
        Name name2 = lambdaForm.names[pos+2];
        return nameRefersTo(name1, MethodHandleImpl.class, "guardWithCatch") &&
               isInvokeBasic(name0) &&
               isInvokeBasic(name2) &&
               name1.lastUseIndex(name0) == 3 &&          // t_{n+1}:L=MethodHandleImpl.guardWithCatch(*, *, *, t_{n});
               lambdaForm.lastUseIndex(name0) == pos+1 && // t_{n} is local: used only in t_{n+1}
               name2.lastUseIndex(name1) == 1 &&          // t_{n+2}:?=MethodHandle.invokeBasic(t_{n+1})
               lambdaForm.lastUseIndex(name1) == pos+2;   // t_{n+1} is local: used only in t_{n+2}
    }

    /**
     * Emit bytecode for the selectAlternative idiom.
     *
     * The pattern looks like (Cf. MethodHandleImpl.makeGuardWithTest):
     * <blockquote><pre>{@code
     *   Lambda(a0:L,a1:I)=>{
     *     t2:I=foo.test(a1:I);
     *     t3:L=MethodHandleImpl.selectAlternative(t2:I,(MethodHandle(int)int),(MethodHandle(int)int));
     *     t4:I=MethodHandle.invokeBasic(t3:L,a1:I);t4:I}
     * }</pre></blockquote>
     */
    private Name emitSelectAlternative(Name selectAlternativeName, Name invokeBasicName) {
        assert isStaticallyInvocable(invokeBasicName);


                    Name receiver = (Name) invokeBasicName.arguments[0];

        Label L_fallback = new Label();
        Label L_done     = new Label();

        // 加载测试结果
        emitPushArgument(selectAlternativeName, 0);

        // if_icmpne L_fallback
        mv.visitJumpInsn(Opcodes.IFEQ, L_fallback);

        // 调用 selectAlternativeName.arguments[1]
        Class<?>[] preForkClasses = localClasses.clone();
        emitPushArgument(selectAlternativeName, 1);  // 获取 selectAlternative 的第二个参数
        emitAstoreInsn(receiver.index());  // 将 MH 存储在接收者槽中
        emitStaticInvoke(invokeBasicName);

        // 跳转到 L_done
        mv.visitJumpInsn(Opcodes.GOTO, L_done);

        // L_fallback:
        mv.visitLabel(L_fallback);

        // 调用 selectAlternativeName.arguments[2]
        System.arraycopy(preForkClasses, 0, localClasses, 0, preForkClasses.length);
        emitPushArgument(selectAlternativeName, 2);  // 获取 selectAlternative 的第三个参数
        emitAstoreInsn(receiver.index());  // 将 MH 存储在接收者槽中
        emitStaticInvoke(invokeBasicName);

        // L_done:
        mv.visitLabel(L_done);
        // 暂时不合并类型状态；仅重置为支配状态
        System.arraycopy(preForkClasses, 0, localClasses, 0, preForkClasses.length);

        return invokeBasicName;  // 返回栈上的内容
    }

    /**
      * 生成 guardWithCatch 习惯用法的字节码。
      *
      * 该模式看起来像（参见 MethodHandleImpl.makeGuardWithCatch）：
      * <blockquote><pre>{@code
      *  guardWithCatch=Lambda(a0:L,a1:L,a2:L,a3:L,a4:L,a5:L,a6:L,a7:L)=>{
      *    t8:L=MethodHandle.invokeBasic(a4:L,a6:L,a7:L);
      *    t9:L=MethodHandleImpl.guardWithCatch(a1:L,a2:L,a3:L,t8:L);
      *   t10:I=MethodHandle.invokeBasic(a5:L,t9:L);t10:I}
      * }</pre></blockquote>
      *
      * 它被编译成等效于以下代码的字节码：
      * <blockquote><pre>{@code
      *  try {
      *      return a1.invokeBasic(a6, a7);
      *  } catch (Throwable e) {
      *      if (!a2.isInstance(e)) throw e;
      *      return a3.invokeBasic(ex, a6, a7);
      *  }}
      */
    private Name emitGuardWithCatch(int pos) {
        Name args    = lambdaForm.names[pos];
        Name invoker = lambdaForm.names[pos+1];
        Name result  = lambdaForm.names[pos+2];

        Label L_startBlock = new Label();
        Label L_endBlock = new Label();
        Label L_handler = new Label();
        Label L_done = new Label();

        Class<?> returnType = result.function.resolvedHandle.type().returnType();
        MethodType type = args.function.resolvedHandle.type()
                              .dropParameterTypes(0,1)
                              .changeReturnType(returnType);

        mv.visitTryCatchBlock(L_startBlock, L_endBlock, L_handler, "java/lang/Throwable");

        // 正常情况
        mv.visitLabel(L_startBlock);
        // 加载目标
        emitPushArgument(invoker, 0);
        emitPushArguments(args, 1); // 跳过第一个参数：方法句柄
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH, "invokeBasic", type.basicType().toMethodDescriptorString(), false);
        mv.visitLabel(L_endBlock);
        mv.visitJumpInsn(Opcodes.GOTO, L_done);

        // 异常情况
        mv.visitLabel(L_handler);

        // 检查异常类型
        mv.visitInsn(Opcodes.DUP);
        // 加载异常类
        emitPushArgument(invoker, 1);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "isInstance", "(Ljava/lang/Object;)Z", false);
        Label L_rethrow = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, L_rethrow);

        // 调用捕获器
        // 加载捕获器
        emitPushArgument(invoker, 2);
        mv.visitInsn(Opcodes.SWAP);
        emitPushArguments(args, 1); // 跳过第一个参数：方法句柄
        MethodType catcherType = type.insertParameterTypes(0, Throwable.class);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH, "invokeBasic", catcherType.basicType().toMethodDescriptorString(), false);
        mv.visitJumpInsn(Opcodes.GOTO, L_done);

        mv.visitLabel(L_rethrow);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitLabel(L_done);

        return result;
    }

    private void emitPushArguments(Name args) {
        emitPushArguments(args, 0);
    }

    private void emitPushArguments(Name args, int start) {
        for (int i = start; i < args.arguments.length; i++) {
            emitPushArgument(args, i);
        }
    }

    private void emitPushArgument(Name name, int paramIndex) {
        Object arg = name.arguments[paramIndex];
        Class<?> ptype = name.function.methodType().parameterType(paramIndex);
        emitPushArgument(ptype, arg);
    }

    private void emitPushArgument(Class<?> ptype, Object arg) {
        BasicType bptype = basicType(ptype);
        if (arg instanceof Name) {
            Name n = (Name) arg;
            emitLoadInsn(n.type, n.index());
            emitImplicitConversion(n.type, ptype, n);
        } else if ((arg == null || arg instanceof String) && bptype == L_TYPE) {
            emitConst(arg);
        } else {
            if (Wrapper.isWrapperType(arg.getClass()) && bptype != L_TYPE) {
                emitConst(arg);
            } else {
                mv.visitLdcInsn(constantPlaceholder(arg));
                emitImplicitConversion(L_TYPE, ptype, arg);
            }
        }
    }

    /**
     * 如果必要，将名称存储到其局部变量中。
     */
    private void emitStoreResult(Name name) {
        if (name != null && name.type != V_TYPE) {
            // 非 void：实际赋值
            emitStoreInsn(name.type, name.index());
        }
    }

    /**
     * 从 LF 调用者生成返回语句。如果需要，将结果类型转换为正确的返回类型。
     */
    private void emitReturn(Name onStack) {
        // 返回语句
        Class<?> rclass = invokerType.returnType();
        BasicType rtype = lambdaForm.returnType();
        assert(rtype == basicType(rclass));  // 必须一致
        if (rtype == V_TYPE) {
            // void
            mv.visitInsn(Opcodes.RETURN);
            // 不管 rclass 是什么；JVM 会丢弃任何值
        } else {
            LambdaForm.Name rn = lambdaForm.names[lambdaForm.result];


                        // 将返回值放在堆栈上，如果它还没有在那里
            if (rn != onStack) {
                emitLoadInsn(rtype, lambdaForm.result);
            }

            emitImplicitConversion(rtype, rclass, rn);

            // 生成实际的返回语句
            emitReturnInsn(rtype);
        }
    }

    /**
     * 发射一个类型转换字节码，从 "from" 转换到 "to"。
     */
    private void emitPrimCast(Wrapper from, Wrapper to) {
        // 这是方法。
        // -   表示禁止
        // <-> 表示隐式
        //      to ----> boolean  byte     short    char     int      long     float    double
        // from boolean    <->        -        -        -        -        -        -        -
        //      byte        -       <->       i2s      i2c      <->      i2l      i2f      i2d
        //      short       -       i2b       <->      i2c      <->      i2l      i2f      i2d
        //      char        -       i2b       i2s      <->      <->      i2l      i2f      i2d
        //      int         -       i2b       i2s      i2c      <->      i2l      i2f      i2d
        //      long        -     l2i,i2b   l2i,i2s  l2i,i2c    l2i      <->      l2f      l2d
        //      float       -     f2i,i2b   f2i,i2s  f2i,i2c    f2i      f2l      <->      f2d
        //      double      -     d2i,i2b   d2i,i2s  d2i,i2c    d2i      d2l      d2f      <->
        if (from == to) {
            // 不需要转换，无论如何应该是死代码
            return;
        }
        if (from.isSubwordOrInt()) {
            // 从 {byte,short,char,int} 转换到任何类型
            emitI2X(to);
        } else {
            // 从 {long,float,double} 转换到任何类型
            if (to.isSubwordOrInt()) {
                // 转换到 {byte,short,char,int}
                emitX2I(from);
                if (to.bitWidth() < 32) {
                    // 除了 int 以外的目标需要另一个转换
                    emitI2X(to);
                }
            } else {
                // 转换到 {long,float,double} - 这是冗长的
                boolean error = false;
                switch (from) {
                case LONG:
                    switch (to) {
                    case FLOAT:   mv.visitInsn(Opcodes.L2F);  break;
                    case DOUBLE:  mv.visitInsn(Opcodes.L2D);  break;
                    default:      error = true;               break;
                    }
                    break;
                case FLOAT:
                    switch (to) {
                    case LONG :   mv.visitInsn(Opcodes.F2L);  break;
                    case DOUBLE:  mv.visitInsn(Opcodes.F2D);  break;
                    default:      error = true;               break;
                    }
                    break;
                case DOUBLE:
                    switch (to) {
                    case LONG :   mv.visitInsn(Opcodes.D2L);  break;
                    case FLOAT:   mv.visitInsn(Opcodes.D2F);  break;
                    default:      error = true;               break;
                    }
                    break;
                default:
                    error = true;
                    break;
                }
                if (error) {
                    throw new IllegalStateException("未处理的原始类型转换: " + from + "2" + to);
                }
            }
        }
    }

    private void emitI2X(Wrapper type) {
        switch (type) {
        case BYTE:    mv.visitInsn(Opcodes.I2B);  break;
        case SHORT:   mv.visitInsn(Opcodes.I2S);  break;
        case CHAR:    mv.visitInsn(Opcodes.I2C);  break;
        case INT:     /* 无操作 */                break;
        case LONG:    mv.visitInsn(Opcodes.I2L);  break;
        case FLOAT:   mv.visitInsn(Opcodes.I2F);  break;
        case DOUBLE:  mv.visitInsn(Opcodes.I2D);  break;
        case BOOLEAN:
            // 为了与 ValueConversions 和 explicitCastArguments 兼容：
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IAND);
            break;
        default:   throw new InternalError("未知类型: " + type);
        }
    }

    private void emitX2I(Wrapper type) {
        switch (type) {
        case LONG:    mv.visitInsn(Opcodes.L2I);  break;
        case FLOAT:   mv.visitInsn(Opcodes.F2I);  break;
        case DOUBLE:  mv.visitInsn(Opcodes.D2I);  break;
        default:      throw new InternalError("未知类型: " + type);
        }
    }

    /**
     * 生成一个 LambdaForm.vmentry，调用 interpretWithArguments 的字节码。
     */
    static MemberName generateLambdaFormInterpreterEntryPoint(String sig) {
        assert(isValidSignature(sig));
        String name = "interpret_"+signatureReturn(sig).basicTypeChar();
        MethodType type = signatureType(sig);  // sig 包括前导参数
        type = type.changeParameterType(0, MethodHandle.class);
        InvokerBytecodeGenerator g = new InvokerBytecodeGenerator("LFI", name, type);
        return g.loadMethod(g.generateLambdaFormInterpreterEntryPointBytes());
    }

    private byte[] generateLambdaFormInterpreterEntryPointBytes() {
        classFilePrologue();

        // 在用户显示的回溯中抑制此方法。
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);

        // 不要内联解释器入口。
        mv.visitAnnotation("Ljava/lang/invoke/DontInline;", true);

        // 创建参数数组
        emitIconstInsn(invokerType.parameterCount());
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

        // 填充参数数组
        for (int i = 0; i < invokerType.parameterCount(); i++) {
            Class<?> ptype = invokerType.parameterType(i);
            mv.visitInsn(Opcodes.DUP);
            emitIconstInsn(i);
            emitLoadInsn(basicType(ptype), i);
            // 如果是原始类型则装箱
            if (ptype.isPrimitive()) {
                emitBoxing(Wrapper.forPrimitiveType(ptype));
            }
            mv.visitInsn(Opcodes.AASTORE);
        }
        // 调用
        emitAloadInsn(0);
        mv.visitFieldInsn(Opcodes.GETFIELD, MH, "form", "Ljava/lang/invoke/LambdaForm;");
        mv.visitInsn(Opcodes.SWAP);  // 交换 form 和数组；避免局部变量
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LF, "interpretWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", false);


                    // 可能需要拆箱
        Class<?> rtype = invokerType.returnType();
        if (rtype.isPrimitive() && rtype != void.class) {
            emitUnboxing(Wrapper.forPrimitiveType(rtype));
        }

        // 返回语句
        emitReturnInsn(basicType(rtype));

        classFileEpilogue();
        bogusMethod(invokerType);

        final byte[] classFile = cw.toByteArray();
        maybeDump(className, classFile);
        return classFile;
    }

    /**
     * 生成一个 NamedFunction 调用者的字节码。
     */
    static MemberName generateNamedFunctionInvoker(MethodTypeForm typeForm) {
        MethodType invokerType = NamedFunction.INVOKER_METHOD_TYPE;
        String invokerName = "invoke_" + shortenSignature(basicTypeSignature(typeForm.erasedType()));
        InvokerBytecodeGenerator g = new InvokerBytecodeGenerator("NFI", invokerName, invokerType);
        return g.loadMethod(g.generateNamedFunctionInvokerImpl(typeForm));
    }

    private byte[] generateNamedFunctionInvokerImpl(MethodTypeForm typeForm) {
        MethodType dstType = typeForm.erasedType();
        classFilePrologue();

        // 在用户显示的堆栈跟踪中抑制此方法。
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);

        // 强制内联此调用者方法。
        mv.visitAnnotation("Ljava/lang/invoke/ForceInline;", true);

        // 加载接收者
        emitAloadInsn(0);

        // 从数组中加载参数
        for (int i = 0; i < dstType.parameterCount(); i++) {
            emitAloadInsn(1);
            emitIconstInsn(i);
            mv.visitInsn(Opcodes.AALOAD);

            // 可能需要拆箱
            Class<?> dptype = dstType.parameterType(i);
            if (dptype.isPrimitive()) {
                Class<?> sptype = dstType.basicType().wrap().parameterType(i);
                Wrapper dstWrapper = Wrapper.forBasicType(dptype);
                Wrapper srcWrapper = dstWrapper.isSubwordOrInt() ? Wrapper.INT : dstWrapper;  // 从 int 窄化子字
                emitUnboxing(srcWrapper);
                emitPrimCast(srcWrapper, dstWrapper);
            }
        }

        // 调用
        String targetDesc = dstType.basicType().toMethodDescriptorString();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MH, "invokeBasic", targetDesc, false);

        // 封装基本类型
        Class<?> rtype = dstType.returnType();
        if (rtype != void.class && rtype.isPrimitive()) {
            Wrapper srcWrapper = Wrapper.forBasicType(rtype);
            Wrapper dstWrapper = srcWrapper.isSubwordOrInt() ? Wrapper.INT : srcWrapper;  // 将子字扩展到 int
            // 不允许布尔类型转换
            emitPrimCast(srcWrapper, dstWrapper);
            emitBoxing(dstWrapper);
        }

        // 如果返回类型是 void，则返回一个 null 引用。
        if (rtype == void.class) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
        emitReturnInsn(L_TYPE);  // 注意：NamedFunction 调用者总是返回一个引用值。

        classFileEpilogue();
        bogusMethod(dstType);

        final byte[] classFile = cw.toByteArray();
        maybeDump(className, classFile);
        return classFile;
    }

    /**
     * 生成一个仅加载一些字符串常量的虚拟方法。这是为了将常量放入常量池中，以便调试。
     */
    private void bogusMethod(Object... os) {
        if (DUMP_CLASS_FILES) {
            mv = cw.visitMethod(Opcodes.ACC_STATIC, "dummy", "()V", null, null);
            for (Object o : os) {
                mv.visitLdcInsn(o.toString());
                mv.visitInsn(Opcodes.POP);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
