/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jsonrpc;

import com.google.gwt.user.client.Timer;

import org.eclipse.che.api.core.jsonrpc.commons.TimeoutActionRunner;

import javax.inject.Singleton;

@Singleton
public class ClientSideTimeoutActionRunner implements TimeoutActionRunner {

    @Override
    public void schedule(int timeoutInMillis, Runnable runnable) {
        new Timer() {
            @Override
            public void run() {
                runnable.run();
            }
        }.schedule(timeoutInMillis);
    }
}
