/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating additional parts for list's header.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHeaderAdditionalParts implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'components/widget/list/list-header/additional-parts/che-list-header-additional-parts.html';
  transclude: boolean = true;
}

