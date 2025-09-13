package com.workplace_bot;

import com.workplace_bot.service.CheckUserSubscription;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    Map<Long, Long> chatIds = new HashMap<>();
// map values:
// 1 - in a promotion
// 2 - F.B. promotion
// 3 - step 3
    private final CheckUserSubscription userSubscriptionChecker;

    public Bot() {
        this.userSubscriptionChecker = new CheckUserSubscription(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("check_subscription")) {
                userSubscriptionChecker.handleCallbackQuery(update.getCallbackQuery());
                return;
            }
        }

        String cmd = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        SendMessage sm = new SendMessage();
        sm.setChatId(update.getMessage().getChatId());
        sm.setParseMode(ParseMode.MARKDOWN);
        sm.enableMarkdownV2(true);

        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setSelective(true);
        rkm.setResizeKeyboard(true);
        rkm.setOneTimeKeyboard(false);

        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();


        if (!chatIds.containsKey(chatId)) {
            chatIds.put(chatId, 0L);
        }

        // If user sends "/start" to restart the session or sends "Back" from any sub-pages of the start page (1 = Account, 2 = Plan)

        if (cmd.equals("/start")) {
            userSubscriptionChecker.sendSubscriptionMessage(update);
            return;
        }
        if (!userSubscriptionChecker.isSubscribed(update.getMessage().getChatId())) return;


        if ((cmd.equals("Back") && (chatIds.get(chatId) == 1 || chatIds.get(chatId) == 2 || chatIds.get(chatId) == 3 || chatIds.get(chatId) == 4))) {

            chatIds.replace(chatId, 0L);
            sm.setText("Welcome to our bot " + update.getMessage().getFrom().getFirstName() +
                    "\nJoin:" +
                    "\n[Telegram Channel](https://t.me/workplace_channel)" +
                    "\n[Telegram Group](https://t.me/workplace_group)");

            List<KeyboardRow> rl = new ArrayList<>();
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
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        if (cmd.equals("Account") || (cmd.equals("Back") && (chatIds.get(chatId) == 5 || chatIds.get(chatId) == 6 || chatIds.get(chatId) == 7 || chatIds.get(chatId) == 8))) {

            chatIds.replace(chatId, 1L);
            sm.setText("NAME           :  " + update.getMessage().getFrom().getFirstName() +
                    "\nUID                :  `" + update.getMessage().getChatId() +
                    "`\nPLAN            : N/A" +
                    "\nBALANCE    :  00" +
                    "\nREFER LINK  : `https://t.me/azmacbot?start=" + update.getMessage().getChatId() + "`");

            List<KeyboardRow> rl = new ArrayList<>();
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton btn1 = new KeyboardButton("Deposit");
            KeyboardButton btn2 = new KeyboardButton("Withdraw");
            KeyboardButton btn3 = new KeyboardButton("Upgrade");
            KeyboardButton btn4 = new KeyboardButton("Back");
            row1.add(btn1);
            row1.add(btn2);
            row2.add(btn3);
            row2.add(btn4);
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        if (cmd.equals("Plan")) {

            chatIds.replace(chatId, 2L);
            sm.setText("\uD83E\uDD49BRONZE :  02$\n\n\uD83E\uDD48SILVER    :  04$\n\n\uD83E\uDD47GOLD      :  06$");

            List<KeyboardRow> rl = new ArrayList<>(); // Keyboard Row list (rl = row list)
            KeyboardRow row1 = new KeyboardRow(); // Keyboard Row 1 (row1)
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton btn1 = new KeyboardButton("Deposit"); // Keyboard Button 1 (btn1)
            KeyboardButton btn2 = new KeyboardButton("Back");
            row1.add(btn1);
            row2.add(btn2);
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        if (cmd.equals("Deposit")) {
            sm.setText("Here is some deposit method\\.\n\nPaypal / Payeer / Payoneer:\n`tanjirxe@gmail.com`");

            List<List<InlineKeyboardButton>> rl = new ArrayList<>(); // InlineKeyboardButton Row List = (rl = row list)
            List<InlineKeyboardButton> row1 = new ArrayList<>();  // InlineKeyboardButton Row 1 (row1)
            InlineKeyboardButton btn1 = new InlineKeyboardButton("Check Deposit"); // InlineKeyboardButton 1 (btn1)
            btn1.setCallbackData("check_deposit");
            row1.add(btn1);
            rl.add(row1);
            ikm.setKeyboard(rl);
            sm.setReplyMarkup(ikm);
        }

        if (update.hasCallbackQuery()) {
            // Set variables
            CallbackQuery cq = update.getCallbackQuery();
            String dta = cq.getData();
            Message msg = cq.getMessage();

            if (dta != null && dta.equals("check_deposit")) {
                sm.setChatId(msg.getChatId());
                sm.setText("ABCD");
                try {
                    execute(sm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        if (cmd.equals("Withdraw")) {
            sm.setText("Send your Skrill email");
        }

        if (cmd.equals("Task") || (cmd.equals("Back") && (chatIds.get(chatId) == 9 || chatIds.get(chatId) == 10 || chatIds.get(chatId) == 11 || chatIds.get(chatId) == 12 ))) {

            chatIds.replace(chatId, 3L);
            sm.setText("Check  this button \uD83D\uDC47");

            List<KeyboardRow> rl = new ArrayList<>();
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton btn1 = new KeyboardButton("Social Media");
            KeyboardButton btn2 = new KeyboardButton("Website");
            KeyboardButton btn3 = new KeyboardButton("Ads");
            KeyboardButton btn4 = new KeyboardButton("Back");
            row1.add(btn1);
            row1.add(btn2);
            row2.add(btn3);
            row2.add(btn4);
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        if (cmd.equals("Website")) {
            sm.setText("Check  this link https://www\\.youtube\\.com/@workplace_bot");
        }

        if (cmd.equals("Promote")){
            chatIds.replace(chatId, 4L);
            sm.setText("Check  this button \uD83D\uDC47");

            List<KeyboardRow> rl = new ArrayList<>();
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton btn1 = new KeyboardButton("Social Media");
            KeyboardButton btn2 = new KeyboardButton("Website");
            KeyboardButton btn3 = new KeyboardButton("Ads");
            KeyboardButton btn4 = new KeyboardButton("Back");
            row1.add(btn1);
            row1.add(btn2);
            row2.add(btn3);
            row2.add(btn4);
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        if (cmd.equals("/help")){
            chatIds.replace(chatId, 4L);
            sm.setText("Check  this button \uD83D\uDC47");

            List<KeyboardRow> rl = new ArrayList<>();
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton btn1 = new KeyboardButton("Write your problem");
            KeyboardButton btn2 = new KeyboardButton("Back");
            row1.add(btn1);
            row2.add(btn2);
            rl.add(row1);
            rl.add(row2);
            rkm.setKeyboard(rl);
            sm.setReplyMarkup(rkm);
        }

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        // TODO: bot's username
        return "azmacbot";
    }

    @Override
    public String getBotToken() {
        // TODO: bot's token
        return "5707774747:AAEeDbZi0eOy-WNrYM3vRoUfma5CrNiztig";
    }
}

