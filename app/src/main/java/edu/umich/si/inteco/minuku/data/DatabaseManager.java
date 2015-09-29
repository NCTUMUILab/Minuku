package edu.umich.si.inteco.minuku.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Armuro on 6/20/14.
 */
public class DatabaseManager {

    private AtomicInteger mOpenDBCounter = new AtomicInteger();

    /** Tag for logging. */
    private static final String LOG_TAG = "DatabaseManager";

    private static DatabaseManager instance;
    private static LocalDBHelper mLocalDBHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(LocalDBHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mLocalDBHelper = helper;
//            Log.d(LOG_TAG, "[test instantiate db]  after instanstiate the database");
        }

    }

    public static synchronized void initializeInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
//            Log.d(LOG_TAG, "[test instantiate db]  after instanstiate the database");
        }

    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
   //     Log.d("test opendatabase", "test opendatabase mDAtabase is" + mOpenDBCounter.incrementAndGet()) ;

        if(mOpenDBCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mLocalDBHelper.getWritableDatabase();
            Log.d("test opendatabase", "test opendatabase mDAtabase is" + mDatabase);

        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
  //      Log.d("test opendatabase", "test opendatabase mDAtabase is" + mOpenDBCounter.decrementAndGet()) ;
        if(mOpenDBCounter.decrementAndGet() == 0) {
            // Closing database
            if (mDatabase!=null)
                mDatabase.close();

        }
    }


}
