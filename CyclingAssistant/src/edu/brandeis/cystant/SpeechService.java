package edu.brandeis.cystant;


import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class SpeechService implements
RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "hi assistant";

    public static final String MSG_SpeechRecognized = "SpeechRec";
    public static final String MSG_SpeechEnd = "SpeechEnd";
    
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    
    private TextToSpeech tts;
    private MessageListener listener;


    public void start(final Context ctx,MessageListener msg) {

    	this.listener=msg;
    	tts=new TextToSpeech(ctx, new OnInitListener() {
    		public void onInit(int status) {
    			if(status != TextToSpeech.ERROR){
    				try {
						setLocale("EN");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("TTS", "Error swiching Locale");
					}
    		    }
    			else
    			{
    				Log.e("TTS", "Error creating the TTS");
    			}
    			
    		}
    	});


        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
            	try {

                    Assets assets = new Assets(ctx);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                	Log.e("Sphinx", "Error creating the TTS");
                 //   ((TextView) findViewById(R.id.caption_text))
                 //           .setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();

        Log.w("Sphinx", "PartialResult " + text);
        if (text.equals("hi assistant"))//text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals("what time is it"))
            switchSearch(KWS_SEARCH);
        else if (text.endsWith("location"))
            switchSearch(KWS_SEARCH);
        else if (text.endsWith("speed"))
            switchSearch(KWS_SEARCH);

      //  else if (text.equals(FORECAST_SEARCH))
        //    switchSearch(FORECAST_SEARCH);
      //  else
         //   ((TextView) findViewById(R.id.result_text)).setText(text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        //((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
        	
            String text = hypothesis.getHypstr();
            Log.w("Sphinx", "Result " + text);
            SendMessage(MSG_SpeechRecognized,text);
          //  makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void SendMessage(String type,Object param)
    {
    	listener.OnMessage(type, param);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    	Log.w("Sphinx", "Endof Speech ");
    	SendMessage(MSG_SpeechEnd,null);
    }

    private void switchSearch(String searchName) {
    	Log.w("Sphinx", "switch "+searchName);
        recognizer.stop();
        recognizer.startListening(searchName);
       // String caption = getResources().getString(captions.get(searchName));
       // ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create grammar-based searches.
        File menuGrammar = new File(modelsDir, "grammar/menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
       // File digitsGrammar = new File(modelsDir, "grammar/digits.gram");
        //recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
        // Create language model search.
       // File languageModel = new File(modelsDir, "lm/weather.dmp");
       // recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
    }
    
	public void setLocale(String languageCode, String countryCode) throws Exception{
	    if(languageCode==null)
	    {
	    	setLocale();
	    	throw new Exception("Language code was not provided, using default locale");
	    }
	    else{
	    	if(countryCode==null)
	    		setLocale(languageCode);
	    	else {
	    		Locale lang = new Locale(languageCode, countryCode);
		    	if (tts.isLanguageAvailable(lang) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE )
		    		tts.setLanguage(lang);
		    	{
		    		setLocale();
		    		throw new Exception("Language or country code not supported, using default locale");
		    	}
	    	}
	    }
	}
	
	public void setLocale(String languageCode) throws Exception{
		if(languageCode==null)
		{
			setLocale();
			throw new Exception("Language code was not provided, using default locale");
		}
		else {
			Locale lang = new Locale(languageCode);
			if (tts.isLanguageAvailable(lang) != TextToSpeech.LANG_MISSING_DATA && tts.isLanguageAvailable(lang) != TextToSpeech.LANG_NOT_SUPPORTED)
				tts.setLanguage(lang);
			else
			{
				setLocale();
				throw new Exception("Language code not supported, using default locale");
			}
		}
	}
	public void setLocale(){
		tts.setLanguage(Locale.getDefault());
	}


	public void speak(String text){
		tts.speak(text, TextToSpeech.QUEUE_ADD, null); 		
	}
	
	public void speak(String text,boolean flush){
		tts.speak(text, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null); 		
	}


	public void stopSpeaking(){
		if(tts.isSpeaking())
			tts.stop();
	}

	public void shutdown(){
		tts.stop();
		tts.shutdown();
	}
}
