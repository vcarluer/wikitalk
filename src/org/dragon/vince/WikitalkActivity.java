package org.dragon.vince;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WikitalkActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
	static final String WIKITALK = "wikitalk";
	private static final String TAG = "TextToSpeechDemo";
    private TextToSpeech mTts;
    private Button mGetWP;
    private Button mReadIt;
    private Button mStopRead;
    private TextView mTxt;
    private ImageView mImage;
    private Button mImgNext;
    private Button mImgPrev;
    private TextView mTitle;
    private Handler mHandler;
    private Spinner mSupportedLanguageView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
     // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this,
            this  //TextToSpeech.OnInitListener
            );
                
        mTxt = (TextView) findViewById(R.id.editWP);
        mGetWP = (Button) findViewById(R.id.getWikiPedia);
        mGetWP.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				search("Albert%20Einstein");				
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
        

        this.mImage = (ImageView) findViewById(R.id.wikiImage);
        this.mImage.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showFullImage();
			}
		});
        
        this.mImgPrev = (Button) findViewById(R.id.imgPrevious);
        this.mImgPrev.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				previousImage();
			}
		});
        
        this.mImgNext = (Button) findViewById(R.id.imgNext);
        this.mImgNext.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				nextImage();				
			}
		});
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          search(query);
        }
        
        this.mTitle = (TextView) findViewById(R.id.txtTitle);
        mSupportedLanguageView = (Spinner) findViewById(R.id.supported_languages);
        mHandler = new Handler();
     // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            this.mTitle.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
        } else {
            this.mTitle.setEnabled(false);
            this.mTitle.setText("Recognizer not present");
        }
        
     // Most of the applications do not have to handle the voice settings. If the application
        // does not require a recognition in a specific language (i.e., different from the system
        // locale), the application does not need to read the voice settings.
        refreshVoiceSettings();
    }
    
    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Rechercher dans Wikitalk");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Specify the recognition language. This parameter has to be specified only if the
        // recognition has to be done in a specific language and not the default one (i.e., the
        // system locale). Most of the applications do not have to set this parameter.
        if (!mSupportedLanguageView.getSelectedItem().toString().equals("Default")) {
            String extraLang = mSupportedLanguageView.getSelectedItem().toString();
        	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    extraLang);
        }
        
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "French");

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
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

	private String textToRead;
	private List<ImageInfo> images;
	private int imageCursor;
	
	private void initTxt(String txt) {
		textToRead = txt;
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
	
	public void setImages(List<ImageInfo> images) {
		this.images = images;
		this.showFirstImage();
	}
	
	private void showFirstImage() {
		this.imageCursor = 0;
		this.showImage();
	}

	private void showImage() {
		if (this.images.size() > this.imageCursor) {
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			retrieveImage.execute(this.images.get(this.imageCursor).thumbUrl);
		}
	}
	
	private void showFullImage() {
		if (this.images.size() > 0 && this.images.size() > this.imageCursor) {
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			retrieveImage.execute(this.images.get(this.imageCursor).url);
		}
	}
	
	public void showImage(Bitmap bitmap) {
		this.mImage.setImageBitmap(bitmap);
	}
	
	public void previousImage() {
		this.imageCursor--;
		if (this.imageCursor < 0) {
			this.imageCursor = this.images.size() - 1;
		}
		
		this.showImage();
	}
	
	public void nextImage() {
		this.imageCursor++;
		if (this.imageCursor >= this.images.size()) {
			this.imageCursor = 0;
		}
		
		this.showImage();
	}
	
	public void search(String toSearch) {
		RetrievePageTask pageTask = new RetrievePageTask(this);
		initTxt(toSearch);
		pageTask.execute(toSearch);
	}
	
	// voice reco
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
	            // Fill the list view with the strings the recognizer thought it could have heard
	            ArrayList<String> matches = data.getStringArrayListExtra(
	                    RecognizerIntent.EXTRA_RESULTS);
	            if (matches.size() > 0) {
	            	// Search first match
	            	this.search(matches.get(0));
	            }
	        }

	        super.onActivityResult(requestCode, resultCode, data);
	    }

	    private void refreshVoiceSettings() {
	        Log.i(TAG, "Sending broadcast");
	        sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(this), null,
	                new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
	    }

	    private void updateSupportedLanguages(List<String> languages) {
	        // We add "Default" at the beginning of the list to simulate default language.
	        languages.add(0, "Default");

	        SpinnerAdapter adapter = new ArrayAdapter<CharSequence>(this,
	                android.R.layout.simple_spinner_item, languages.toArray(
	                        new String[languages.size()]));
	        mSupportedLanguageView.setAdapter(adapter);
	    }

	    private void updateLanguagePreference(String language) {
	        TextView textView = (TextView) findViewById(R.id.language_preference);
	        textView.setText(language);
	    }

	    /**
	     * Handles the response of the broadcast request about the recognizer supported languages.
	     *
	     * The receiver is required only if the application wants to do recognition in a specific
	     * language.
	     */
	    private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {

	        public void onReceive(Context context, final Intent intent) {
	            Log.i(TAG, "Receiving broadcast " + intent);

	            final Bundle extra = getResultExtras(false);

	            if (getResultCode() != Activity.RESULT_OK) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        showToast("Error code:" + getResultCode());
	                    }
	                });
	            }

	            if (extra == null) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        showToast("No extra");
	                    }
	                });
	            }

	            if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
	                mHandler.post(new Runnable() {

	                    public void run() {
	                        updateSupportedLanguages(extra.getStringArrayList(
	                                RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
	                    }
	                });
	            }

	            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
	                mHandler.post(new Runnable() {

	                    public void run() {
	                        updateLanguagePreference(
	                                extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
	                    }
	                });
	            }
	        }

	        private void showToast(String text) {
	            Toast.makeText(WikitalkActivity.this, text, 1000).show();
	        }
	    }
}