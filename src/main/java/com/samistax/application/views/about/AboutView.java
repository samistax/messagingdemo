package com.samistax.application.views.about;

import com.samistax.application.data.service.UserService;
import com.samistax.application.views.MainLayout;
import com.samistax.application.views.login.LoginView;
import com.samistax.application.views.signup.SignupView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.wontlost.zxing.ZXingVaadinWriter;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.UnknownHostException;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
public class AboutView extends VerticalLayout {

    public AboutView(UserService userService) {
        setSpacing(false);

        Image img = new Image("images/datastax-white.png", "DataStax logo");
        img.setWidth("200px");
        add(img);

        add(new H2("Delivering products that the developers love and that will change the trajectory of the enterprises they work for."));
        //add(new Paragraph("Copyright © 2022 DataStax."));

        add(new Paragraph("Show this QR code to invite a new user to the demo"));

        // Retrieve dynamically the QR code address and generate QR code
        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            // This is your own method that you may do something with the url.
            // Note that this method runs asynchronously

            InetAddress ipAddr = null;
            try {
                 ipAddr = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
            String hostName = currentUrl.getHost();
            // Replace localhost with public host address if available
            if ( ipAddr != null && ipAddr.getHostAddress() != null ) {
                hostName = ipAddr.getHostAddress();
            }
            String signupURL =
                    currentUrl.getProtocol()+"://"+ hostName+":"+ currentUrl.getPort()+"/"+
                    RouteConfiguration.forSessionScope().getUrl(SignupView.class);

            // Generate and add QR code to UI
            FlexLayout qrCodeBackground = new FlexLayout();
            qrCodeBackground.getStyle().set("background","white");
            ZXingVaadinWriter zXingVaadin = new  ZXingVaadinWriter();
            zXingVaadin.setSize(300);
            zXingVaadin.setValue(signupURL);
            qrCodeBackground.add(zXingVaadin);
            add(qrCodeBackground);
        });

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
