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
import static akka.pattern.PatternsCS.ask;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestTell {

    ActorSystem system = ActorSystem.create("system");

    TestProbe cacheProp = new TestProbe(system);
    TestProbe httpClientProp = new TestProbe(system);
    ActorRef parserActor = system.actorOf(Props.create(ParsingActor.class));
    Timeout timeout = Timeout.apply(1000 , TimeUnit.SECONDS);

    ActorRef tellParserActor = system.actorOf(Props.create(TellArticleParser.class ,
            cacheProp.ref().path().toString(),
            httpClientProp.ref().path().toString(),
            parserActor.path().toString(),
            timeout

            ));

    @Test
    public void itShouldParseArticleTest() throws InterruptedException, ExecutionException, TimeoutException {

        CompletionStage<Object> ask = ask(tellParserActor, new ParseArticle("www.google.com"), timeout);

        cacheProp.expectMsgClass(Get.class);
        cacheProp.reply(new Status.Failure(new Exception("no cache")));


        httpClientProp.expectMsgClass(String.class);
        httpClientProp.reply(new HttpResponse(Articles.article1));


        String result  =(String) ask.toCompletableFuture().get(1000, TimeUnit.SECONDS);

        assert(result.contains("fuck body"));
        assert(!result.contains("<body>"));



    }



}
