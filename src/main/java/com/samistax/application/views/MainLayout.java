package com.samistax.application.views;

import com.samistax.application.components.appnav.AppNav;
import com.samistax.application.components.appnav.AppNavItem;
import com.samistax.application.data.entity.User;
import com.samistax.application.security.AuthenticatedUser;
import com.samistax.application.views.about.AboutView;
import com.samistax.application.views.books.BooksView;
import com.samistax.application.views.support.SupportView;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;
    private Footer footer;
    private AppNav navigation;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {

        Image headerLogo = new Image("images/datastax-square.png","logo");
        headerLogo.setWidth(20, Unit.PIXELS);
        H1 appName = new H1("Astra CDC Demo");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.add(headerLogo,appName);
        headerLayout.setFlexGrow(0.2, headerLogo);
        headerLayout.setFlexGrow(0.8, appName);
        Header header = new Header(headerLayout);

        Scroller scroller = new Scroller(updateNavigation(navigation = new AppNav()));
        addToDrawer(header, scroller, updateFooter(footer  = new Footer()));
    }

    private AppNav updateNavigation(AppNav nav) {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        //AppNav nav = new AppNav();
        nav.removeAllItems();

        if (accessChecker.hasAccess(BooksView.class)) {
            nav.addItem(new AppNavItem("Book Management", BooksView.class, "la la-book-open"));

        }
        if (accessChecker.hasAccess(SupportView.class)) {
            nav.addItem(new AppNavItem("Support Chat", SupportView.class, "la la-comments"));

        }
        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(new AppNavItem("About", AboutView.class, "la la-file"));

        }
        return nav;
    }

    private Footer updateFooter(Footer layout) {
        //Footer layout = new Footer();
        layout.removeAll();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            if ( user.getProfilePicture() != null ) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(user.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
        updateNavigation(navigation);
        updateFooter(footer);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
