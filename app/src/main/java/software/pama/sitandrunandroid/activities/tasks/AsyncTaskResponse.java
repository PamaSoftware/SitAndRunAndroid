package software.pama.sitandrunandroid.activities.tasks;

public interface AsyncTaskResponse<T> {
    void onTaskFinish(T result);
}

