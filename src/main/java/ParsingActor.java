import akka.actor.AbstractActor;
 import messages.ArticleBody;
import messages.ParseHtmlArticle;

public class ParsingActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ParseHtmlArticle.class , htmlArticle -> {
//        String body = ArticleExtractor.getInstance().getText(htmlArticle.htmlString);
        sender().tell(new ArticleBody(htmlArticle.uri , "fuck body") , self());


        }).build();
    }
}
