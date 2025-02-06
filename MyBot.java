package com.company;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyBot extends TelegramLongPollingBot {
    private DBConnection dbConnection = new DBConnection();
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatID = message.getChatId();

            // Agar xabar video bo'lsa
            if (message.hasVideo()) {
                String fileId = message.getVideo().getFileId();
                String videoName = message.getCaption() != null ? message.getCaption() : "Noma'lum video";

                createVideo(fileId, videoName);
                sendMessage(chatID, "✅ Video muvaffaqiyatli saqlandi!");
                return;
            }

            // Agar xabar matn bo'lsa
            if (message.hasText()) {
                String text = message.getText();

                // Kino ID orqali qidirish
                try {
                    int videoId = Integer.parseInt(text); // Xabar ID bo‘lsa, uni raqamga o‘tkazamiz
                    String videoFileId = getVideoByFileId(videoId);

                    if (videoFileId != null) {
                        sendVideo(chatID, videoFileId);
                    } else {
                        sendMessage(chatID, "❌ Bu ID dagi kino mavjud emas!");
                    }
                    return;
                } catch (NumberFormatException e) {
                    // Agar raqam emas bo'lsa, nom sifatida qidiramiz
                }

                // Kino nomi orqali qidirish
                String videoFileId = getVideoByName(text);
                if (videoFileId != null) {
                    sendVideo(chatID, videoFileId);
                } else {
                    sendMessage(chatID, "❌ Bunday nomdagi kino mavjud emas!");
                }
            }
        }
    }




    private void createVideo(String fileId, String videoName) {
        String sql = "INSERT INTO movies (file_id, movie_name) VALUES (?, ?)";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fileId);
            stmt.setString(2, videoName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getVideoByFileId(int id) {
        String sql = "SELECT file_id FROM movies WHERE id = ?";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("file_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getVideoByName(String name){
        String sql = "SELECT file_id FROM movies WHERE movie_name = ?";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("movie_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(Long chatID, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendVideo(Long chatID, String fileId) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatID);
        sendVideo.setVideo(new InputFile(fileId));
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBotUsername() {
        return "https://t.me/movie_ni_bot";
    }
    @Override
    public String getBotToken() {
        return "7635599781:AAFwgEHj8ZVUPafOffZK6kn-wVvUZ-L7dBs";
    }
}
