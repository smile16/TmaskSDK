package com.zkxltech.marksdk.task.asynctask;


public interface IProgressUpdate<Progress>{
    void onProgressUpdate(Progress... values);
}