/*
 * This file is part of "SnipSnap Wiki/Weblog". Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel All Rights Reserved. Please visit http://snipsnap.org/ for
 * updates and contact. --LICENSE NOTICE-- This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */

package org.olat.presentation.wiki.versioning;

import java.sql.Timestamp;

/**
 * Information about a Snip version
 * 
 * @author Stephan J. Schmidt
 * @version $Id: VersionInfo.java,v 1.4 2011-02-11 10:32:44 patrickb Exp $
 */

public class VersionInfo {
    private int version;
    private String mUser;
    private Timestamp mTime;
    private long size;
    private int viewCount;

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getMUser() {
        return mUser;
    }

    public void setMUser(final String mUser) {
        this.mUser = mUser;
    }

    public Timestamp getMTime() {
        return mTime;
    }

    public void setMTime(final Timestamp mTime) {
        this.mTime = mTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(final int viewCount) {
        this.viewCount = viewCount;
    }
}
