package com.zkxltech.marksdk.task;


import android.os.AsyncTask;
import android.os.Build;

import com.zkxltech.marksdk.task.asynctask.IDoInBackground;
import com.zkxltech.marksdk.task.asynctask.IIsViewActive;
import com.zkxltech.marksdk.task.asynctask.IPostExecute;
import com.zkxltech.marksdk.task.asynctask.IPreExecute;
import com.zkxltech.marksdk.task.asynctask.IProgressUpdate;
import com.zkxltech.marksdk.task.asynctask.IPublishProgress;


/**
 * 功能描述：Android AsyncTask异步任务封装类
 *
 */
public class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements IPublishProgress<Progress> {
    private IPreExecute mPreExecute;
    private IProgressUpdate<Progress> mProgressUpdate;
    private IDoInBackground<Params, Progress, Result> mDoInBackground;
    private IIsViewActive mViewActive;
    private IPostExecute<Result> mPostExecute;

    private MyAsyncTask() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPreExecute != null) mPreExecute.onPreExecute();
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        if (mProgressUpdate != null) mProgressUpdate.onProgressUpdate(values);
    }

    @Override
    public Result doInBackground(Params... params) {
        return mDoInBackground == null ? null : mDoInBackground.doInBackground(this, params);
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (mPostExecute != null && (mViewActive == null || mViewActive.isViewActive())) mPostExecute.onPostExecute(result);
    }

    @SafeVarargs
    public final AsyncTask<Params, Progress, Result> start(Params... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        } else {
            return super.execute(params);
        }
    }


    @Override
    public void showProgress(Progress[] values) {
        this.publishProgress(values);
    }

    public static <Params, Progress, Result> Builder<Params, Progress, Result> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<Params, Progress, Result> {

        private final MyAsyncTask<Params, Progress, Result> mAsyncTask;

        public Builder() {
            mAsyncTask = new MyAsyncTask<Params, Progress, Result>();
        }

        public Builder<Params, Progress, Result> setPreExecute(IPreExecute preExecute) {
            mAsyncTask.mPreExecute = preExecute;
            return this;
        }

        public Builder<Params, Progress, Result> setProgressUpdate(IProgressUpdate<Progress> progressUpdate) {
            mAsyncTask.mProgressUpdate = progressUpdate;
            return this;
        }

        public Builder<Params, Progress, Result> setDoInBackground(IDoInBackground<Params, Progress, Result> doInBackground) {
            mAsyncTask.mDoInBackground = doInBackground;
            return this;
        }

        public Builder<Params, Progress, Result> setViewActive(IIsViewActive viewActive) {
            mAsyncTask.mViewActive = viewActive;
            return this;
        }

        public Builder<Params, Progress, Result> setPostExecute(IPostExecute<Result> postExecute) {
            mAsyncTask.mPostExecute = postExecute;
            return this;
        }

        @SafeVarargs
        public final AsyncTask<Params, Progress, Result> start(Params... params) {
            return mAsyncTask.start(params);
        }
    }
}
    /**
     * 全功能调用方式
     *
     *
    private void loadData() {
        MyAsyncTask.<String, Integer, Boolean>newBuilder()
                .setPreExecute(new IPreExecute() {
                    @Override
                    public void onPreExecute() {
                        mainTextView.setText("开始下载数据……");
                    }
                })
                .setDoInBackground(new IDoInBackground<String, Integer, Boolean>() {
                    @Override
                    public Boolean doInBackground(IPublishProgress<Integer> publishProgress, String... strings) {
                        try {
                            for (int i = 1; i < 11; i++) {
                                Thread.sleep(1000);
                                publishProgress.showProgress(i);
                            }
                        } catch (Exception e) {
                            return false;
                        }
                        return true;
                    }
                })
                .setProgressUpdate(new IProgressUpdate<Integer>() {
                    @Override
                    public void onProgressUpdate(Integer... values) {
                        mainTextView.setText("正在下载数据，当前进度为：" + (values[0] * 100 / 10) + "%");
                    }
                })
                .setViewActive(new IIsViewActive() {
                    @Override
                    public boolean isViewActive() {
                        return MainActivity.this.isViewActive();
                    }
                })
                .setPostExecute(new IPostExecute<Boolean>() {
                    @Override
                    public void onPostExecute(Boolean aBoolean) {
                        if (aBoolean) {
                            mainTextView.setText("下载成功");
                        } else {
                            mainTextView.setText("下载失败");
                        }
                    }
                })
                .start("参数");
    }

     /**
     * 最简短的调用方式
     *
     *
    private void saveData() {
        MyAsyncTask.<Void, Void, Void>newBuilder()
                .setDoInBackground(new IDoInBackground<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(IPublishProgress<Void> publishProgress, Void... voids) {
                        return null;
                    }
                })
                .start();
    }
*/