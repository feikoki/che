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
package org.eclipse.che.plugin.gdb.server.parser;

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'clear' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbDelete {

    private static final Pattern GDB_DELETE = Pattern.compile(".*Delete all breakpoints.*answered Y; input not from terminal.*");

    private GdbDelete() {
    }

    /**
     * Factory method.
     */
    public static GdbDelete parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        Matcher matcher = GDB_DELETE.matcher(output);
        if (matcher.find()) {
            return new GdbDelete();
        }

        throw new GdbParseException(GdbDelete.class, output);
    }
}
