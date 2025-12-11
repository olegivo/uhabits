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
package org.isoron.uhabits

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.isoron.uhabits.inject.AppContext
import org.isoron.uhabits.utils.FileUtils
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AndroidDirFinder @Inject constructor(@param:AppContext private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getFilesDir(relativePath: String): File? {
        return runBlocking(scope.coroutineContext) {
            var result: File? = null
            var i = 5
            while (result == null && i > 0) {
                val potentialParentDirs = ContextCompat.getExternalFilesDirs(context, null)
                    .filter { it != null && isStorageWritable(it) }
                if (potentialParentDirs.isNotEmpty()) {
                    result = FileUtils.getDir(
                        potentialParentDirs = potentialParentDirs,
                        relativePath = relativePath
                    )
                }

                if (result == null) {
                    i--
                    delay(1.seconds)
                }
            }

            result
        }
    }

    private fun isStorageWritable(storageDir: File?): Boolean {
        return storageDir != null && storageDir.exists() &&
            Environment.getExternalStorageState(storageDir) == Environment.MEDIA_MOUNTED
    }
}
