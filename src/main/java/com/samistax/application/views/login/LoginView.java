package com.samistax.application.views.login;

import com.samistax.application.security.AuthenticatedUser;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Map;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
@CssImport(value = "./themes/messagingdemo/views/login-view.css", themeFor = "vaadin-login-overlay-wrapper")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private boolean SIGNUP_MODE = false;
    private LoginI18n i18n;
    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Astra CDC demo");
        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);
        if ( SIGNUP_MODE ) {
            i18n.getForm().setTitle("Sign Up");
        }

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }
        Map parameters = event.getLocation().getQueryParameters().getParameters();
        setError(parameters.containsKey("error"));
        if ( parameters.containsKey("signup") ) {
            SIGNUP_MODE = true;
            i18n.getForm().setTitle("Sign Up");
        }
    }
}
