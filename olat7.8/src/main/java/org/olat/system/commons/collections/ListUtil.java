/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.system.commons.collections;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 * Initial Date: 16.08.2012 <br>
 * 
 * @author aabouc
 */
public class ListUtil {

    @SuppressWarnings("unchecked")
    public static <T> List<List<T>> split(List<T> list, int size) {
        if (list == null || list.isEmpty() || !(size > 0)) {
            return Collections.EMPTY_LIST;
        }

        return new Split<T>(list, size);
    }

    private static class Split<T> extends AbstractList<List<T>> {
        final List<T> list;
        final int size;

        private Split(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            int listSize = size();
            if (listSize < 0) {
                throw new IllegalArgumentException("negative size: " + listSize);
            }
            if (index < 0) {
                throw new IndexOutOfBoundsException("index " + index + " must not be negative");
            }
            if (index >= listSize) {
                throw new IndexOutOfBoundsException("index " + index + " must be less than size " + listSize);
            }
            int start = index * size;
            int end = Math.min(start + size, list.size());
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return (list.size() + size - 1) / size;
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

    }

    public static boolean isNotBlank(List<?> list) {
        return (list != null && !list.isEmpty());
    }

    public static boolean isBlank(List<?> list) {
        return (list == null || list.isEmpty());
    }

}
