import InMemoryAkkaDB.messages.Get;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import messages.HttpResponse;
import messages.ParseArticle;
import org.junit.Test;
import scala.concurrent.Await;

import static akka.pattern.PatternsCS.ask;


import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestAsk {

    ActorSystem actorSystem = ActorSystem.create("sys");

    Timeout timeout = Timeout.apply(1000 , TimeUnit.SECONDS);

    TestProbe cachProp = new TestProbe(actorSystem);
    TestProbe httpProp = new TestProbe(actorSystem);
    ActorRef parser = actorSystem.actorOf(Props.create(ParsingActor.class));

    ActorRef askActorDemo = actorSystem.actorOf(Props.create(AskarticleParserActor.class ,
                cachProp.ref().path().toString(),
                httpProp.ref().path().toString(),
                parser.path().toString(),
                timeout
            ));


    @Test

    public void itShouldParseArticleTest() throws InterruptedException, ExecutionException, TimeoutException {

        CompletionStage<Object> ask = ask(askActorDemo, new ParseArticle(("http://www.google.com")), timeout);

        cachProp.expectMsgClass(Get.class);
        cachProp.reply(new Status.Failure(new Exception("key not found")));

        httpProp.expectMsgClass(String.class);
        httpProp.reply(new HttpResponse(Articles.article1));

        String result = (String) ask.toCompletableFuture().get(10000 ,TimeUnit.SECONDS);

        assert(result.contains("fuck body"));
        assert(!result.contains("<body>"));


    }






}
