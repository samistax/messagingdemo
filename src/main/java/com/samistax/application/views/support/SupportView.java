package com.samistax.application.views.support;

import com.samistax.application.data.entity.User;
import com.samistax.application.data.service.UserService;
import com.samistax.application.data.service.astra.AstraStreamingService;
import com.samistax.application.security.AuthenticatedUser;
import com.samistax.application.views.MainLayout;
import com.vaadin.collaborationengine.*;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

@PageTitle("Support Chat")
@Route(value = "support", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed({"ADMIN","USER"})
public class SupportView extends VerticalLayout implements BeforeLeaveObserver,BeforeEnterObserver {
    private AstraStreamingService astraCDCService;
    private AuthenticatedUser authenticatedUser;
    private UserService userService;

    public SupportView(AuthenticatedUser authenticatedUser, AstraStreamingService astraCDCService, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.astraCDCService = astraCDCService;
        this.userService = userService;

        String topicId = "chat/#general";
        addClassName("support-view");
        setSpacing(false);
        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        //UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");
        UserInfo userInfo = null;
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            userInfo = new UserInfo(user.getId().toString(), user.getName());
        } else {
            userInfo = new UserInfo("0", "Anonymous User");
        }

        // Tabs allow us to change chat rooms.
        Tabs tabs = new Tabs(new Tab("#general"), new Tab("#support"), new Tab("#casual"));
        tabs.setWidthFull();

        // `CollaborationMessageList` displays messages that are in a
        // Collaboration Engine topic. You should give in the user details of
        // the current user using the component, and a topic Id. Topic id can be
        // any freeform string. In this template, we have used the format
        // "chat/#general". Check
        // https://vaadin.com/docs/latest/ce/collaboration-message-list/#persisting-messages
        // for information on how to persisting are retrieving messages over
        // server restarts.
        CollaborationMessageList list = new CollaborationMessageList(userInfo, topicId);
        UserInfo finalUserInfo = userInfo;
        list.setMessageConfigurator((message, user) -> {
            if (user.getId().equals("astra-cdc")) {
                message.addThemeNames("astracdc-user");
            } else if (finalUserInfo != null && user.getId().equals(finalUserInfo.getId())) {
                message.addThemeNames("current-user");
            } else {
                message.addThemeNames("other-user");
            }
            // Monitor for keywords and if found send pulsar message
            if ( message.getText().toLowerCase().contains("astra") ||
                    message.getText().toLowerCase().contains("pulsar"))  {
                // TODO: Demonstrate how to send Pulsar message and sink based on keyword
                // astraCDCService.sendPulsarMessage(message);
            }

        });
        list.setWidthFull();
        list.addClassNames("chat-view-message-list");

        // `CollaborationMessageInput is a textfield and button, to be able to
        // submit new messages. To avoid having to set the same info into both
        // the message list and message input, the input takes in the list as an
        // constructor argument to get the information from there.
        CollaborationMessageInput input = new CollaborationMessageInput(list);
        input.addClassNames("chat-view-message-input");
        input.setWidthFull();


        UserInfo pulsarUser = new UserInfo("astra-cdc", "Astra CDC", "https://plugins.jetbrains.com/files/17013/169775/icon/pluginIcon.svg");
        MessageManager msgManager = new MessageManager(this, userInfo,topicId );
        //astraCDCService.setMessageManagerParent(this);

        // Start Astra Streaming consumer to listen to incoming messages. Provide Chat engine and topic id where to push messages.
        astraCDCService.startAsynchConsumer(CollaborationEngine.getInstance(), topicId);

        CollaborationAvatarGroup avatarGroup = new CollaborationAvatarGroup(userInfo, topicId);
        /* avatarGroup.setImageProvider(userInfo -> {
            StreamResource streamResource = new StreamResource(
                    "avatar_" + userInfo.getId(), () -> {
                // The following not recommended way of doing thing but for demo purposes want to reduce clicks required to join demo with own device.
                User user = userService.get(userInfo.getId());
                return new ByteArrayInputStream(userDetails..getProfilePicture());
            });
            streamResource.setContentType("image/png");
            return streamResource;
        }); */

        avatarGroup.setClassName("avatar-label");
        Span label = new Span("Active users online: ");
        label.setWidthFull();
        label.setClassName("avatar-label");
        HorizontalLayout avatarBanner = new HorizontalLayout(label, avatarGroup);
        avatarBanner.setAlignItems(Alignment.CENTER);
        avatarBanner.setClassName("avatar-banner");

        // Layouting
        //add(tabs, list, input);
        add(list, input, avatarBanner);

        setSizeFull();
        expand(list);

        // Change the topic id of the chat when a new tab is selected
        tabs.addSelectedChangeListener(event -> {
            String channelName = event.getSelectedTab().getLabel();
            list.setTopic("chat/" + channelName);
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if ( astraCDCService != null ) {
            // astraCDCService.stopAsynchConsumer();
        }
    }
    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        if ( astraCDCService != null ) {
            // astraCDCService.stopAsynchConsumer();
        }
    }
}