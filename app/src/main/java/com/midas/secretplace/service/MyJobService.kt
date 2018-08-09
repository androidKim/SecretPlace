package com.midas.secretplace.service

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService


class MyJobService : JobService()
{
    override fun onStartJob(job: JobParameters?): Boolean
    {
        println("job started")
        return false
    }

    override fun onStopJob(job: JobParameters?): Boolean
    {
        println("job stop")
        return false
    }

}