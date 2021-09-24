import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import org.apache.log4j.BasicConfigurator;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

public class TextRecoSpeechLet implements SpeechletV2
{
    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope)
    {

    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope)
    {
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope)
    {
        Intent intent = speechletRequestEnvelope.getRequest().getIntent();

        String intentName = (intent != null) ? intent.getName() : "";

        if (intentName != null && intentName.equals("GetImageUrl"))
        {
            String imageUrl = intent.getSlot("url").getValue();

            if (imageUrl != null && !imageUrl.equals("") && !imageUrl.equals("null"))
            {
                String response = getResponseForUrl(imageUrl);

                if (!response.equals("") && !response.equals("null"))
                {
                    String rePromptText = "If you want to get text from another image, simple say Alexa image url is \" your auth free image url \"";

                    return getSimpleCardWithTextSpeechLetResponse("Detected text from image",response,rePromptText,true);
                }
                else
                {
                    String speechText = "I could not find or access the image, please say correct and authentication free site url";

                    String cardTitle = "Image not found";

                    String rePromptText = "please say correct and authentication free site url.";

                    return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,rePromptText,true);
                }
            }
            else
            {
                return getFallbackResponse();
            }
        }
        else if (intentName != null && intentName.equals("AMAZON.FallbackIntent"))
        {
            return getFallbackResponse();
        }
        else if (intentName != null && intentName.equals("AMAZON.HelpIntent"))
        {
            return getHelpResponse();
        }
        else if (intentName != null && intentName.equals("AMAZON.StopIntent"))
        {
            return getStopOrCancelResponse();
        }
        else if (intentName != null && intentName.equals("AMAZON.CancelIntent"))
        {
            return getStopOrCancelResponse();
        }
        else if (intentName != null && intentName.equals("AMAZON.YesIntent"))
        {
            return getYesResponse();
        }
        else if (intentName != null && intentName.equals("AMAZON.NoIntent"))
        {
            return getNoResponse();
        }
        else
        {
            return getFallbackResponse();
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope)
    {

    }

    private String getResponseForUrl(String imageUrl)
    {
        try
        {
            BasicConfigurator.configure();

            URL url = new URL(imageUrl);

            InputStream in = new BufferedInputStream(url.openStream());

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];

            int n;

            while (-1!=(n=in.read(buf)))
            {
                out.write(buf, 0, n);
            }

            out.close();
            in.close();

            byte[] response = out.toByteArray();

            DetectTextRequest request = new DetectTextRequest().withImage(new Image().withBytes(ByteBuffer.wrap(response)));

            AWSCredentials credentials = new AWSCredentials()
            {
                @Override
                public String getAWSAccessKeyId()
                {
                    return "AKIAINP5QUYC67NNSFUA";
                }

                @Override
                public String getAWSSecretKey()
                {
                    return "CfmsXdlwrsrsqqkAuEQCOP8/CgEhkAfiTIhOnrpJ";
                }
            };

            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1).build();

            try
            {
                DetectTextResult result = rekognitionClient.detectText(request);
                List<TextDetection> textDetections = result.getTextDetections();

                StringBuilder stringBuilder = new StringBuilder();

                for (TextDetection text: textDetections)
                {
                    stringBuilder.append(text.getDetectedText());
                }

                String extractedText = stringBuilder.toString();

                if (!extractedText.equals("") && !extractedText.equals("null"))
                {
                    extractedText = extractedText + ". If you want to get text from another image, simple say Alexa image url is \" your auth free image url \"";

                    return extractedText;
                }
                else
                {
                    return "I could not find any text from the image, please say correct and clear text image url.";
                }
            }
            catch (AmazonRekognitionException e)
            {
                return "I could not find or access the image, please say correct and authentication free site url";
            }
        }
        catch (IOException e)
        {
            return "I could not find or access the image, please say correct and authentication free site url";
        }
    }

    private SpeechletResponse getWelcomeResponse()
    {
        String speechText = "Hi, Welcome to Alexa Text Reco. " +
                "It's a pleasure to talk to you. " +
                "My job is to extract the text from a picture. " +
                "Ok, now i give detailed instructions below to how to get the text from the images. " +
                "Please follow the instructions properly. " +
                "\n First of all choose your authentication free image url. " +
                "Authentication free url means, that site url does not contain any username or password. " +
                "Because, i can access only authentication free sites. " +
                "Also, another thing the site url must ends with jpg or png and the image size is less than 1Mb. "+
                "Next, tell the url to me. " +
                "I analyse the image and extract the text from image and show it to you. " +
                "Don't worry, i will use url only for extract the text, i don't do any unwanted things. " +
                "After finish the extraction, automatically my memory will erased. " +
                "If you want more instructions or help, ask \" Alexa ask text reco to say help.\" " +
                "Ok, now you can start to say some authentication free image url";

        String cardTitle = "Welcome";

        String rePromptText = "Ok, now you can start to say some authentication free image url";

        return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,rePromptText,true);
    }

    private SpeechletResponse getFallbackResponse()
    {
        String speechText = "Oops..There was some internal or server problem, don't worry. \nplease say that the name again.";

        String cardTitle = "Problem";

        String rePromptText = "please say that the name again.";

        return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,rePromptText,true);
    }

    private SpeechletResponse getHelpResponse()
    {
        String speechText = "It pleasure to help you." +
                "If you have any doubts or you don't know how to ask to 'text reco', don't worry. " +
                "I clarify your doubts. " +
                "I give you some examples to how to ask to 'text reco'." +
                "Ok, now let's start, you say, 'some authentication free image url', i analyse the image and extract the text from image and show it to you." +
                "Ok, now what is authentication free site, the url site does not contain any username or password that site is called authentication free site." +
                "Also, another thing the site url must ends with jpg or png and the image size is less than 1Mb. " +
                "Ok i say some example image site urls, the site urls are, " +
                "1. https://s3.amazonaws.com/imagefiber/1484634776440_700.jpg, " +
                "2. https://i.stack.imgur.com/t3qWG.png." +
                "3. https://upload.wikimedia.org/wikipedia/commons/c/ce/Textbeispiel_Kleinschreibung.png. " +
                "Ok, i trust the above site urls are useful to you." +
                "Don't worry, i will use url only for extract the text, i don't do any unwanted things." +
                "Ok, now you can start to say some authentication free image url";

        String cardTitle = "Help";

        String rePromptText = "Ok, now you can start to say some authentication free image url";

        return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,rePromptText,true);
    }

    private SpeechletResponse getStopOrCancelResponse()
    {
        String speechText = "Would you like to cancel or stop all the conversations?";

        String cardTitle = "Stop or Cancel";

        return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,speechText,true);
    }

    private SpeechletResponse getYesResponse()
    {
        String speechText = "Ok, i stopped the all conversations. If you like to speak to me again. Simply you can ask or say \"Alexa, open text reco and url of the image is \" your auth free url \".";

        return getSimpleCardWithTextSpeechLetResponse("GoodBye!",speechText,speechText,false);
    }

    private SpeechletResponse getNoResponse()
    {
        String speechText = "Ok, don't worry. We can continue the conversation. \nPlease, tell me the auth free image url";

        String cardTitle = "Continue Conversation";

        String rePrompt = "Please, tell me the auth free image url";

        return getSimpleCardWithTextSpeechLetResponse(cardTitle,speechText,rePrompt,true);
    }

    private SpeechletResponse getSimpleCardWithTextSpeechLetResponse(String cardTitle,String speechText, String repromptText, boolean isAskResponse)
    {
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse)
        {
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        }
        else
        {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
}
