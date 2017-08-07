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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Edition {
    int getBeginLine();

    void setBeginLine(int startLine);

    Edition withBeginLine(int startLine);

    int getEndLine();

    void setEndLine(int endLine);

    Edition withEndLine(int endLine);

//    int[] getLines();
//
//    void setLines(int[] lines);
//
//    Edition withLines(int[] lines);

    String getType();

    void setType(String type);

    Edition withType(String type);
}
