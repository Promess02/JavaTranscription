package org.example;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class JavaTranscription
{
    public static void main( String[] args ) throws Exception
    {
        Transcript transcript = new Transcript();// object to store our transcription
        //set audio url
        transcript.setAudio_url("https://actions.google.com/sounds/v1/human_voices/pa_announcement_close.ogg");
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);//converts transcript object to String in json format
        System.out.println(jsonRequest);//prints body of post request
        //builds a post Request to assemblyai api
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", Constants.API_KEY)//header containing API key
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))//adds body to POST request
                .build();

        //create an httpClient object
        HttpClient httpClient = HttpClient.newHttpClient();
        // sends postRequest via httpClient and stores the post Response inside a postResponse object
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(postResponse.body());

        //converts the PostResponse to our transcript object in order to get the post response id
        //to use later in our get request inside the url
        transcript = gson.fromJson(postResponse.body(), Transcript.class);

        System.out.println(transcript.getId());

        //creating a Get Request
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId())) //using the id given by the post response
                .header("Authorization", Constants.API_KEY)//api key authorization inside the header
                .GET()
                .build();

        // loop that operates until given a getResponse with a "completed" or "error" status
        while(true){
            // sending the getRequest via httpClient and saving the response to a getResponse object
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            //converting the body of getResponse to a transcript object
            transcript = gson.fromJson(getResponse.body(), Transcript.class);
            // printing transcript status once every second
            System.out.println(transcript.getStatus());
            //breaks out of the loop if status is "completed" or "error"
            if("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) break;
            Thread.sleep(1000);
        }

        System.out.println("Transcription completed");
        System.out.println(transcript.getText());

    }
}
