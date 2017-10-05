import InMemoryAkkaDB.messages.Get;
import InMemoryAkkaDB.messages.Set;
import akka.actor.*;
import akka.util.Timeout;
import messages.ArticleBody;
import messages.HttpResponse;
import messages.ParseArticle;
import messages.ParseHtmlArticle;

import java.util.concurrent.TimeoutException;

public class TellArticleParser extends AbstractActor {

   private final ActorSelection cacheActor;
   private final ActorSelection httpClientActor;
   private final ActorSelection articleParserActor;
   private final Timeout timeout;

    public TellArticleParser(String cacheActor, String httpClientActor, String articleParser, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActor);
        this.httpClientActor =  context().actorSelection(httpClientActor) ;
        this.articleParserActor = context().actorSelection(articleParser);
        this.timeout = timeout;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ParseArticle.class ,
                        message -> {

                            ActorRef extraActor = buildExtraActor(sender() , message.url);
                            cacheActor.tell(new Get(message.url), extraActor);
                            httpClientActor.tell(message.url, extraActor);

                            context().system().scheduler().scheduleOnce(timeout.duration() , extraActor ,"timeout" , context().system().dispatcher() , ActorRef.noSender());


                        })
                .build();
    }

    private ActorRef buildExtraActor(ActorRef sender, String url) {

        class ExtraActor extends AbstractActor
        {

//            public ExtraActor(){
//
//                createReceive();
//            }
            @Override
            public Receive createReceive() {
                return receiveBuilder()
                        .match(String.class , s -> s.equals("timeout") , x ->
                        {
                            sender.tell(new Status.Failure(new TimeoutException(" TimeOut ")) , self());

                            context().stop(self());
                        })
                        .match(String.class , body -> {
                            sender.tell(body , self());
                            context().stop(self());

                        })
                        .match(ArticleBody.class , articleBody -> {
                            cacheActor.tell(new Set(url , articleBody.body) , self());
                            sender.tell(articleBody.body , self());
                            context().stop(self());

                        })
                        .match(HttpResponse.class , httpResponse -> {
                            articleParserActor.tell(new ParseHtmlArticle(url , httpResponse.body) , self());
                        })

                        .matchAny(o -> {
                            System.out.println(" inValid Message Type ");
                        })
                        .build();
            }
        }

        return  context().actorOf(Props.create(ExtraActor.class ,()-> new ExtraActor()));




    }
}
