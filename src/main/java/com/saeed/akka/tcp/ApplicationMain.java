package com.saeed.akka.tcp;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import com.saeed.akka.tcp.client.ClientActor;
import com.saeed.akka.tcp.client.ListenerActor;
import com.saeed.akka.tcp.server.ManagerActor;
import com.saeed.akka.tcp.server.ServerActor;

import java.net.InetSocketAddress;

public class ApplicationMain {

    public static void main(String[] args) {
        ActorSystem serverActorSystem = ActorSystem.create("ServerActorSystem");

        ActorRef managerActor = serverActorSystem.actorOf(ManagerActor.props(), "managerActor");
        ActorRef serverActor = serverActorSystem.actorOf(ServerActor.props(managerActor), "serverActor");

        ActorSystem clientActorSystem = ActorSystem.create("ClientActorSystem");

        ActorRef listenerActor = clientActorSystem.actorOf(ListenerActor.props(), "listenerActor");
        ActorRef clientActor = clientActorSystem.actorOf(ClientActor.props(new InetSocketAddress("localhost", 9090), listenerActor), "clientActor");

        serverActorSystem.awaitTermination();
        clientActorSystem.awaitTermination();


    }

}