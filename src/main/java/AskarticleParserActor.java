import InMemoryAkkaDB.messages.Get;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Status;
import akka.util.Timeout;
import messages.ArticalBody;
import messages.HttpResponse;
import messages.ParseArticle;
import messages.ParseHtmlArticle;

import static akka.pattern.PatternsCS.ask;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AskarticleParserActor extends AbstractActor {

   private final ActorSelection cacheActor;
   private final ActorSelection httpClientActor;
   private final ActorSelection articleParserActor;
   private final Timeout timeout;


    public AskarticleParserActor(String cacheActor, String httpClientActor, String articleParserActor, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActor);
        this.httpClientActor = context().actorSelection(httpClientActor);
        this.articleParserActor =context().actorSelection( articleParserActor);
        this.timeout = timeout;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ParseArticle.class,
                message ->{

            final CompletionStage cacheResult = ask(cacheActor ,new Get(message.url) , 3000);

            final CompletionStage result = cacheResult.handle((x , t) ->
                  (x != null) ? CompletableFuture.completedFuture(x) :
                                        ask(httpClientActor , message.url ,3000 )
                                        .thenCompose(rawArtical -> ask(articleParserActor , new ParseHtmlArticle(message.url , ((HttpResponse)rawArtical).body) ,3000))
            ).thenCompose(o -> o);

            final ActorRef sender = sender();
            result.handle((x, t) ->{

                if (x != null){

                    if (x instanceof ArticalBody ){
                        String body =  ((ArticalBody)x).body;

                        cacheActor.tell(body , self());
                        sender.tell(body , self());
                    }else if(x instanceof String){
                        sender.tell(x ,self());
                    }

                    else if (x== null)
                        sender.tell(new Status.Failure((Throwable)t) , self());
                }
                
                return null;


            } );


                }


        )
                .build();
    }
}
