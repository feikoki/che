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
package org.eclipse.che.ide.editor.synchronization.workingCopy;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto.Type;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.api.project.shared.dto.EditorChangesDto.Type.INSERT;
import static org.eclipse.che.api.project.shared.dto.EditorChangesDto.Type.REMOVE;

/**
 * Default implementation of {@link EditorWorkingCopySynchronizer} which provides synchronization working copy on server side.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorWorkingCopySynchronizerImpl implements EditorWorkingCopySynchronizer {
    private static final String ENDPOINT_ID                   = "ws-agent";
    private static final String WORKING_COPY_ERROR_METHOD     = "track:editor-working-copy-error";
    private static final String EDITOR_CONTENT_CHANGES_METHOD = "track:editor-content-changes";

    private DtoFactory         dtoFactory;
    private RequestTransmitter requestTransmitter;

    @Inject
    public EditorWorkingCopySynchronizerImpl(DtoFactory dtoFactory,
                                             RequestTransmitter requestTransmitter) {
        this.dtoFactory = dtoFactory;
        this.requestTransmitter = requestTransmitter;
    }

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(WORKING_COPY_ERROR_METHOD)
                    .paramsAsDto(ServerError.class)
                    .noResult()
                    .withConsumer(this::onError);
    }

    public JsonRpcPromise<Void> synchronize(String filePath, String projectPath, DirtyRegion dirtyRegion) {
        Type type = dirtyRegion.getType().equals(DirtyRegion.INSERT) ? INSERT : REMOVE;
        EditorChangesDto changes = dtoFactory.createDto(EditorChangesDto.class)
                                             .withType(type)
                                             .withProjectPath(projectPath)
                                             .withFileLocation(filePath)
                                             .withOffset(dirtyRegion.getOffset())
                                             .withText(dirtyRegion.getText());

        int length = dirtyRegion.getLength();
        if (DirtyRegion.REMOVE.equals(dirtyRegion.getType())) {
            changes.withRemovedCharCount(length);
        } else {
            changes.withLength(length);
        }

        return requestTransmitter.newRequest()
                          .endpointId(ENDPOINT_ID)
                          .methodName(EDITOR_CONTENT_CHANGES_METHOD)
                          .paramsAsDto(changes)
                          .sendAndReceiveResultAsEmpty();
    }

    private void onError(ServerError error) {
        Log.error(getClass(), error.getMessage());
    }
}
