package com.samistax.application.views.support;

import com.samistax.application.data.entity.User;
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
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;

@PageTitle("Support")
@Route(value = "support", layout = MainLayout.class)
@PermitAll
public class SupportView extends VerticalLayout implements BeforeLeaveObserver {
    private AstraStreamingService astraCDCService;
    private AuthenticatedUser authenticatedUser;

    public SupportView(AuthenticatedUser authenticatedUser, AstraStreamingService astraCDCService) {
        this.authenticatedUser = authenticatedUser;
        this.astraCDCService = astraCDCService;

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
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        if ( astraCDCService != null ) {
            // astraCDCService.stopAsynchConsumer();
        }
    }
}