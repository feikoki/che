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
package org.eclipse.che.plugin.pullrequest.client.parts.contribute;

/**
 * Used to detect if pull request title/comment/branch is changed.
 *
 * @author Yevhenii Voevodin
 * @see ContributePartView#addBranchChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionCommentChangedHandler(TextChangedHandler)
 * @see ContributePartView#addContributionTitleChangedHandler(TextChangedHandler)
 */
public interface TextChangedHandler {

    /**
     * Called when title/comment/branch is changed
     *
     * @param newText
     *         new text content
     */
    void onTextChanged(String newText);
}
