import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.io.UdpConnected;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import akka.util.ByteString;
import com.saeed.akka.tcp.client.ClientActor;
import com.saeed.akka.tcp.server.ServerActor;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

/**
 * Created by saeed on 28/April/15 AD.
 */
public class ActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testClientActor() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9090);
        InetSocketAddress inetSocketAddressLocal = new InetSocketAddress("localhost", 9091);

        TestProbe tcpProbe = new TestProbe(system);

        ActorRef tcpRef = tcpProbe.ref();
        ActorRef clientActor = system.actorOf(ClientActor.props(inetSocketAddress, tcpRef), "clientActor");

        tcpProbe.expectMsg(TcpMessage.connect(inetSocketAddress));

        tcpProbe.send(clientActor, new Tcp.Connected(inetSocketAddress, inetSocketAddressLocal));

        tcpProbe.expectMsg(TcpMessage.register(clientActor));

        String hello = "hello";
        tcpProbe.expectMsg(TcpMessage.write(ByteString.fromArray(hello.getBytes())));

        tcpProbe.send(clientActor, new Tcp.Received(ByteString.fromArray(("echo "+hello).getBytes())));
    }

        @Test
    public void testServerActor() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9090);

        TestProbe tcpProbe = new TestProbe(system);

        ActorRef tcpRef = tcpProbe.ref();
        ActorRef serverActor = system.actorOf(ServerActor.props(tcpRef), "serverActor");


        tcpProbe.expectMsg(TcpMessage.bind(serverActor, inetSocketAddress, 100));

        tcpProbe.send(serverActor, new Tcp.Bound(inetSocketAddress));
    }

    @Test
    public void demonstrateTestActorBehavior() throws Exception {
        final Props props = Props.create(MyActor.class);
        final TestActorRef<MyActor> ref = TestActorRef.create(system, props, "testB");
        final Future<Object> future = akka.pattern.Patterns.ask(ref, "say42", 3000);
        assertTrue(future.isCompleted());
        assertEquals(42, Await.result(future, Duration.Zero()));
    }


    /////////test actor

    static class MyActor extends UntypedActor {
        public void onReceive(Object o) throws Exception {
            if (o.equals("say42")) {
                getSender().tell(42, getSelf());
            } else if (o instanceof Exception) {
                throw (Exception) o;
            }
        }
        public boolean testMe() { return true; }
    }


}
