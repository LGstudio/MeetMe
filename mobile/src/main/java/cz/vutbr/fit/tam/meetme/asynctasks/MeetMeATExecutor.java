package cz.vutbr.fit.tam.meetme.asynctasks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lada on 26.11.2015.
 */
public class MeetMeATExecutor {
        static final int corePoolSize = 60;
        static final int maximumPoolSize = 60;
        static final int keepAliveTime = 5;

        public final BlockingQueue<Runnable> workQueue;
        public final Executor threadPoolExecutor;

        private static MeetMeATExecutor instance;

        private MeetMeATExecutor(){
              workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
              threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
        }

        public static MeetMeATExecutor getInstance(){
            if(instance==null){
                instance = new MeetMeATExecutor();
            }

            return instance;
        }


}
