/*
 * ******************************************************************************
 *  * Copyright (c) 2012-2017 Red Hat, Inc.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *   Red Hat, Inc. - initial API and implementation
 *   ******************************************************************************
 */
package org.eclipse.che.ide.ext.git.client.oldContentPopup;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * @author Evgen Vidolob
 */
@ImplementedBy(ContentViewImpl.class)
public interface ContentView extends View<ContentView.ActionDelegate> {
    void show(String url, int x, int y);

    interface ActionDelegate{

        void onCloseView();
    }
}
