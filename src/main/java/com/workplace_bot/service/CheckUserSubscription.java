package com.workplace_bot.service;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckUserSubscription {
    private final AbsSender bot;
    private final List<String> resourcesForSubscribe;
    private final List<String> goodStatus = List.of("creator", "administrator", "member", "restricted");
    private final Map<Long, Boolean> subscribersCache = new HashMap<>();

    public CheckUserSubscription(AbsSender sender) {
        this.bot = sender;

        // TODO: add your channels and groups
        resourcesForSubscribe = List.of("@test_channel", "@test_group");
    }

    public boolean isSubscribed(long userId) {
        return subscribersCache.getOrDefault(userId, false);
    }

    public void sendSubscriptionMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        // TODO: add your own channels and groups
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Welcome to our bot " + update.getMessage().getFrom().getFirstName() +
                        "\nJoin:" +
                        "\n[Telegram Channel](https://t.me/test_channel)" +
                        "\n[Telegram Group](https://t.me/test_group)")
                .parseMode(ParseMode.MARKDOWN)
                .disableWebPagePreview(true)
                .build();
        sendMessage.setReplyMarkup(setCheckInlineKeyboard());
        replyToUser(sendMessage);
    }

    public void handleCallbackQuery(CallbackQuery query) {
        long userId = query.getFrom().getId();
        subscribersCache.put(userId, false);

        if (!isJoinedToResources(userId)) {
            answerCallbackQuery(query.getId(), "Please, join to all channels from list.", true);
        } else {
            deleteSubscriptionMessage(query);
            subscribersCache.put(userId, true);
        }
    }

    private boolean isJoinedToResources(long userId) {
        for (String channel: resourcesForSubscribe) {
            try {
                ChatMember member = bot.execute(new GetChatMember(channel, userId));
                if (!goodStatus.contains(member.getStatus())) return false;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void deleteSubscriptionMessage(CallbackQuery query) {
        long messageId = query.getMessage().getMessageId();
        long chatId = query.getMessage().getChatId();

        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId((int)messageId)
                .text("Complete.")
                .build();
        replyToUser(newMessage);

        String answer = EmojiParser.parseToUnicode("Thanks for joined! " + ":heart:");
        SendMessage sendGratitude = SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(startingButtonAfterVerification())
                .build();
        replyToUser(sendGratitude);
    }

    private ReplyKeyboardMarkup startingButtonAfterVerification() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        KeyboardButton btn1 = new KeyboardButton("Account");
        KeyboardButton btn2 = new KeyboardButton("Plan");
        KeyboardButton btn3 = new KeyboardButton("Task");
        KeyboardButton btn4 = new KeyboardButton("Promote");

        row1.add(btn1);
        row1.add(btn2);
        row2.add(btn3);
        row2.add(btn4);

        keyboardRows.add(row1);
        keyboardRows.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup setCheckInlineKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        String text = EmojiParser.parseToUnicode("Done " + ":white_check_mark:");
        InlineKeyboardButton doneButton = new InlineKeyboardButton(text);
        doneButton.setCallbackData("check_subscription");

        markup.setKeyboard(List.of(List.of(doneButton)));
        return markup;
    }

    private void replyToUser(BotApiMethod<?> msg) {
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void answerCallbackQuery(String callbackQueryId, String message, boolean alert) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        answer.setText(message);
        answer.setShowAlert(alert);
        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
