import akka.actor.AbstractActor;
 import messages.ArticalBody;
import messages.ParseHtmlArticle;

public class ParsingActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ParseHtmlArticle.class , htmlArticle -> {
//        String body = ArticleExtractor.getInstance().getText(htmlArticle.htmlString);
        sender().tell(new ArticalBody(htmlArticle.uri , "fuck body") , self());


        }).build();
    }
}
