package com.saeed.akka.tcp.client;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;

public class ClientActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorRef tcpActor;
    final InetSocketAddress remote;

    public static Props props(InetSocketAddress remote, ActorRef tcpActor) {
        return Props.create(ClientActor.class, remote, tcpActor);
    }

    public ClientActor(InetSocketAddress remote, ActorRef tcpActor) {
        this.remote = remote;
        this.tcpActor = tcpActor;

        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }

        tcpActor.tell(TcpMessage.connect(remote), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof CommandFailed) {
            log.info("In ClientActor - received message: failed");
            getContext().stop(getSelf());

        } else if (msg instanceof Connected) {
            log.info("In ClientActor - received message: connected");

            getSender().tell(TcpMessage.register(getSelf()), getSelf());
            getContext().become(connected(getSender()));

            getSender().tell(TcpMessage.write(ByteString.fromArray("hello".getBytes())), getSelf());
        }
    }

    private Procedure<Object> connected(final ActorRef connection) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object msg) throws Exception {

                if (msg instanceof ByteString) {
                    connection.tell(TcpMessage.write((ByteString) msg), getSelf());

                } else if (msg instanceof CommandFailed) {
                    // OS kernel socket buffer was full

                } else if (msg instanceof Received) {
                    log.info("In ClientActor - Received message: " + ((Received) msg).data().utf8String());

                } else if (msg.equals("close")) {
                    connection.tell(TcpMessage.close(), getSelf());

                } else if (msg instanceof ConnectionClosed) {
                    getContext().stop(getSelf());
                }
            }
        };
    }

    public static class Failed {
    }


}