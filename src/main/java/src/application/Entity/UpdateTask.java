package src.application.Entity;

import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Задача обновления репозитория до актуального состояния
 */
public class UpdateTask extends Task<String> {
    private Logger rootLogger;
    private final String updateCommand;

    public UpdateTask(Logger rootLogger, String updateCommand) {
        this.rootLogger = rootLogger;
        this.updateCommand = updateCommand;
    }

    /**
     * @return String Статус завершения задачи обновления репозитория
     */
    @Override
    protected String call() {
        rootLogger.info("Calling git repository update task");
        try {
            ProcessBuilder pullBuilder = new ProcessBuilder("cmd.exe", "/c", this.updateCommand);
            pullBuilder.redirectErrorStream(true);
            Process pullProcess = pullBuilder.start();
            BufferedReader pullReader = new BufferedReader(new InputStreamReader(pullProcess.getInputStream()));
            String line;
            while (true) {
                line = pullReader.readLine();
                rootLogger.debug("Trace from ProcessBuilder: " + line);
                if (line.contains("Updating")) {
                    rootLogger.debug("Updating repository due to detected changes");
                    break;
                }
                if (line.contains("Already up to date")) {
                    rootLogger.debug("No changes detected");
                    return "No changes";
                }
                if (line.contains("fatal: not a git repository")) {
                    rootLogger.error("Not empty non-git repository");
                    return "Выбранная папка не пуста и\nне является репозиторием git";
                }
            }
            return "Репозиторий обновлён\nдо актуальной версии";
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
            return "Ошибка выполнения\nкоманды git pull";
        }
    }
}
