package src.application.Entity;

import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Задача обновления репозитория до актуального состояния
 */
public class UpdateTask extends Task<Integer> {
    private Logger rootLogger;
    private final String repoName;
    private final String directory;
    private final String token;
    private final String repoUrl;

    public UpdateTask(String repoName, String directory, String token, String repoUrl, Logger rootLogger) {
        this.repoName = repoName;
        this.directory = directory;
        this.token = token;
        this.repoUrl = repoUrl;
        this.rootLogger = rootLogger;
    }

    /**
     * Функция, выполняющаяся при запуске updateTask
     *
     * @return Integer Статус завершения задачи обновления репозитория:
     * 0    -   локальный репозиторий актуален
     * 1    -   локальный репозиторий обновлён до актуального
     * -1   -   ошибка: выбранная папка непуста и не является репозиторием git
     */
    @Override
    protected Integer call() {
        rootLogger.info("Calling git repository update task");
        try {
            String name = repoName.toLowerCase().replace(" ", "-");
            String pbCommand = "cd " + directory + "\\" + name + " && "
                    + "git pull https://gitlab-ci-token:"
                    + token
                    + "@"
                    + repoUrl;
            rootLogger.debug("Update command for ProcessBuilder: " + pbCommand);
            ProcessBuilder pullBuilder = new ProcessBuilder("cmd.exe", "/c", pbCommand);
            pullBuilder.redirectErrorStream(true);
            Process pullProcess = pullBuilder.start();
            BufferedReader pullReader = new BufferedReader(new InputStreamReader(pullProcess.getInputStream()));
            String line;
            while (true) {
                line = pullReader.readLine();
                rootLogger.debug("Trace from ProcessBuilder: " + line);
                if (line.contains("Already up to date")) {
                    this.updateProgress(100, 100);
                    rootLogger.debug("No changes detected");
                    return 0;
                }
                if (line.contains("detecting host provider")) {
                    this.updateProgress(25, 100);
                }
                if (line.contains("Updating")) {
                    this.updateProgress(50, 100);
                }
                if (line.contains("changed")) {
                    this.updateProgress(100, 100);
                    return 1;
                }
                if (line.contains("fatal: not a git repository")) {
                    rootLogger.error("Not empty non-git repository");
                    return -1;
                }
                if (line.contains("fatal: refusing to merge unrelated histories")) {
                    rootLogger.error("Not empty git repository. Unable to merge histories");
                    return -2;
                }
            }
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
            return -3;
        }
    }
}
