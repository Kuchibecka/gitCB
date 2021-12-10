package src.application.Entity;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Задача клонирования репозитория
 */
public class CloneTask extends Task<Integer> {
    private final String directory;
    private final String token;
    private final String repoUrl;
    private final Logger rootLogger;

    public CloneTask(String directory, String token, String repoUrl, Logger rootLogger) {
        this.directory = directory;
        this.token = token;
        this.repoUrl = repoUrl;
        this.rootLogger = rootLogger;
    }

    /**
     * @return String Статус завершения задачи клонирования репозитория
     */
    @Override
    protected Integer call() {
        rootLogger.info("Calling git clone task");
        try {
            String pbCommand = "cd " + directory + " && "
                    + "git clone --progress https://gitlab-ci-token:"
                    + token
                    + "@"
                    + repoUrl;
            rootLogger.debug("Command for ProcessBuilder is: " + pbCommand);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", pbCommand);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int totalProgress = 0;
            int progress = 0;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    this.updateProgress(100, 100);
                    break;
                }
                if (line.contains(":") && line.contains("%") && !line.contains("done")) {
                    int curProgress = Integer.parseInt(line.substring(line.lastIndexOf(':') + 1, line.lastIndexOf('%')).replaceAll("\\s+", ""));
                    if (curProgress >= progress) {
                        totalProgress = totalProgress - progress + curProgress;
                        this.updateProgress(totalProgress, 400);
                        progress = curProgress;
                        if (progress == 100)
                            progress = 0;
                    }
                }
                //todo: Это может быть не последний этап клонирования
                if (line.contains("Receiving objects: 100%"))
                    this.updateProgress(400, 400);
                rootLogger.debug("Trace from ProcessBuilder: " + line);
                if (line.contains("already exists and is not an empty directory.")) {
                    this.updateProgress(0, 400);
                    rootLogger.debug("Directory already exists. Trying to update repository");
                    return 0;
                }
            }
        } catch (IOException e) {
            rootLogger.error("CloneTask ended with error: " + e.getMessage());
            return -1;
        }
        rootLogger.debug("Repository cloned successfully");
        return 1;
    }
}
