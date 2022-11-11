package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Message mess = update.message();
            Long chatId = update.message().chat().id();

            if (mess.text().equals("/start")) {
                SendMessage sendMess = new SendMessage(chatId,
                        "¡Hola Amigo! \n Введи дату и время в формате: \n 01.01.2022 20:00 \n Введи задачу");
                SendResponse response = telegramBot.execute(sendMess);
            }
            Matcher matcher = pattern.matcher(mess.text());
            if (matcher.matches()) {
                String date = matcher.group(1);
                String item = matcher.group(3);
                LocalDateTime ldt = LocalDateTime.parse(date, dateTimeFormatter);

                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChartId(chatId);
                notificationTask.setMessage(item);
                notificationTask.setDateTime(ldt.truncatedTo(ChronoUnit.MINUTES));

                notificationTaskRepository.save(notificationTask);
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
