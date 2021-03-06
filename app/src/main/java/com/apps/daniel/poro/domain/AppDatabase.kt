package com.apps.daniel.poro.domain

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import com.apps.daniel.poro.MainApplication
import com.apps.daniel.poro.domain.dao.LabelDao
import com.apps.daniel.poro.domain.dao.ProfileDao
import com.apps.daniel.poro.domain.dao.SessionDao
import com.apps.daniel.poro.domain.models.Label
import com.apps.daniel.poro.domain.models.Profile
import com.apps.daniel.poro.domain.models.Session
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Database(
    entities = [Session::class, Label::class, Profile::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionModel(): SessionDao
    abstract fun labelDao(): LabelDao
    abstract fun profileDao(): ProfileDao

    companion object {
        private const val TAG = "PoroDatabase"
        private val dbToInstanceId = ConcurrentHashMap<Int, String>()
        private val threadToInstanceId = ConcurrentHashMap<Long, String>()

        const val DATABASE_NAME = "poro-db"
        private val LOCK = Any()
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // do nothing here; it seems to be needed by the switch to kapt room compiler
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null || !INSTANCE!!.isOpen) {
                synchronized(LOCK) {
                    if (INSTANCE == null || !INSTANCE!!.isOpen) {
                        INSTANCE = recreateInstance(context)
                    }
                }
            }
            return INSTANCE!!
        }

        fun closeInstance() {
            if (INSTANCE!!.isOpen) {
                INSTANCE!!.openHelper.close()
            }
        }

        fun recreateInstance(context: Context): AppDatabase {
            // keep track of which thread belongs to which local database
            val instanceId = UUID.randomUUID().toString()

            // custom thread with an exception handler strategy
            val executor = Executors.newCachedThreadPool { runnable: Runnable? ->
                val defaultThreadFactory =
                    Executors.defaultThreadFactory()
                val thread = defaultThreadFactory.newThread(runnable)
                thread.uncaughtExceptionHandler = resetDatabaseOnUnhandledException
                threadToInstanceId[thread.id] = instanceId
                thread
            } as ThreadPoolExecutor

            val db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(executor)
                .build()
            dbToInstanceId[db.hashCode()] = instanceId
            return db
        }

        private var resetDatabaseOnUnhandledException =
            Thread.UncaughtExceptionHandler { thread, throwable ->
                val message = "uncaught exception in a LocalDatabase thread, resetting the database"
                Log.e(TAG, message, throwable)
                synchronized(LOCK) {
                    // there is no active local database to clean up
                    if (INSTANCE == null) return@UncaughtExceptionHandler
                    val instanceIdOfThread: String? = threadToInstanceId[thread.id]
                    val instanceIdOfActiveLocalDb: String? = dbToInstanceId[INSTANCE.hashCode()]
                    if (instanceIdOfThread == null || instanceIdOfThread != instanceIdOfActiveLocalDb) {
                        // the active local database instance is not the one
                        // that caused this thread to fail, so leave it as is
                        return@UncaughtExceptionHandler
                    }
                    INSTANCE!!.tryResetDatabase()
                }
            }
    }

    private fun tryResetDatabase() {
        try {
            // try closing existing connections
            try {
                if (this.openHelper.writableDatabase.isOpen) {
                    this.openHelper.writableDatabase.close()
                }
                if (this.openHelper.readableDatabase.isOpen) {
                    this.openHelper.readableDatabase.close()
                }
                if (this.isOpen) {
                    this.close()
                }
                if (this == INSTANCE) INSTANCE = null
            } catch (ex: Exception) {
                Log.e(TAG, "Could not close LocalDatabase", ex)
            }

            // try deleting database file
            val f: File = MainApplication.context.getDatabasePath(DATABASE_NAME)
            if (f.exists()) {
                val deleteSucceeded = SQLiteDatabase.deleteDatabase(f)
                if (!deleteSucceeded) {
                    Log.e(TAG, "Could not delete LocalDatabase")
                }
            }

            val tmp: AppDatabase = recreateInstance(MainApplication.context)
            tmp.query("SELECT * from Session", null)
            tmp.close()

            this.openHelper.readableDatabase
            this.openHelper.writableDatabase
            this.query("SELECT * from Session", null)
        } catch (ex: Exception) {
            Log.e(TAG, "Could not reset LocalDatabase", ex)
        }
    }
}