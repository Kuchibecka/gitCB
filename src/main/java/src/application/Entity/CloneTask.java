package src.application.Entity;

import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CloneTask extends Task<String> {

    private String cloneCommand;

    private Logger rootLogger;

    public CloneTask(String cloneCommand, Logger rootLogger) {
        this.cloneCommand = cloneCommand;
        this.rootLogger = rootLogger;
    }

    public String getCloneCommand() {
        return cloneCommand;
    }

    public void setCloneCommand(String cloneCommand) {
        this.cloneCommand = cloneCommand;
    }

    @Override
    protected String call() {
        rootLogger.info("Calling git clone task");
        try {
            rootLogger.debug("Full command for ProcessBuilder is: " + cloneCommand);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cloneCommand);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int totalProgress = 0;
            int progress = 0;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                // Вывод командной строки
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
                if (line.contains("Receiving objects: 100%"))
                    this.updateProgress(400, 400);
                rootLogger.debug("Trace from ProcessBuilder: " + line);
                if (line.contains("already exists and is not an empty directory.")) {
                    rootLogger.error("Directory already exists");
                    this.updateProgress(0, 400);
                    return "Непустая папка с таким названием уже существует";
                }
            }
        } catch (IOException e) {
            rootLogger.error(e.getMessage());
            return "Ошибка выполнения команды git clone";
        }
        return "none";
    }
}
