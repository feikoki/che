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
package org.eclipse.che.ide.ext.git.client;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.git.shared.Edition;
import org.eclipse.che.api.git.shared.GitChangeEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.vcs.HasVcsMarkRender;
import org.eclipse.che.ide.api.vcs.VcsMarkRender;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.editor.orion.client.events.NewLineAddedEvent;
import org.eclipse.che.ide.editor.orion.client.events.OnNewLineAddedHandler;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.util.List;

import static org.eclipse.che.ide.api.vcs.VcsStatus.ADDED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.UNTRACKED;

/**
 * Receives and handel's git changes notifications caught by server side VFS file watching system.
 *
 * @author Igor Vinokur
 */
@Singleton
public class GitChangesHandler {

    private final AppContext                         appContext;
    private final Provider<EditorAgent>              editorAgentProvider;
    private final Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider;
    private final Provider<EditorMultiPartStack>     multiPartStackProvider;

    @Inject
    public GitChangesHandler(RequestHandlerConfigurator configurator,
                             EventBus eventBus,
                             GitServiceClient gitServiceClient,
                             AppContext appContext,
                             Provider<EditorAgent> editorAgentProvider,
                             Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider,
                             Provider<EditorMultiPartStack> multiPartStackProvider) {
        this.appContext = appContext;
        this.editorAgentProvider = editorAgentProvider;
        this.projectExplorerPresenterProvider = projectExplorerPresenterProvider;
        this.multiPartStackProvider = multiPartStackProvider;

        eventBus.addHandler(NewLineAddedEvent.TYPE, new OnNewLineAddedHandler() {
            @Override
            public void onNewLineAdded(NewLineAddedEvent event) {
                event.getOrionEditorPresenter().getOrCreateVcsMarkRender().then(new Operation<VcsMarkRender>() {
                    @Override
                    public void apply(VcsMarkRender arg) throws OperationException {
                        arg.handleEnter(event.getLine());
                    }
                });
            }
        });

        eventBus.addHandler(EditorOpenedEvent.TYPE,
                            event -> ((HasVcsMarkRender)event.getEditor())
                                    .getOrCreateVcsMarkRender()
                                    .then(render -> {
                                        gitServiceClient.getEditions(event.getFile()
                                                                          .getLocation()
                                                                          .uptoSegment(1),
                                                                     event.getFile()
                                                                          .getLocation()
                                                                          .removeFirstSegments(1)
                                                                          .toString())
                                                        .then(edition -> {
                                                            render.clearMarks();
                                                            handleEdition(edition, render);
                                                        });
                                    }));

        configureHandler(configurator);
    }

    private void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event/git-change")
                    .paramsAsDto(GitChangeEventDto.class)
                    .noResult()
                    .withBiConsumer(this::apply);

        configurator.newConfiguration()
                    .methodName("event/git-index")
                    .paramsAsDto(Status.class)
                    .noResult()
                    .withBiConsumer(this::apply);
    }

    public void apply(String endpointId, GitChangeEventDto dto) {
        Tree tree = projectExplorerPresenterProvider.get().getTree();
        tree.getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> node instanceof FileNode && ((ResourceNode)node).getData()
                                                                            .getLocation()
                                                                            .equals(Path.valueOf(dto.getPath())))
            .forEach(node -> {
                ((ResourceNode)node).getData().asFile().setVcsStatus(VcsStatus.from(dto.getType().toString()));
                tree.refresh(node);
            });

        editorAgentProvider.get()
                           .getOpenedEditors()
                           .stream()
                           .filter(editor -> editor.getEditorInput().getFile().getLocation().equals(Path.valueOf(dto.getPath())))
                           .forEach(editor -> {
                               VcsStatus vcsStatus = VcsStatus.from(dto.getType().toString());
                               EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
                               if (vcsStatus != null) {
                                   tab.setTitleColor(vcsStatus.getColor());
                               }
                           });

        appContext.getWorkspaceRoot().synchronize();

        editorAgentProvider.get()
                           .getOpenedEditors()
                           .forEach(editor -> ((HasVcsMarkRender)editor).getOrCreateVcsMarkRender()
                                                                        .then(arg -> {
                                                                            arg.clearMarks();
                                                                            handleEdition(dto.getEditions(), arg);
                                                                        }));
    }

    private void handleEdition(List<Edition> editions, VcsMarkRender render) {
        editions.forEach(edition -> {
            for (int i = edition.getBeginLine(); i <= edition.getEndLine(); i++) {
                switch (edition.getType()) {
                    case "INSERT": {
                        render.addVcsMarkAdded(i);
                        continue;
                    }
                    case "REPLACE": {
                        render.addVcsMarkModified(i);
                        continue;
                    }
                    case "DELETE": {
                        render.addVcsMarkDeleted(i);
                    }
                }
            }
        });
    }

    public void apply(String endpointId, Status status) {
        Tree tree = projectExplorerPresenterProvider.get().getTree();
        tree.getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> node instanceof FileNode)
            .forEach(node -> {
                Resource resource = ((ResourceNode)node).getData();
                File file = resource.asFile();
                String nodeLocation = resource.getLocation().removeFirstSegments(1).toString();
                if (status.getUntracked().contains(nodeLocation) && file.getVcsStatus() != UNTRACKED) {
                    file.setVcsStatus(UNTRACKED);
                    tree.refresh(node);
                } else if (status.getModified().contains(nodeLocation) || status.getChanged().contains(nodeLocation)) {
                    file.setVcsStatus(MODIFIED);
                    tree.refresh(node);
                } else if (status.getAdded().contains(nodeLocation) && file.getVcsStatus() != ADDED) {
                    file.setVcsStatus(ADDED);
                    tree.refresh(node);
                } else if (file.getVcsStatus() != NOT_MODIFIED) {
                    file.setVcsStatus(VcsStatus.NOT_MODIFIED);
                    tree.refresh(node);
                }
            });

        editorAgentProvider.get()
                           .getOpenedEditors()
                           .forEach(editor -> {
                               EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
                               String nodeLocation = tab.getFile().getLocation().removeFirstSegments(1).toString();
                               if (status.getUntracked().contains(nodeLocation)) {
                                   tab.setTitleColor(UNTRACKED.getColor());
                               } else if (status.getModified().contains(nodeLocation) || status.getChanged().contains(nodeLocation)) {
                                   tab.setTitleColor(MODIFIED.getColor());
                               } else if (status.getAdded().contains(nodeLocation)) {
                                   tab.setTitleColor(ADDED.getColor());
                               } else if (((File)tab.getFile()).getVcsStatus() != NOT_MODIFIED) {
                                   tab.setTitleColor(NOT_MODIFIED.getColor());
                               }
                           });

        appContext.getWorkspaceRoot().synchronize();
    }
}
