package com.bibliotheque.controller;

import com.bibliotheque.MainApp;
import com.bibliotheque.dao.BookDAO;
import com.bibliotheque.dao.LoanDAO;
import com.bibliotheque.model.Book;
import com.bibliotheque.model.Loan;
import com.bibliotheque.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class MemberDashboardController {

    @FXML private Label lblWelcome;
    @FXML private TextField txtSearch;

    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book,String> colTitle, colAuthor, colGenre;
    @FXML private TableColumn<Book,Integer> colYear, colAvail;
    @FXML private TableColumn<Book,Void> colAction;

    @FXML private TableView<Loan> tableMyLoans;
    @FXML private TableColumn<Loan,String> colLoanBook, colLoanAuthor, colLoanDate, colLoanDue, colLoanReturn, colLoanStatus;

    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    @FXML
    public void initialize() {
        lblWelcome.setText("👤 " + SessionManager.getCurrentUser().getFullName());
        setupBooksTable();
        setupLoansTable();
        loadBooks();
        loadMyLoans();
    }

    private void setupBooksTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colAvail.setCellValueFactory(new PropertyValueFactory<>("available"));
        colAction.setCellFactory(borrowButtonFactory());
    }

    private void setupLoansTable() {
        colLoanBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colLoanAuthor.setCellValueFactory(new PropertyValueFactory<>("bookAuthor"));
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colLoanDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colLoanReturn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        colLoanStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadBooks() {
        tableBooks.setItems(FXCollections.observableArrayList(bookDAO.getAllBooks()));
    }

    private void loadMyLoans() {
        int uid = SessionManager.getCurrentUser().getId();
        tableMyLoans.setItems(FXCollections.observableArrayList(loanDAO.getLoansByUser(uid)));
    }

    @FXML private void handleSearch() {
        String kw = txtSearch.getText().trim();
        tableBooks.setItems(FXCollections.observableArrayList(
            kw.isEmpty() ? bookDAO.getAllBooks() : bookDAO.searchBooks(kw)
        ));
    }

    @FXML private void handleRefresh() {
        txtSearch.clear();
        loadBooks();
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
        MainApp.loadScene("login");
    }

    private Callback<TableColumn<Book,Void>, TableCell<Book,Void>> borrowButtonFactory() {
        return col -> new TableCell<>() {
            final Button btn = new Button("📥 Emprunter");
            {
                btn.setStyle("-fx-background-color:#3b82f6;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;-fx-font-size:12;");
                btn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    int uid = SessionManager.getCurrentUser().getId();

                    if (!book.isAvailable()) {
                        showAlert("Indisponible", "Ce livre n'est pas disponible actuellement.", Alert.AlertType.WARNING);
                        return;
                    }
                    if (loanDAO.hasActiveLoan(uid, book.getId())) {
                        showAlert("Déjà emprunté", "Vous avez déjà emprunté ce livre.", Alert.AlertType.WARNING);
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmer l'emprunt");
                    confirm.setHeaderText("Emprunter: " + book.getTitle());
                    confirm.setContentText("Durée: 14 jours. Confirmer?");
                    confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                        if (loanDAO.createLoan(uid, book.getId())) {
                            bookDAO.decrementAvailable(book.getId());
                            loadBooks();
                            loadMyLoans();
                            showAlert("Succès", "Emprunt enregistré! À rendre dans 14 jours.", Alert.AlertType.INFORMATION);
                        }
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Book b = getTableView().getItems().get(getIndex());
                btn.setDisable(!b.isAvailable());
                btn.setStyle(b.isAvailable()
                    ? "-fx-background-color:#3b82f6;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;"
                    : "-fx-background-color:#9ca3af;-fx-text-fill:white;-fx-background-radius:6;");
                setGraphic(btn);
            }
        };
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
