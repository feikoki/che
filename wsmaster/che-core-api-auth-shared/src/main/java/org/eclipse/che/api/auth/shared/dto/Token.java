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
package org.eclipse.che.api.auth.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Authentication token.
 *
 * @author gazarenkov
 */
@DTO
public interface Token {
    /** "Authentication token obtained after login" */
    String getValue();

    void setValue(String value);

    Token withValue(String value);
}
