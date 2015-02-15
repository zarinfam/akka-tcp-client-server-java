package com.saeed.akka.tcp.client;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;

public class ListenerActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(ListenerActor.class);
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof ClientActor.Failed) {
            log.info("In ListenerActor - received message: failed");
        } else if (message instanceof Tcp.Connected){
            log.info("In ListenerActor - received message: connected");

        }
    }
}