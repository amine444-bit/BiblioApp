module com.bibliotheque {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.bibliotheque to javafx.fxml;
    opens com.bibliotheque.controller to javafx.fxml;
    opens com.bibliotheque.model to javafx.base;

    exports com.bibliotheque;
    exports com.bibliotheque.controller;
    exports com.bibliotheque.model;
    exports com.bibliotheque.dao;
    exports com.bibliotheque.util;
}
