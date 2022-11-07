package com.samistax.application.views.signup;

import com.samistax.application.components.ui.AvatarField;
import com.samistax.application.components.ui.AvatarImage;
import com.samistax.application.data.Role;
import com.samistax.application.data.entity.User;
import com.samistax.application.data.service.UserService;
import com.samistax.application.views.MainLayout;
import com.samistax.application.views.login.LoginView;
import com.samistax.application.views.support.SupportView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.*;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@PageTitle("Sign Up")
@Route(value = "signup", layout = MainLayout.class)
@AnonymousAllowed
public class SignupView extends VerticalLayout {

    private UserService service;
    private UserDetailsService detailsService;

    private PasswordField passwordField1;
    private PasswordField passwordField2;

    private AvatarField avatarField;

    private BeanValidationBinder<User> binder;
    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;
    private PasswordEncoder passwordEncoder;
    @Autowired
    public SignupView(UserService service,
                      UserDetailsService detailsService,
                      PasswordEncoder passwordEncoder) {
        this.service = service;
        this.detailsService = detailsService;
        this.passwordEncoder = passwordEncoder;

        /*
         * Create the components we'll need
         */
        H2 title = new H2("Signup form");

        TextField usernameField = new TextField("Username");

        // This is a custom field we create to handle the field 'avatar' in our data. It
        // work just as any other field, e.g. the TextFields above. Instead of a String
        // value, it has an AvatarImage value.
        avatarField = new AvatarField("Select Avatar image");

        passwordField1 = new PasswordField("User password");
        passwordField2 = new PasswordField("Password again");

        passwordField1.setValue("demo");
        passwordField2.setValue("demo");

        Span errorMessage = new Span();

        Button submitButton = new Button("Join the demo");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*
         * Build the visible layout
         */

        // Create a FormLayout with all our components. The FormLayout doesn't have any
        // logic (validation, etc.), but it allows us to configure Responsiveness from
        // Java code and its defaults looks nicer than just using a VerticalLayout.
        FormLayout formLayout = new FormLayout(
                title,
                usernameField,
                avatarField,
                passwordField1,
                passwordField2,
                errorMessage,
                submitButton);


        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // Allow the form layout to be responsive. On device widths 0-490px we have one
        // column, then we have two. Field labels are always on top of the fields.
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        // These components take full width regardless if we use one column or two (it
        // just looks better that way)
        formLayout.setColspan(title, 2);
        formLayout.setColspan(avatarField, 2);
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(submitButton, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");

        // Add the form to the page
        add(formLayout);

        /*
         * Set up form functionality
         */

        /*
         * Binder is a form utility class provided by Vaadin. Here, we use a specialized
         * version to gain access to automatic Bean Validation (JSR-303). We provide our
         * data class so that the Binder can read the validation definitions on that
         * class and create appropriate validators. The BeanValidationBinder can
         * automatically validate all JSR-303 definitions, meaning we can concentrate on
         * custom things such as the passwords in this class.
         */
        binder = new BeanValidationBinder<User>(User.class);

        // Basic User fields that are required to fill in
        binder.forField(usernameField).asRequired().bind("username");

        // Here we use our custom Vaadin component to handle the image portion of our
        // data, since Vaadin can't do that for us. Because the AvatarField is of type
        // HasValue<AvatarImage>, the Binder can bind it automatically. The avatar is
        // not required and doesn't have a validator, but could.
        binder.forField(avatarField).withConverter(new AvatarConverter()).bind("profilePicture");


        // Another custom validator, this time for passwords
        binder.forField(passwordField1)
                .asRequired().withValidator(this::passwordValidator).bind("hashedPassword");
        // We won't bind passwordField2 to the Binder, because it will have the same
        // value as the first field when correctly filled in. We just use it for
        // validation.

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes. The easiest way
        // is just to do that manually.
        passwordField2.addValueChangeListener(e -> {

            // The user has modified the second field, now we can validate and show errors.
            // See passwordValidator() for how this flag is used.
            enablePasswordValidation = true;

            binder.validate();
        });

        // A label where bean-level error messages go
        binder.setStatusLabel(errorMessage);

        // And finally the submit button
        submitButton.addClickListener(e -> {
            try {

                // Create empty bean to store the details into
                User userBean = new User();

                // Run validators and write the values to the bean
                binder.writeBean(userBean);

                // Modify additional values and encode password
                userBean.setRoles(Set.of(Role.USER));
                userBean.setName("Guest "+userBean.getUsername());

                // Convert plain password binded to object to hashed pwd
                userBean.setHashedPassword(passwordEncoder.encode(userBean.getHashedPassword()));

                if ( avatarField.getValue() != null ) {
                    userBean.setProfilePicture(avatarField.getValue().getImage());
                }
                // Call backend to store the data
                service.register(userBean);

                // Show success message if everything went well
                showSuccess(userBean);

            } catch (ValidationException e1) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            } catch (UserService.UserServiceException e2) {

                // For some reason, the save failed in the back end.

                // First, make sure we store the error in the server logs (preferably using a
                // logging framework)
                e2.printStackTrace();

                // Notify, and let the user try again.
                errorMessage.setText("Registration failed: "+ e2.getMessage());
            }
        });

/*

        TextField username = new TextField("Username");
        PasswordField password1 = new PasswordField("Password (default: demo)");
      //  PasswordField password2 = new PasswordField("Confirm password");
        username.setPlaceholder("User " + service.count());
        password1.setValue("demo");
    //    password2.setValue("demo");
        add( new H2("Register"),
            username,
            password1,
            //password2,
            new Button("Send", event -> register(
                    username.getValue(),
                password1.getValue(),
                password1.getValue())
            )
        );

 */
    }
    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass1 == null || pass1.length() < 4) {
            return ValidationResult.error("Password should be at least 4 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = passwordField2.getValue();
        if (pass1 != null && pass2.equals(pass1) ) {
        //&& passwordEncoder.matches(pass2, pass1) ) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(User userBean) {
        Notification notification = Notification.show("Welcome onboard,  " + userBean.getUsername());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.TOP_CENTER);

        // The following not recommended way of doing thing but for demo purposes want to reduce clicks required to join demo with own device.
        UserDetails userDetails = detailsService.loadUserByUsername(userBean.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername (),userDetails.getPassword (),userDetails.getAuthorities ());
        SecurityContextHolder.getContext().setAuthentication(auth);
        UI.getCurrent().navigate(SupportView.class);
    }
    class PasswordConverter implements Converter<String, String> {
        @Override
        public Result<String> convertToModel(
                String fieldValue, ValueContext context) {
            // Produces a converted value or an error
            PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
            try {
                // ok is a static helper method that
                // creates a Result
                return Result.ok(pwdEncoder.encode(fieldValue));
            } catch (NumberFormatException e) {
                // error is a static helper method
                // that creates a Result
                return Result.error("Password hashing failed");
            }
        }

        @Override
        public String convertToPresentation(
                String hashedPassword, ValueContext context) {
            // Converting to the field type should
            // always succeed, so there is no support for
            // returning an error Result.

            return String.valueOf(hashedPassword);
        }
    }
    class AvatarConverter implements Converter<AvatarImage, byte[]> {
        @Override
        public Result<byte[]> convertToModel(
                AvatarImage fieldValue, ValueContext context) {
            // Produces a converted value or an error
            try {

                // ok is a static helper method that
                // creates a Result
                return Result.ok(fieldValue.getImage());
            } catch (NumberFormatException e) {
                // error is a static helper method
                // that creates a Result
                return Result.error("Enter a number");
            }
        }

        @Override
        public AvatarImage convertToPresentation(
                byte[] profilePicture, ValueContext context) {
            // Converting to the field type should
            // always succeed, so there is no support for
            // returning an error Result.
            AvatarImage img = new AvatarImage();
            img.setImage(profilePicture);
            return img;
        }
    }
}
