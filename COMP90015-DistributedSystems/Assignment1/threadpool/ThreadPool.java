//Zhuoyang Hao 1255309
package threadpool;
import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private final List<WorkerThread> threads;
    private final LinkedList<Task> taskQueue;
    private boolean isShutdown = false;

    public ThreadPool(int numberOfThreads) {
        taskQueue = new LinkedList<>();
        threads = new LinkedList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            WorkerThread thread = new WorkerThread();
            thread.start();
            threads.add(thread);
        }
    }

    public synchronized void submit(Task task) {
        if (!isShutdown) {
            synchronized (taskQueue) {
                taskQueue.addLast(task);
                taskQueue.notify();
            }
        }
    }

    public synchronized void shutdown() {
        isShutdown = true;
        for (WorkerThread thread : threads) {
            thread.interrupt();
        }
    }

    private class WorkerThread extends Thread {
        public void run() {
            Task task;
            while (!isShutdown || !taskQueue.isEmpty()) {
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty()) {
                        if (isShutdown) return;
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            if (isShutdown) return;
                        }
                    }
                    task = taskQueue.removeFirst();
                }

                try {
                    task.run();
                } catch (RuntimeException e) {
                    System.err.println("Thread pool is interrupted due to an issue: " + e.getMessage());
                }
            }
        }
    }
}