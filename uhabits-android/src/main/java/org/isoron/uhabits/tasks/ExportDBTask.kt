/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.tasks

import android.content.Context
import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.inject.AppContext
import org.isoron.uhabits.utils.DatabaseUtils.saveDatabaseCopy
import java.io.IOException

class ExportDBTask(
    @param:AppContext private val context: Context,
    private val system: AndroidDirFinder,
    private val listener: Listener,
): Task {
    private lateinit var result: Result

    override fun doInBackground() {
        result = try {
            system.getFilesDir("Backups")?.let { dir ->
                val filename = saveDatabaseCopy(context, dir)
                Result.Success(filename)
            } ?: Result.CannotFindSaveDirError
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun onPostExecute() {
        when (val actual = result) {
            Result.CannotFindSaveDirError -> listener.onExportDBError()
            is Result.Success -> listener.onExportDBFinished(actual.filename)
        }
    }

    interface Listener {
        fun onExportDBFinished(filename: String)
        fun onExportDBError()
    }

    private sealed interface Result {
        data class Success(val filename: String): Result
        data object CannotFindSaveDirError: Result
    }
}
