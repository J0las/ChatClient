/*
 *  Copyright (C) 2020  Felix Johannsmann, Johan Bücker
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 3, as published by
 *  the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package chatclient.lib;

import chatclient.log.Log;
import chatclient.log.LogType;

@SuppressWarnings("serial")

public class ComponendAlreadyRunningError extends Error {
    public ComponendAlreadyRunningError(Object obj) {
       Log.log(new String[] {
               obj.getClass().getName()
       }, LogType.MULTIPLE_OBJECT_INSTANCE);
       System.out.println(1);
    }
}
