package com.zkxltech.marksdk.task.asynctask;


public interface IDoInBackground<Params,Progress,Result> {
    Result doInBackground(IPublishProgress<Progress> publishProgress, Params... params);
}
