package edu.umich.si.inteco.minuku.contextmanager;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.record.Record;

/**
 * Created by Armuro on 10/2/15.
 */
public abstract class ContextSourceManager {

    protected static ArrayList<Record> mLocalRecordPool;
    protected static ArrayList<Condition> mConditions;

    //size of record pool. If the number of records exceed the size, we remove outdated
    //record pool or clear the record pool if we save it in the public record pool
    private int mSizeOfRecordPool = 300;

    public abstract void examineConditions();
    public abstract void stateChanged();
    public abstract void saveRecordsInLocalRecordPool();


    public ContextSourceManager() {
        mLocalRecordPool = new ArrayList<Record>();

    }

    public ContextSourceManager(ArrayList<Record> mlocalRecordPool) {
        this.mLocalRecordPool = mlocalRecordPool;
    }


    public static void addRecord(Record record){
        mLocalRecordPool.add(record);
    }

    public static Record getLastSavedRecord() {
        if (!mLocalRecordPool.isEmpty()){
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        }
        return null;
    }

    public static void removeRecord(Record record){
        mLocalRecordPool.remove(record);
    }

    public static void clearRecordPool(){
        mLocalRecordPool.clear();
    }


    public static ArrayList<Condition> getConditions() {
        return mConditions;
    }

    public static void setConditions(ArrayList<Condition> conditions) {
        mConditions = conditions;
    }

    public static ArrayList<Record> getLocalRecordPool() {
        return mLocalRecordPool;
    }

    public static void setLocalRecordPool(ArrayList<Record> localRecordPool) {
        mLocalRecordPool = localRecordPool;
    }
}
