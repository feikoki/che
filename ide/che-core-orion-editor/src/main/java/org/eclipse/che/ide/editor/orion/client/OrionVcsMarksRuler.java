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
package org.eclipse.che.ide.editor.orion.client;

import elemental.dom.Element;

import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutters;
import org.eclipse.che.ide.editor.orion.client.jso.ModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationIteratorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationModelOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionStyleOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Orion implementation of the ruler for adding breakpoints marks.
 *
 * @author Anatoliy Bazko
 */
public class OrionVcsMarksRuler implements Gutter {

    private static final String VCS_MARK = "vcs.makr";

    private final OrionExtRulerOverlay        orionExtRulerOverlay;
    private final OrionEditorOverlay          editorOverlay;
    private final OrionAnnotationModelOverlay annotationModel;

    private OrionTextModelOverlay.EventHandler<ModelChangedEventOverlay> modelChangingEventHandler;

    public OrionVcsMarksRuler(OrionExtRulerOverlay rulerOverlay, OrionEditorOverlay editorOverlay) {
        this.orionExtRulerOverlay = rulerOverlay;
        this.editorOverlay = editorOverlay;
        this.orionExtRulerOverlay.addAnnotationType(VCS_MARK, 1);
        this.annotationModel = orionExtRulerOverlay.getAnnotationModel();
    }

    /** {@inheritDoc} */
    @Override
    public void addGutterItem(int line, String gutterId, Element element) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return;
        }

        OrionAnnotationOverlay annotation = toAnnotation(element, line);
        annotationModel.addAnnotation(annotation);
    }

    /** {@inheritDoc} */
    @Override
    public void addGutterItem(int line, String gutterId, Element element, final LineNumberingChangeCallback lineCallback) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return;
        }

        addGutterItem(line, gutterId, element);
        if (modelChangingEventHandler == null) {
            modelChangingEventHandler = new OrionTextModelOverlay.EventHandler<ModelChangedEventOverlay>() {
                @Override
                public void onEvent(ModelChangedEventOverlay parameter) {
                    int linesAdded = parameter.addedLineCount();
                    int linesRemoved = parameter.removedLineCount();
                    int fromLine = editorOverlay.getModel().getLineAtOffset(parameter.start());
                    String line = editorOverlay.getModel().getLine(fromLine);

                    if (linesAdded > 0 || linesRemoved > 0 || line.trim().isEmpty()) {
                        removeAnnotations(getAnnotationsFrom(fromLine));
                        lineCallback.onLineNumberingChange(fromLine, linesRemoved, linesAdded);
                    }
                }
            };

            this.editorOverlay.getModel().addEventListener("Changed", modelChangingEventHandler, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeGutterItem(int line, String gutterId) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return;
        }

        OrionAnnotationOverlay[] annotations = getAnnotations(line);
        removeAnnotations(annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Element getGutterItem(int line, String gutterId) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return null;
        }

        OrionAnnotationOverlay[] annotations = getAnnotations(line);
        for (OrionAnnotationOverlay annotation : annotations) {
            if (isVcsMarkAnnotation(annotation)) {
                return Elements.createDivElement(annotation.getStyle().getStyleClass());
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void clearGutter(String gutterId) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return;
        }

        OrionAnnotationOverlay[] annotations = getAllAnnotations();
        removeAnnotations(annotations);
    }

    /** {@inheritDoc} */
    @Override
    public void setGutterItem(int line, String gutterId, Element element) {
        if (!Gutters.VCS_MARKS_GUTTER.equals(gutterId)) {
            return;
        }

        OrionAnnotationOverlay[] oldAnnotations = getAnnotations(line);
        removeAnnotations(oldAnnotations);

        addGutterItem(line, gutterId, element);
    }

    private void removeAnnotations(OrionAnnotationOverlay[] annotations) {
        for (OrionAnnotationOverlay annotation : annotations) {
            if (isVcsMarkAnnotation(annotation)) {
                annotationModel.removeAnnotation(annotation);
            }
        }
    }

    private OrionAnnotationOverlay toAnnotation(Element element, int line) {
        int lineStart = editorOverlay.getModel().getLineStart(line);
        int lineEnd = editorOverlay.getModel().getLineEnd(line);

        OrionAnnotationOverlay annotation = OrionAnnotationOverlay.create();

        OrionStyleOverlay styleOverlay = OrionStyleOverlay.create();
        styleOverlay.setStyleClass(element.getClassName());

        annotation.setStyle(styleOverlay);
        annotation.setType(VCS_MARK);
        annotation.setStart(lineStart);
        annotation.setEnd(lineEnd);

        return annotation;
    }

    private OrionAnnotationOverlay[] getAnnotations(int line) {
        int lineStart = editorOverlay.getModel().getLineStart(line);
        int lineEnd = editorOverlay.getModel().getLineEnd(line);
        return doGetAnnotations(lineStart, lineEnd);
    }

    private OrionAnnotationOverlay[] getAnnotationsFrom(int fromLine) {
        int lineStart = editorOverlay.getModel().getLineStart(fromLine);
        return doGetAnnotations(lineStart, Integer.MAX_VALUE);
    }

    private OrionAnnotationOverlay[] getAllAnnotations() {
        return doGetAnnotations(0, Integer.MAX_VALUE);
    }

    private OrionAnnotationOverlay[] doGetAnnotations(int lineStart, int lineEnd) {
        return orionExtRulerOverlay.getAnnotationsByType(annotationModel, lineStart, lineEnd);
    }

    private boolean isVcsMarkAnnotation(OrionAnnotationOverlay annotation) {
        return VCS_MARK.equals(annotation.getType());
    }
}