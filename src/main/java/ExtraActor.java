import InMemoryAkkaDB.messages.Set;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Status;
import messages.ArticleBody;
import messages.HttpResponse;
import messages.ParseHtmlArticle;

import java.util.concurrent.TimeoutException;

class ExtraActor extends AbstractActor
{

    private final ActorRef sender ;
    private final ActorRef articleParserActor ;
    private final ActorRef cacheActor ;
    private final String url ;

    public ExtraActor(ActorRef sender, ActorRef articleParserActor, ActorRef cacheActor, String url) {
        this.sender = sender;
        this.articleParserActor = articleParserActor;
        this.cacheActor = cacheActor;
        this.url = url;
    }


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
                .match(HttpResponse.class , httpResponse -> {
                    articleParserActor.tell(new ParseHtmlArticle(url , httpResponse.body) , self());
                })
                .match(ArticleBody.class , articleBody -> {
                    cacheActor.tell(new Set(url , articleBody.body) , self());
                    sender.tell(articleBody.body , self());
                    context().stop(self());

                })
                .matchAny(o -> {
                    System.out.println(" inValid Message Type ");
                })
                .build();
    }
}