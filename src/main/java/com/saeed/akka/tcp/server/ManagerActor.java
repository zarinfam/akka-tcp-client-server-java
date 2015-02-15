package com.saeed.akka.tcp.server;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;

public class ManagerActor extends UntypedActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public static Props props() {
        return Props.create(ManagerActor.class);
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof Tcp.Bound) {
            log.info("In ManagerActor - received message: bound");
        } else if (message instanceof Tcp.Connected){
            log.info("In ManagerActor - received message: connected");

        }
    }
}