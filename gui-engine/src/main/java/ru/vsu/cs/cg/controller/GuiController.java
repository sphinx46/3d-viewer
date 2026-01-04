package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import java.net.URL;

public class GuiController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private MenuItem menuThemeDark;

    @FXML
    private MenuItem menuThemeLight;

    private static final String DARK_THEME = "/static/css/theme-dark.css";
    private static final String LIGHT_THEME = "/static/css/theme-light.css";

    @FXML
    private void initialize() {
        setupThemeHandlers();
    }

    private void setupThemeHandlers() {
        menuThemeDark.setOnAction(event -> applyTheme(DARK_THEME));
        menuThemeLight.setOnAction(event -> applyTheme(LIGHT_THEME));
    }

    private void applyTheme(String themePath) {
        URL themeUrl = getClass().getResource(themePath);
        if (themeUrl != null) {
            anchorPane.getStylesheets().clear();
            anchorPane.getStylesheets().add(themeUrl.toExternalForm());
        }
    }
}
