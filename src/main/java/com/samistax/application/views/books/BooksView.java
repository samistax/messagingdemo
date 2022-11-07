package com.samistax.application.views.books;

import com.samistax.application.data.Role;
import com.samistax.application.data.entity.SampleBook;
import com.samistax.application.data.entity.astra.BookUpdate;
import com.samistax.application.data.service.SampleBookService;
import com.samistax.application.data.service.astra.BookChangeLogService;
import com.samistax.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToFloatConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Book Management")
@Route(value = "books/:sampleBookID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class BooksView extends Div implements BeforeEnterObserver {

    private final String SAMPLEBOOK_ID = "sampleBookID";
    private final String SAMPLEBOOK_EDIT_ROUTE_TEMPLATE = "books/%s/edit";

    private final Grid<SampleBook> grid = new Grid<>(SampleBook.class, false);

    private Upload image;
    private Image imagePreview;
    private TextField name;
    private TextField author;
    private DatePicker publicationDate;
    private TextField pages;
    private TextField isbn;
    private TextField qty;
    private TextField price;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SampleBook> binder;

    private SampleBook sampleBook;
    private SampleBook sampleBookOld;

    private final SampleBookService sampleBookService;
    private final BookChangeLogService bookChangeLogService;

    @Autowired
    public BooksView(SampleBookService sampleBookService,
                     BookChangeLogService bookChangeLogService) {

        this.sampleBookService = sampleBookService;
        this.bookChangeLogService = bookChangeLogService;

        addClassNames("books-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<SampleBook> imageRenderer = LitRenderer
                .<SampleBook>of("<img style='height: 64px' src=${item.image} />").withProperty("image", item -> {
                    if (item != null && item.getImage() != null) {
                        return "data:image;base64," + Base64.getEncoder().encodeToString(item.getImage());
                    } else {
                        return "";
                    }
                });
        grid.addColumn(imageRenderer).setHeader("Image").setWidth("68px").setFlexGrow(0);
        grid.addColumn("isbn").setAutoWidth(true);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("author").setAutoWidth(true);
        grid.addColumn("publicationDate").setAutoWidth(true);
        grid.addColumn("pages").setAutoWidth(true);
        grid.addColumn("price").setAutoWidth(true);
        grid.addColumn("qty").setAutoWidth(true);

        grid.setItems(query -> sampleBookService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEBOOK_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BooksView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SampleBook.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(pages).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("pages");
        binder.forField(qty).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("qty");
        binder.forField(price).withConverter(new StringToFloatConverter("Must enter a number")).bind("price");
        binder.bindInstanceFields(this);

        attachImageUpload(image, imagePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sampleBook == null) {
                    this.sampleBook = new SampleBook();
                }
                // Persist new sampleBook
                binder.writeBean(this.sampleBook);
                sampleBookService.update(this.sampleBook);

                // Determine and store changed value to Book_changelog table in AStra DB
                try {
                    BookUpdate bookLog = createChangeLog(this.sampleBook, this.sampleBookOld);
                    if ( bookLog != null ) {
                        bookChangeLogService.update(bookLog);
                    }
                } catch (Exception ex ){
                    System.out.println("Exception while trying to persist book change log " +  ex);
                }

                clearForm();
                refreshGrid();
                Notification.show("SampleBook details stored.");
                UI.getCurrent().navigate(BooksView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sampleBook details.");
            }
        });

    }
    private BookUpdate createChangeLog(SampleBook newItem, SampleBook oldItem) {
        BookUpdate bookLog = new BookUpdate();

        // Check what has changed
        HashMap<String, String> changes = new HashMap<>();
        if ( newItem.getImage() != oldItem.getImage() ) {
            changes.put("image", newItem.getImage().toString());
        }
        if (! newItem.getAuthor().equals(oldItem.getAuthor()) ){
            changes.put("author", newItem.getAuthor());
        }
        if (! newItem.getIsbn().equals(oldItem.getIsbn()) ){
            changes.put("isbn", newItem.getIsbn());
        }
        if (! newItem.getName().equals(oldItem.getName()) ){
            changes.put("name", newItem.getName());
        }
        if ( newItem.getPages().compareTo(oldItem.getPages()) != 0){
            changes.put("pages", newItem.getPages().toString());
        }
        if ( newItem.getPrice().compareTo(oldItem.getPrice()) != 0)  {
            changes.put("price", newItem.getPrice().toString());
        }
        if ( newItem.getQty().compareTo(oldItem.getQty()) != 0 ){
            changes.put("qty", newItem.getQty().toString());
        }
        if (! newItem.getPublicationDate().isEqual(oldItem.getPublicationDate()) ){
            changes.put("publicationDate", newItem.getPublicationDate().toString());
        }
        bookLog.setUpdatedValues(changes);
        if ( changes.size() > 0 ) {
            // log all new values
            bookLog.setIsbn(newItem.getIsbn());
            //bookLog.setImage(newItem.getImage()); // Do not store image value to keep the table queries using CQL human readable.
            bookLog.setAuthor(newItem.getAuthor());
            bookLog.setName(newItem.getName());
            bookLog.setPages(newItem.getPages());
            bookLog.setPrice(newItem.getPrice());
            bookLog.setQty(newItem.getQty());
            bookLog.setPublicationDate(newItem.getPublicationDate());
            // Add timestamp
            bookLog.setUpdatedAt(LocalDateTime.now());

            return bookLog;
        }
        return null;
    }
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> sampleBookId = event.getRouteParameters().get(SAMPLEBOOK_ID).map(UUID::fromString);
        if (sampleBookId.isPresent()) {
            Optional<SampleBook> sampleBookFromBackend = sampleBookService.get(sampleBookId.get());
            if (sampleBookFromBackend.isPresent()) {
                populateForm(sampleBookFromBackend.get());
            } else {
                Notification.show(String.format("The requested sampleBook was not found, ID = %s", sampleBookId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BooksView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        Label imageLabel = new Label("Image");
        imagePreview = new Image();
        imagePreview.setWidth("100%");
        image = new Upload();
        image.getStyle().set("box-sizing", "border-box");
        image.getElement().appendChild(imagePreview.getElement());
        name = new TextField("Name");
        author = new TextField("Author");
        publicationDate = new DatePicker("Publication Date");
        pages = new TextField("Pages");
        isbn = new TextField("Isbn");
        qty = new TextField("Qty");
        price = new TextField("Price");

        formLayout.add(imageLabel, image, isbn, name, author, publicationDate, pages, qty, price);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            uploadBuffer.reset();
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            StreamResource resource = new StreamResource(e.getFileName(),
                    () -> new ByteArrayInputStream(uploadBuffer.toByteArray()));
            preview.setSrc(resource);
            preview.setVisible(true);
            if (this.sampleBook == null) {
                this.sampleBook = new SampleBook();
            }
            this.sampleBook.setImage(uploadBuffer.toByteArray());
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SampleBook value) {
        this.sampleBook = value;
        if ( value != null ) {
            sampleBookOld = value.clone();
        } else {
            sampleBookOld = null;
        }

        binder.readBean(this.sampleBook);
        this.imagePreview.setVisible(value != null);
        if (value == null || value.getImage() == null) {
            this.image.clearFileList();
            this.imagePreview.setSrc("");
        } else {
            this.imagePreview.setSrc("data:image;base64," + Base64.getEncoder().encodeToString(value.getImage()));
        }

    }
}
