/*
 * Copyright (c) 2002, 2021, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.security.cert;

import java.util.*;

import sun.security.provider.certpath.CertPathHelper;

import sun.security.x509.GeneralNameInterface;

/**
 * 辅助类，允许 Sun CertPath 提供程序访问 CertPath 框架中的实现依赖 API。
 *
 * @author Andreas Sterbenz
 */
class CertPathHelperImpl extends CertPathHelper {

    private CertPathHelperImpl() {
        // empty
    }

    /**
     * 初始化辅助框架。此方法必须从每个类的静态初始化器中调用，这些类是此类方法的目标之一。
     * 这确保了在从 Sun 提供程序进行隧道调用之前，辅助程序已初始化。
     */
    synchronized static void initialize() {
        if (CertPathHelper.instance == null) {
            CertPathHelper.instance = new CertPathHelperImpl();
        }
    }

    protected void implSetPathToNames(X509CertSelector sel,
            Set<GeneralNameInterface> names) {
        sel.setPathToNamesInternal(names);
    }

    protected void implSetDateAndTime(X509CRLSelector sel, Date date, long skew) {
        sel.setDateAndTime(date, skew);
    }

    protected boolean implIsJdkCA(TrustAnchor anchor) {
        return anchor.isJdkCA();
}
}
