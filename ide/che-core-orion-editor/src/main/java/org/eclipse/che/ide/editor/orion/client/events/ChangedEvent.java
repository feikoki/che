/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.events;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;

/**
 * Event for scrolls.
 */
public class ChangedEvent extends GwtEvent<ChangedHandler> {
    /** The type instance for this event. */
    public static final Type<ChangedHandler> TYPE = new Type<>();
    private final OrionEditorPresenter orionEditorPresenter;


    public ChangedEvent(OrionEditorPresenter orionEditorPresenter) {
        this.orionEditorPresenter = orionEditorPresenter;
    }

    @Override
    public Type<ChangedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final ChangedHandler handler) {
        handler.onChanged(this);
    }

    public OrionEditorPresenter getOrionEditorPresenter() {
        return orionEditorPresenter;
    }
}
