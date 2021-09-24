import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class TextRecoRequestStreamHandler extends SpeechletRequestStreamHandler
{
    private static final Set<String> supportedApplicationIds;

    static
    {
        supportedApplicationIds = new HashSet<>();

        supportedApplicationIds.add("amzn1.ask.skill.a1956f45-7592-4ceb-9873-39fa32c5a517");
    }

    public TextRecoRequestStreamHandler()
    {
        super(new TextRecoSpeechLet(), supportedApplicationIds);
    }
}
