package com.bibliotheque.controller;

import com.bibliotheque.MainApp;
import com.bibliotheque.dao.*;
import com.bibliotheque.model.*;
import com.bibliotheque.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    // Stats
    @FXML private Label lblWelcome, lblTotalBooks, lblAvailableBooks, lblTotalMembers, lblActiveLoans;

    // Books table
    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book,String> colBookTitle, colBookAuthor, colBookIsbn, colBookGenre;
    @FXML private TableColumn<Book,Integer> colBookYear, colBookQty, colBookAvail;
    @FXML private TableColumn<Book,Void> colBookActions;
    @FXML private TextField txtSearchBook;

    // Members table
    @FXML private TableView<User> tableMembers;
    @FXML private TableColumn<User,Integer> colMemberId;
    @FXML private TableColumn<User,String> colMemberName, colMemberUsername, colMemberEmail, colMemberDate;
    @FXML private TableColumn<User,Void> colMemberActions;

    // Loans table
    @FXML private TableView<Loan> tableLoans;
    @FXML private TableColumn<Loan,Integer> colLoanId;
    @FXML private TableColumn<Loan,String> colLoanMember, colLoanBook, colLoanDate, colLoanDue, colLoanStatus;
    @FXML private TableColumn<Loan,Void> colLoanActions;

    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    @FXML
    public void initialize() {
        lblWelcome.setText("👤 " + SessionManager.getCurrentUser().getFullName());
        setupBooksTable();
        setupMembersTable();
        setupLoansTable();
        refreshAll();
    }

    // ==================== STATS ====================
    private void refreshStats() {
        lblTotalBooks.setText(String.valueOf(bookDAO.getTotalBooks()));
        lblAvailableBooks.setText(String.valueOf(bookDAO.getAvailableCount()));
        lblTotalMembers.setText(String.valueOf(userDAO.getAllMembers().size()));
        lblActiveLoans.setText(String.valueOf(loanDAO.getActiveLoansCount()));
    }

    private void refreshAll() {
        refreshStats();
        loadBooks(bookDAO.getAllBooks());
        loadMembers(userDAO.getAllMembers());
        loadLoans(loanDAO.getAllLoans());
    }

    // ==================== BOOKS ====================
    private void setupBooksTable() {
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colBookAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colBookIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colBookGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colBookYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colBookQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colBookAvail.setCellValueFactory(new PropertyValueFactory<>("available"));
        colBookActions.setCellFactory(bookActionsFactory());
    }

    private void loadBooks(List<Book> books) {
        tableBooks.setItems(FXCollections.observableArrayList(books));
    }

    @FXML private void handleSearchBook() {
        String kw = txtSearchBook.getText().trim();
        loadBooks(kw.isEmpty() ? bookDAO.getAllBooks() : bookDAO.searchBooks(kw));
    }

    @FXML private void handleRefreshBooks() {
        txtSearchBook.clear();
        loadBooks(bookDAO.getAllBooks());
    }

    @FXML private void handleAddBook() {
        showBookDialog(null);
    }

    private void showBookDialog(Book book) {
        boolean isEdit = book != null;
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le Livre" : "Ajouter un Livre");
        dialog.setHeaderText(isEdit ? "Modifier les informations du livre" : "Nouveau livre");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField fTitle = new TextField(isEdit ? book.getTitle() : "");
        TextField fAuthor = new TextField(isEdit ? book.getAuthor() : "");
        TextField fIsbn = new TextField(isEdit ? book.getIsbn() : "");
        TextField fGenre = new TextField(isEdit ? book.getGenre() : "");
        TextField fYear = new TextField(isEdit ? String.valueOf(book.getYear()) : "");
        TextField fQty = new TextField(isEdit ? String.valueOf(book.getQuantity()) : "1");

        for (TextField f : new TextField[]{fTitle, fAuthor, fIsbn, fGenre, fYear, fQty})
            f.setPrefWidth(280);

        grid.add(new Label("Titre *"), 0, 0); grid.add(fTitle, 1, 0);
        grid.add(new Label("Auteur *"), 0, 1); grid.add(fAuthor, 1, 1);
        grid.add(new Label("ISBN"), 0, 2); grid.add(fIsbn, 1, 2);
        grid.add(new Label("Genre"), 0, 3); grid.add(fGenre, 1, 3);
        grid.add(new Label("Année"), 0, 4); grid.add(fYear, 1, 4);
        grid.add(new Label("Quantité *"), 0, 5); grid.add(fQty, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/bibliotheque/css/style.css").toExternalForm());

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (fTitle.getText().isBlank() || fAuthor.getText().isBlank()) {
                    showAlert("Erreur", "Titre et Auteur sont obligatoires.", Alert.AlertType.ERROR);
                    return null;
                }
                Book b = isEdit ? book : new Book();
                b.setTitle(fTitle.getText().trim());
                b.setAuthor(fAuthor.getText().trim());
                b.setIsbn(fIsbn.getText().trim());
                b.setGenre(fGenre.getText().trim());
                try { b.setYear(Integer.parseInt(fYear.getText().trim())); } catch (Exception ignored) {}
                try {
                    int qty = Integer.parseInt(fQty.getText().trim());
                    if (isEdit) { b.setAvailable(b.getAvailable() + (qty - b.getQuantity())); }
                    b.setQuantity(qty);
                } catch (Exception ignored) {}
                return b;
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(b -> {
            boolean ok = isEdit ? bookDAO.updateBook(b) : bookDAO.addBook(b);
            if (ok) { refreshAll(); showAlert("Succès", isEdit ? "Livre modifié." : "Livre ajouté.", Alert.AlertType.INFORMATION); }
            else showAlert("Erreur", "Opération échouée.", Alert.AlertType.ERROR);
        });
    }

    private Callback<TableColumn<Book,Void>, TableCell<Book,Void>> bookActionsFactory() {
        return col -> new TableCell<>() {
            final Button btnEdit = new Button("✏️");
            final Button btnDel = new Button("🗑️");
            final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#3b82f6;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
                btnDel.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
                btnEdit.setOnAction(e -> showBookDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> {
                    Book b = getTableView().getItems().get(getIndex());
                    if (confirmDelete("livre \"" + b.getTitle() + "\"")) {
                        bookDAO.deleteBook(b.getId()); refreshAll();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    // ==================== MEMBERS ====================
    private void setupMembersTable() {
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colMemberUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colMemberEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colMemberDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colMemberActions.setCellFactory(memberActionsFactory());
    }

    private void loadMembers(List<User> members) {
        tableMembers.setItems(FXCollections.observableArrayList(members));
    }

    @FXML private void handleAddMember() { showMemberDialog(null); }

    private void showMemberDialog(User user) {
        boolean isEdit = user != null;
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier Membre" : "Ajouter Membre");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField fName = new TextField(isEdit ? user.getFullName() : "");
        TextField fUser = new TextField(isEdit ? user.getUsername() : "");
        PasswordField fPass = new PasswordField();
        if (isEdit) fPass.setPromptText("Laisser vide = inchangé");
        TextField fEmail = new TextField(isEdit ? user.getEmail() : "");

        for (TextField f : new TextField[]{fName, fUser, fEmail}) f.setPrefWidth(280);

        grid.add(new Label("Nom Complet *"), 0, 0); grid.add(fName, 1, 0);
        grid.add(new Label("Identifiant *"), 0, 1); grid.add(fUser, 1, 1);
        grid.add(new Label("Mot de passe *"), 0, 2); grid.add(fPass, 1, 2);
        grid.add(new Label("Email"), 0, 3); grid.add(fEmail, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/bibliotheque/css/style.css").toExternalForm());

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (fName.getText().isBlank() || fUser.getText().isBlank()) {
                    showAlert("Erreur", "Nom et identifiant obligatoires.", Alert.AlertType.ERROR);
                    return null;
                }
                if (!isEdit && fPass.getText().isBlank()) {
                    showAlert("Erreur", "Mot de passe obligatoire.", Alert.AlertType.ERROR);
                    return null;
                }
                User u = isEdit ? user : new User();
                u.setFullName(fName.getText().trim());
                u.setUsername(fUser.getText().trim());
                if (!fPass.getText().isBlank()) u.setPassword(fPass.getText());
                u.setEmail(fEmail.getText().trim());
                u.setRole("MEMBER");
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            boolean ok = isEdit ? userDAO.updateUser(u) : userDAO.addUser(u);
            if (ok) { refreshAll(); showAlert("Succès", isEdit ? "Membre modifié." : "Membre ajouté.", Alert.AlertType.INFORMATION); }
            else showAlert("Erreur", "Opération échouée (identifiant déjà utilisé?).", Alert.AlertType.ERROR);
        });
    }

    private Callback<TableColumn<User,Void>, TableCell<User,Void>> memberActionsFactory() {
        return col -> new TableCell<>() {
            final Button btnEdit = new Button("✏️");
            final Button btnDel = new Button("🗑️");
            final HBox box = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color:#3b82f6;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
                btnDel.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
                btnEdit.setOnAction(e -> showMemberDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (confirmDelete("membre \"" + u.getFullName() + "\"")) {
                        userDAO.deleteUser(u.getId()); refreshAll();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    // ==================== LOANS ====================
    private void setupLoansTable() {
        colLoanId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLoanMember.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colLoanBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colLoanDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colLoanStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLoanActions.setCellFactory(loanActionsFactory());
    }

    private void loadLoans(List<Loan> loans) {
        tableLoans.setItems(FXCollections.observableArrayList(loans));
    }

    @FXML private void handleShowAllLoans() { loadLoans(loanDAO.getAllLoans()); }
    @FXML private void handleShowActiveLoans() { loadLoans(loanDAO.getActiveLoans()); }

    @FXML private void handleNewLoan() {
        List<User> members = userDAO.getAllMembers();
        List<Book> books = bookDAO.getAvailableBooks();

        if (members.isEmpty()) { showAlert("Info", "Aucun membre enregistré.", Alert.AlertType.INFORMATION); return; }
        if (books.isEmpty()) { showAlert("Info", "Aucun livre disponible.", Alert.AlertType.INFORMATION); return; }

        Dialog<Loan> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Emprunt");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        ComboBox<User> cbMember = new ComboBox<>(FXCollections.observableArrayList(members));
        ComboBox<Book> cbBook = new ComboBox<>(FXCollections.observableArrayList(books));
        cbMember.setPrefWidth(300); cbBook.setPrefWidth(300);

        grid.add(new Label("Membre *"), 0, 0); grid.add(cbMember, 1, 0);
        grid.add(new Label("Livre *"), 0, 1); grid.add(cbBook, 1, 1);
        grid.add(new Label("Durée: 14 jours"), 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/bibliotheque/css/style.css").toExternalForm());

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (cbMember.getValue() == null || cbBook.getValue() == null) {
                    showAlert("Erreur", "Sélectionnez un membre et un livre.", Alert.AlertType.ERROR);
                    return null;
                }
                Loan l = new Loan();
                l.setUserId(cbMember.getValue().getId());
                l.setBookId(cbBook.getValue().getId());
                return l;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(l -> {
            if (loanDAO.hasActiveLoan(l.getUserId(), l.getBookId())) {
                showAlert("Erreur", "Ce membre a déjà emprunté ce livre.", Alert.AlertType.ERROR);
                return;
            }
            boolean ok = loanDAO.createLoan(l.getUserId(), l.getBookId());
            if (ok) {
                bookDAO.decrementAvailable(l.getBookId());
                refreshAll();
                showAlert("Succès", "Emprunt enregistré (retour dans 14 jours).", Alert.AlertType.INFORMATION);
            }
        });
    }

    private Callback<TableColumn<Loan,Void>, TableCell<Loan,Void>> loanActionsFactory() {
        return col -> new TableCell<>() {
            final Button btnReturn = new Button("↩ Retour");
            {
                btnReturn.setStyle("-fx-background-color:#10b981;-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
                btnReturn.setOnAction(e -> {
                    Loan loan = getTableView().getItems().get(getIndex());
                    if ("RETURNED".equals(loan.getStatus())) return;
                    loanDAO.returnLoan(loan.getId());
                    bookDAO.incrementAvailable(loan.getBookId());
                    refreshAll();
                    showAlert("Succès", "Retour enregistré.", Alert.AlertType.INFORMATION);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Loan l = getTableView().getItems().get(getIndex());
                btnReturn.setDisable("RETURNED".equals(l.getStatus()));
                setGraphic(btnReturn);
            }
        };
    }

    // ==================== UTILS ====================
    @FXML private void handleLogout() {
        SessionManager.logout();
        MainApp.loadScene("login");
    }

    private boolean confirmDelete(String item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + item + "?");
        alert.setContentText("Cette action est irréversible.");
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
