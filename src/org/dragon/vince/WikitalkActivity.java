package org.dragon.vince;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WikitalkActivity extends Activity implements TextToSpeech.OnInitListener {
	static final String WIKITALK = "wikitalk";
	private static final String TAG = "TextToSpeechDemo";
    private TextToSpeech mTts;
    private Button mAgainButton;
    private Button mGetWP;
    private Button mReadIt;
    private Button mStopRead;
    private TextView mTxt;
    private RetrievePageTask pageTask;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.pageTask = new RetrievePageTask(this);
        
     // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this,
            this  //TextToSpeech.OnInitListener
            );
        mAgainButton = (Button) findViewById(R.id.again_button);
        mAgainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                sayHello();
            }
        });
        
        mTxt = (TextView) findViewById(R.id.editWP);
        mGetWP = (Button) findViewById(R.id.getWikiPedia);
        mGetWP.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				initTxt();
				pageTask.execute("Albert%20Einstein");				
			}
		});
        
        mReadIt = (Button) findViewById(R.id.readIt);
        mReadIt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				readText();
			}
		});
        
        mStopRead = (Button) findViewById(R.id.stopRead);
        mStopRead.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (mTts.isSpeaking()) {
					mTts.stop();
				}
			}
		});
    }

	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
                       int result = mTts.setLanguage(Locale.FRANCE);
           
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.
                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                mAgainButton.setEnabled(true);
                // Greet the user.
                sayHello();
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
	}
	
	private static final Random RANDOM = new Random();
    private static final String[] HELLOS = {
      "Salut",
      "Le bouton ci-dessous récupérera les informations d'une recherche sur Albert Einstein",
      "Le bouton suivant lira l'article wikipedia"
      
    };
    int i =0;
    private void sayHello() {
        // Select a random hello.
        int helloLength = HELLOS.length;
        String hello = HELLOS[i];
        i++;
        if(i == helloLength) i =0;
        mTts.speak(hello,
            TextToSpeech.QUEUE_FLUSH,  // Drop allpending entries in the playback queue.
            null);
    }
	
	public Document XMLfromString(String xml){

	Document doc = null;

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

		DocumentBuilder db = dbf.newDocumentBuilder();

		InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        doc = db.parse(is); 

		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}

        return doc;
	}

	private String textToRead;
	
	private void initTxt() {
		textToRead = "";
		mTxt.setText(textToRead);
	}
	
	public void addTextToRead(String line) {
		this.textToRead += line;
		this.mTxt.setText(this.textToRead);
	}
	
	public void readText() {
		String[] splitSentence = this.textToRead.split("\\.");
		for(String sentence : splitSentence) {
			// Parse wikimedia tag here
			sentence = sentence.replaceAll("[\\[\\]]", "");
			// sentence = sentence.replaceAll("<br />", "");
			// sentence = sentence.replaceAll("<ref>", "");
			// sentence = sentence.replaceAll("</ref>", "");
			// sentence = sentence.replaceAll("/>", "");
			// sentence = sentence.replaceAll("<ref ", "");
			if (sentence.trim().length() > 0 && 
					!sentence.contains("{") && 
					!sentence.contains("}") &&
					!sentence.contains("|") && 
					!sentence.contains("[") && 
					!sentence.contains("]") &&
					!sentence.contains("<a") &&
					!sentence.contains("_")) {
				mTts.speak(sentence,
			            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
			            null);
			}			
		}
		
//		mTts.speak(this.textToRead,
//	            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
//	            null);
	}
}