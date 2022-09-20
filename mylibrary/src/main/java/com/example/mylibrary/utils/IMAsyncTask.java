package com.example.mylibrary.utils;

public abstract class IMAsyncTask {
    public void execute(final Object... params) {
        IMThreadPool.runInBackground(new Runnable() {
            @Override
            public void run() {
                final int code = doInBackground(params);
                IMThreadPool.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(code);
                    }
                });
            }
        });
    }

    protected abstract Integer doInBackground(Object... params);

    protected void onPostExecute(Integer code) {
    }
}
