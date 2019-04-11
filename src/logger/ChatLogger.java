package logger;

import main.Main;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger {
    private Main instance;
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("[ yyyy.MM.dd :: HH:mm:ss ] ");

    public ChatLogger(Main instance){
        this.instance = instance;
    }

    public void log(String message){
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        instance.getLogger().info(time.format(format) + message);
    }
}
