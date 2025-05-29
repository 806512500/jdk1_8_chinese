/*
 * 版权所有 (c) 2008, 2012, Oracle 和/或其附属公司。保留所有权利。
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

import static java.lang.invoke.LambdaForm.BasicType.*;
import static java.lang.invoke.MethodHandleStatics.*;

/**
 * 一个其行为仅由其 LambdaForm 确定的方法句柄。
 * @author jrose
 */
final class SimpleMethodHandle extends BoundMethodHandle {
    private SimpleMethodHandle(MethodType type, LambdaForm form) {
        super(type, form);
    }

    /*non-public*/ static BoundMethodHandle make(MethodType type, LambdaForm form) {
        return new SimpleMethodHandle(type, form);
    }

    /*non-public*/ static final SpeciesData SPECIES_DATA = SpeciesData.EMPTY;

    /*non-public*/ public SpeciesData speciesData() {
            return SPECIES_DATA;
    }

    @Override
    /*non-public*/ BoundMethodHandle copyWith(MethodType mt, LambdaForm lf) {
        return make(mt, lf);
    }

    @Override
    String internalProperties() {
        return "\n& Class="+getClass().getSimpleName();
    }

    @Override
    /*non-public*/ public int fieldCount() {
        return 0;
    }

    @Override
    /*non-public*/ final BoundMethodHandle copyWithExtendL(MethodType mt, LambdaForm lf, Object narg) {
        return BoundMethodHandle.bindSingle(mt, lf, narg); // 使用已知的快速路径。
    }
    @Override
    /*non-public*/ final BoundMethodHandle copyWithExtendI(MethodType mt, LambdaForm lf, int narg) {
        try {
            return (BoundMethodHandle) SPECIES_DATA.extendWith(I_TYPE).constructor().invokeBasic(mt, lf, narg);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
    }
    @Override
    /*non-public*/ final BoundMethodHandle copyWithExtendJ(MethodType mt, LambdaForm lf, long narg) {
        try {
            return (BoundMethodHandle) SPECIES_DATA.extendWith(J_TYPE).constructor().invokeBasic(mt, lf, narg);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
    }
    @Override
    /*non-public*/ final BoundMethodHandle copyWithExtendF(MethodType mt, LambdaForm lf, float narg) {
        try {
            return (BoundMethodHandle) SPECIES_DATA.extendWith(F_TYPE).constructor().invokeBasic(mt, lf, narg);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
    }
    @Override
    /*non-public*/ final BoundMethodHandle copyWithExtendD(MethodType mt, LambdaForm lf, double narg) {
        try {
            return (BoundMethodHandle) SPECIES_DATA.extendWith(D_TYPE).constructor().invokeBasic(mt, lf, narg);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
    }
}
